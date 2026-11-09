import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import { Counter, Gauge, Rate, Trend } from 'k6/metrics';

const plan = JSON.parse(open(__ENV.PLAN || '/results/plan.json'));
const base = (__ENV.BASE_URL || 'http://backend:8080').replace(/\/$/, '');
if (!/^http:\/\/(backend|localhost|127\.0\.0\.1|host\.docker\.internal)(:\d+)?$/.test(base)) {
  throw new Error('Only the isolated local test backend is allowed');
}
const latency = new Trend('operation_latency_ms', true);
const unexpected = new Rate('unexpected_errors');
const intentional = new Rate('intentional_errors');
const completed = new Counter('journeys_completed');
const interrupted = new Counter('journeys_failed');
const started = new Gauge('load_started_ms');
const stages = plan.phases.map(p => ({ duration: `${p.seconds}s`, target: p.vus }));
export const options = {
  scenarios: {
    users: { executor: 'ramping-vus', startVUs: plan.phases[0].vus, stages,
      gracefulRampDown: '15s', gracefulStop: '15s' },
  },
  maxRedirects: 0,
  summaryTrendStats: ['avg', 'min', 'med', 'p(75)', 'p(90)', 'p(95)', 'p(99)', 'max'],
  systemTags: ['status', 'method', 'name', 'scenario', 'expected_response'],
  thresholds: {
    'unexpected_errors{phase:measurement}': ['rate<0.01'],
    'operation_latency_ms{phase:measurement,expected_error:false}': [`p(95)<${plan.latency_limit_ms}`],
    journeys_completed: ['count>0'],
  },
};

function phase() {
  let seconds = (Date.now() - exec.scenario.startTime) / 1000;
  for (const item of plan.phases) {
    if (seconds < item.seconds) return item;
    seconds -= item.seconds;
  }
  return { name: 'drain', kind: 'drain', vus: 0 };
}

function request(method, path, name, token, payload, status, valid = () => true, bad = false) {
  const current = phase();
  const response = http.request(method, base + '/api/v1' + path,
    payload === null ? null : JSON.stringify(payload), {
      timeout: `${plan.request_timeout_seconds}s`,
      responseCallback: http.expectedStatuses(status),
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      tags: { name, phase: current.kind, stage: current.name },
    });
  let value;
  try { value = response.json(); } catch (_) { value = null; }
  const ok = response.status === status && valid(value);
  const tags = { operation: name, phase: current.kind, stage: current.name,
    expected_error: String(bad), unexpected: String(!ok), status: String(response.status) };
  latency.add(response.timings.duration, tags);
  unexpected.add(!ok, tags);
  intentional.add(bad, tags);
  check(response, { 'expected status and response data': () => ok }, tags);
  return { ok, value };
}

export default function () {
  if (exec.vu.idInTest === 1 && exec.vu.iterationInScenario === 0) started.add(exec.scenario.startTime);
  const id = 1000 + exec.vu.idInTest;
  let application = null;
  let token;
  let finished = false;
  try {
    const auth = request('POST', '/auth/applicant/login', 'login', null,
      { email: `load-${exec.vu.idInTest}@example.test`, password: 'load-password' }, 200,
      body => typeof body?.accessToken === 'string');
    if (!auth.ok) return;
    token = auth.value.accessToken;
    sleep(plan.think_seconds / 3);
    const search = request('GET', '/vacancies/search?limit=20&offset=0', 'vacancy_search', token, null, 200,
      body => Array.isArray(body?.items) && body.items.length > 0);
    if (!search.ok) return;
    const vacancy = search.value.items[exec.scenario.iterationInTest % search.value.items.length].id;
    if (!request('GET', `/vacancies/${vacancy}`, 'vacancy_details', token, null, 200,
      body => body?.id === vacancy).ok) return;
    if (!request('GET', `/applicants/${id}`, 'own_profile', token, null, 200,
      body => body?.id === id).ok) return;
    if (!request('GET', `/resumes/${id}`, 'own_resume', token, null, 200,
      body => body?.id === id).ok) return;
    sleep(plan.think_seconds / 3);
    const payload = { vacancyId: vacancy, resumeId: id, applicationStatus: 'NEW' };
    const created = request('POST', '/applications', 'apply', token, payload, 201,
      value => Number.isInteger(value) && value > 0);
    if (!created.ok) return;
    application = created.value;
    if (!request('GET', `/applications/${application}`, 'application_status', token, null, 200,
      body => body?.applicationStatus === 'NEW').ok) return;
    if (!request('DELETE', `/applications/${application}`, 'withdraw', token, null, 204).ok) return;
    application = null;
    const first = plan.error_percent >= 10
      ? request('GET', '/applications/1', 'missing_auth', null, null, 403, () => true, true)
      : request('GET', '/categories', 'categories', token, null, 200, Array.isArray);
    const second = plan.error_percent >= 20
      ? request('POST', '/applications', 'invalid_application', token, {}, 400, () => true, true)
      : request('GET', '/skills', 'skills', token, null, 200, Array.isArray);
    finished = first.ok && second.ok;
    if (finished) completed.add(1, { phase: phase().kind, stage: phase().name });
  } finally {
    if (application !== null && token) {
      request('DELETE', `/applications/${application}`, 'cleanup_application', token, null, 204);
    }
    if (!finished) interrupted.add(1, { phase: phase().kind, stage: phase().name });
    sleep(plan.think_seconds / 3);
  }
}

export function handleSummary(data) {
  return { '/results/k6-summary.json': JSON.stringify(data, null, 2) };
}
