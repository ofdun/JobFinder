#!/usr/bin/env node
// Contract-only checks; no server, resolvers, database or network is used.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { createRequire } = require('node:module');
const root = process.env.JOBFINDER_ROOT || path.resolve(__dirname, '..');
const dependency = createRequire(path.join(root, 'JobFinder-front/package.json'));
const { buildSchema, validateSchema, parse, validate, Kind, TypeInfo, visit, visitWithTypeInfo } = dependency('graphql');
const { getVariableValues } = dependency('graphql/execution/values');
const Ajv = dependency('ajv').default;
const addFormats = dependency('ajv-formats');
const schemaPath = path.resolve(process.argv[2] || path.join(root, 'docs/schema.graphqls'));
const examplesPath = path.resolve(process.argv[3] || path.join(root, 'docs/graphql.md'));

function main() {
  const schema = buildSchema(fs.readFileSync(schemaPath, 'utf8'));
  const ajv = new Ajv({ allErrors: true });
  addFormats(ajv, { mode: 'full' });
  const scalarSchemas = {
    Date: { type: 'string', format: 'date' },
    DateTime: { type: 'string', format: 'date-time' },
    Email: { type: 'string', format: 'email' },
    URL: { type: 'string', format: 'uri', pattern: '^https?://' },
    Decimal: { type: 'string', pattern: '^(0|[1-9][0-9]*)(\\.[0-9]+)?$' },
    Long: { type: 'string', pattern: '^(0|[1-9][0-9]*)$' },
  };
  for (const [name, scalarSchema] of Object.entries(scalarSchemas)) {
    const type = schema.getType(name);
    if (!type) throw new Error(`Missing scalar ${name}`);
    const isValid = ajv.compile(scalarSchema);
    function coerce(value) {
      if (!isValid(value)) throw new TypeError(`${name}: ${ajv.errorsText(isValid.errors)}`);
      if (name === 'Long' && BigInt(value) > 9223372036854775807n) throw new TypeError('Long exceeds Java Long range');
      return value;
    }
    type.parseValue = coerce;
    type.serialize = coerce;
    type.parseLiteral = node => {
      if (node.kind !== Kind.STRING) throw new TypeError(`${name} requires a string literal`);
      return coerce(node.value);
    };
  }
  const schemaErrors = validateSchema(schema);
  if (schemaErrors.length) throw new Error(schemaErrors.map(error => error.message).join('\n'));
  const markdown = fs.readFileSync(examplesPath, 'utf8');
  const blocks = [...markdown.matchAll(/```graphql\s*\n([\s\S]*?)```/g)].map(match => match[1]);
  if (!blocks.length) throw new Error('No documented GraphQL operations');
  const fixturesMatch = markdown.match(/<!-- graphql-variables -->\s*```json\s*\n([\s\S]*?)```/);
  if (!fixturesMatch) throw new Error('Missing documented variable fixtures');
  const fixtures = JSON.parse(fixturesMatch[1]);
  const document = parse(blocks.join('\n'));
  const operationErrors = validate(schema, document);
  if (operationErrors.length) throw new Error(operationErrors.map(error => error.message).join('\n'));
  const operations = document.definitions.filter(definition => definition.kind === Kind.OPERATION_DEFINITION);
  const names = new Set();
  for (const operation of operations) {
    if (!operation.name) throw new Error('Every example must have an operationName');
    const name = operation.name.value;
    names.add(name);
    const variables = fixtures[name] || {};
    const declared = new Set(operation.variableDefinitions.map(definition => definition.variable.name.value));
    for (const key of Object.keys(variables)) if (!declared.has(key)) throw new Error(`${name}: unknown fixture variable ${key}`);
    const result = getVariableValues(schema, operation.variableDefinitions, variables);
    if (result.errors) throw new Error(`${name}: ${result.errors.map(error => error.message).join('; ')}`);
  }
  for (const name of Object.keys(fixtures)) if (!names.has(name)) throw new Error(`Unknown operation fixture ${name}`);
  const covered = new Set();
  const info = new TypeInfo(schema);
  visit(document, visitWithTypeInfo(info, {
    Field(node) {
      const parent = info.getParentType();
      if (parent === schema.getQueryType() || parent === schema.getMutationType()) covered.add(`${parent.name}.${node.name.value}`);
    },
  }));
  for (const type of [schema.getQueryType(), schema.getMutationType()]) {
    for (const name of Object.keys(type.getFields())) {
      if (!covered.has(`${type.name}.${name}`)) throw new Error(`Missing documented example for ${type.name}.${name}`);
    }
  }
  let negativeChecks = 0;
  function reject(source, variables = {}) {
    let errors;
    try {
      const candidate = parse(source);
      errors = validate(schema, candidate);
      if (!errors.length) {
        const operation = candidate.definitions.find(definition => definition.kind === Kind.OPERATION_DEFINITION);
        errors = getVariableValues(schema, operation.variableDefinitions, variables).errors || [];
      }
    } catch (error) { errors = [error]; }
    assert.ok(errors.length, `Invalid example unexpectedly accepted: ${source}`);
    negativeChecks++;
  }
  reject('query Broken {');
  reject('query UnknownField { vacancy(id: "10") { missingField } }');
  reject('query Secret { employer(id: "2") { password } }');
  reject('mutation MissingInput { createVacancy(input: {description: "Java"}) { id } }');
  reject('mutation BadStatus { updateApplicationStatus(id: "40", status: OTHER) { id } }');
  reject('query BadPage($page: PageInput) { vacancies(page: $page) { totalElements } }', { page: { limit: '20' } });
  reject('mutation MissingVariable($id: ID!) { deleteVacancy(id: $id) { id } }');
  reject('query BadDateLiteral { resumes(filter: {creationDateFrom: "not-a-date"}) { totalElements } }');
  reject('query BadDateVariable($filter: ResumeFilter) { resumes(filter: $filter) { totalElements } }', { filter: { creationDateFrom: '2026-02-30T10:00:00Z' } });
  reject('mutation BadCalendarDate { createResume(input: {applicantId: "1", categoryId: "1", jobExperiences: [{startDate: "2026-02-30"}]}) { id } }');
  reject('mutation BadEmail { login(input: {accountType: APPLICANT, email: "invalid", password: "ExamplePass123!"}) { accessToken } }');
  reject('mutation BadUrl { registerEmployer(input: {name: "Example", email: "hr@example.com", password: "ExamplePass123!", locationId: "1", websiteLink: "ftp://example.com"}) { id } }');
  reject('query BadMoney { vacancies(filter: {salaryMin: "-1.00"}) { totalElements } }');
  reject('query NumericMoney { vacancies(filter: {salaryMin: 150000}) { totalElements } }');
  reject('mutation InjectStatus { createApplication(input: {vacancyId: "10", resumeId: "20", applicationStatus: INVITATION}) { id } }');
  assert.equal(schema.getType('Long').serialize('9223372036854775807'), '9223372036854775807');
  assert.throws(() => schema.getType('Long').serialize('9223372036854775808'), /range/);
  negativeChecks++;
  console.log(`GraphQL SDL, ${operations.length} operations, ${Object.keys(fixtures).length} variable sets and ${covered.size} root fields passed; ${negativeChecks} invalid examples rejected. No backend executed.`);
}
try { main(); } catch (error) { console.error(error.message); process.exitCode = 1; }
