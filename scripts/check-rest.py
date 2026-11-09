#!/usr/bin/env python3
"""Exercise the real REST API using isolated records, then delete only those records."""
import argparse
import json
import secrets
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--base-url', default='http://127.0.0.1:8080')
parser.add_argument('--location-query', default='Moscow', help='Search term in the seeded location catalogue')
args = parser.parse_args()
base = args.base_url.rstrip('/')
api = '/api/v2'
checks = 0
cleanup = []

def request(method, path, expected=200, body=None, token=None, accept="application/json"):
    global checks
    headers = {'Accept': accept}
    if body is not None:
        headers['Content-Type'] = 'application/json'
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(base + path, data=None if body is None else json.dumps(body).encode(), headers=headers, method=method)
    try:
        response = urllib.request.urlopen(req, timeout=90)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        raw = response.read()
        assert response.status == expected, f'{method} {path}: expected {expected}, got {response.status}'
        checks += 1
        data = json.loads(raw) if raw and 'json' in response.headers.get('Content-Type', '') else raw
        return data, response.headers

def call(method, path, **kw):
    return request(method, api + path, **kw)[0]

def eventually(path, token=None):
    # Replicated reads may lag a just-confirmed write.
    for attempt in range(20):
        try:
            return call('GET', path, token=token)
        except AssertionError as error:
            if 'got 404' not in str(error) or attempt == 19:
                raise
            time.sleep(0.5)

try:
    locations = call('GET', '/locations?q=' + urllib.parse.quote(args.location_query) + '&limit=1')
    assert locations, 'No matching location; select a seeded city with --location-query.'
    location = locations[0]['id']
    category = call('GET', '/categories')[0]['id']
    assert isinstance(call('GET', '/vacancies?sortBy=salary&limit=1')['items'], list)
    call('GET', '/vacancies?limit=0', expected=400)
    call('GET', '/vacancies?sortBy=password', expected=400)
    call('GET', '/locations', expected=400)
    call('GET', '/resumes', expected=401)
    for role in [None, 'ADMIN', 'applicant']:
        payload = {'email': 'missing@example.com', 'password': 'ExamplePass123!'}
        if role is not None:
            payload['userType'] = role
        call('POST', '/auth/login', body=payload, expected=400)
        payload = {'refreshToken': 'invalid', **({'userType': role} if role is not None else {})}
        call('POST', '/auth/refresh', body=payload, expected=400)
    call('POST', '/auth/login', body={'email': 'missing-' + uuid.uuid4().hex + '@example.com', 'password': 'ExamplePass123!', 'userType': 'APPLICANT'}, expected=401)
    actors = {}
    for role, collection in [('APPLICANT', 'applicants'), ('EMPLOYER', 'employers')]:
        email = 'contract-' + uuid.uuid4().hex + '@example.com'
        password = secrets.token_urlsafe(20)
        data = {'name': 'Contract verification', 'email': email, 'password': password, 'locationId': location}
        identity, headers = request('POST', api + '/' + collection, expected=201, body=data)
        assert isinstance(identity, int) and identity > 0
        assert headers['Location'].endswith('/' + collection + '/' + str(identity))
        actor = {'id': identity, 'token': None}
        cleanup.append((collection, identity, actor))
        login = {'email': email, 'password': password, 'userType': role}
        pair = call('POST', '/auth/login', body=login)
        actor['token'] = pair['accessToken']
        wrong_role = 'EMPLOYER' if role == 'APPLICANT' else 'APPLICANT'
        call('POST', '/auth/refresh', body={'refreshToken': pair['refreshToken'], 'userType': wrong_role}, expected=401)
        renewed = call('POST', '/auth/refresh', body={'refreshToken': pair['refreshToken'], 'userType': role})
        actor['token'] = renewed['accessToken']
        call('POST', '/auth/refresh', body={'refreshToken': pair['refreshToken'], 'userType': role}, expected=401)
        call('POST', '/auth/refresh', body={'refreshToken': renewed['accessToken'], 'userType': role}, expected=401)
        assert eventually('/' + collection + '/' + str(identity), actor['token'])['id'] == identity
        legacy, _ = request('POST', '/api/v1/auth/' + role.lower() + '/login', body={'email': email, 'password': password})
        assert legacy['accessToken']
        actors[role] = actor
    applicant, employer = actors['APPLICANT'], actors['EMPLOYER']
    vacancy_data = {'employerId': employer['id'], 'locationId': location, 'salary': 150000, 'paymentFrequency': 'MONTHLY', 'experience': 'One year', 'jobFormat': 'REMOTE', 'employmentType': 'FULL_TIME', 'description': 'Contract verification vacancy', 'address': 'Москва', 'skillIds': [], 'languageIds': [], 'status': 'ACTIVE'}
    vacancy = call('POST', '/vacancies', body=vacancy_data, token=employer['token'], expected=201)
    cleanup.append(('vacancies', vacancy, employer))
    assert eventually('/vacancies/' + str(vacancy))['employerId'] == employer['id']
    for _ in range(6):
        assert isinstance(call('GET', '/vacancies?employerId=' + str(employer['id']) + '&sortBy=salary')['items'], list)
    resume_data = {'applicantId': applicant['id'], 'categoryId': category, 'description': 'Java API developer', 'skillIds': [], 'languageIds': [], 'educations': [], 'jobExperiences': []}
    resume = call('POST', '/resumes', body=resume_data, token=applicant['token'], expected=201)
    cleanup.append(('resumes', resume, applicant))
    assert eventually('/resumes/' + str(resume), applicant['token'])['applicantId'] == applicant['id']
    call('GET', '/resumes?applicantId=' + str(applicant['id']), token=applicant['token'])
    call('GET', '/resumes?applicantId=' + str(applicant['id'] + 1000000), token=applicant['token'], expected=403)
    call('GET', '/resumes?limit=1', token=employer['token'])
    application = call('POST', '/applications', body={'vacancyId': vacancy, 'resumeId': resume, 'applicationStatus': 'NEW'}, token=applicant['token'], expected=201)
    cleanup.append(('applications', application, applicant))
    eventually('/applications/' + str(application), employer['token'])
    status_path = '/applications/' + str(application) + '/status'
    call('PUT', status_path, body={'applicationStatus': 'INVITATION'}, token=applicant['token'], expected=403)
    call('PUT', status_path, body={'applicationStatus': 'INVALID'}, token=employer['token'], expected=400)
    for _ in range(2):
        changed = call('PUT', status_path, body={'applicationStatus': 'INVITATION'}, token=employer['token'])
        assert changed['applicationStatus'] == 'INVITATION' and changed['resumeId'] == resume and changed['vacancyId'] == vacancy
    assert eventually('/applications/' + str(application), applicant['token'])['applicationStatus'] == 'INVITATION'
    draft = call('POST', '/vacancies/' + str(vacancy) + '/drafts', token=employer['token'], expected=201)
    draft_path = '/vacancies/' + str(vacancy) + '/drafts/' + str(draft['id'])
    changed_data = {**vacancy_data, 'description': 'Edited after snapshot', 'status': 'INACTIVE'}
    call('PUT', '/vacancies/' + str(vacancy), body=changed_data, token=employer['token'])
    call('POST', draft_path, token=applicant['token'], expected=403)
    restored = call('POST', draft_path, token=employer['token'])
    assert restored['description'] == vacancy_data['description'] and restored['status'] == 'ACTIVE'
    call('DELETE', draft_path, token=employer['token'], expected=204)
    legacy, _ = request('GET', '/api/v1/vacancies/search?limit=1')
    assert isinstance(legacy['items'], list)
    graphql, _ = request('POST', api, body={'query': 'query ContractCheck { categories { id name } }', 'operationName': 'ContractCheck'}, accept='application/graphql-response+json')
    assert not graphql.get('errors') and graphql['data']['categories']
    ui, _ = request('GET', api + '/docs', accept='text/html')
    assert b'swagger' in ui.lower()
    contract, _ = request('GET', api + '/openapi.yml', accept='*/*')
    assert b'/api/v2/auth/login:' in contract and b'/api/v2/auth/applicant/login:' not in contract
    print(f'PASS: {checks} real HTTP checks on {base}; role selection, refresh rotation, collections, status PUT, draft restoration, v1, GraphQL and Swagger.')
finally:
    errors = []
    for resource, identity, actor in reversed(cleanup):
        if not actor['token']:
            errors.append(f'{resource}/{identity}: no login token available for cleanup')
            continue
        try:
            call('DELETE', '/' + resource + '/' + str(identity), token=actor['token'], expected=204)
        except Exception as error:
            errors.append(str(error))
    if errors:
        raise RuntimeError('Cleanup incomplete: ' + '; '.join(errors))
    if cleanup:
        print(f'Cleanup: deleted all {len(cleanup)} records created by this run.')
