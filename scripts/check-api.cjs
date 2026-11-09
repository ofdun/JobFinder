#!/usr/bin/env node
// Uses the versions already pinned by JobFinder-front/package-lock.json.
const fs = require('node:fs');
const path = require('node:path');
const { createRequire } = require('node:module');
const root = process.env.JOBFINDER_ROOT || path.resolve(__dirname, '..');
const dependency = createRequire(path.join(root, 'JobFinder-front/package.json'));
const { createConfig, lintFromString } = dependency('@redocly/openapi-core');
const yaml = dependency('js-yaml');
const Ajv2020 = dependency('ajv/dist/2020').default;
const addFormats = dependency('ajv-formats');
const methods = new Set(['get', 'post', 'put', 'patch', 'delete', 'head', 'options', 'trace']);
const specPath = path.resolve(process.argv[2] || path.join(root, 'docs/swagger.yml'));

async function main() {
  const source = fs.readFileSync(specPath, 'utf8');
  const spec = yaml.load(source, { schema: yaml.JSON_SCHEMA });
  if (!/^3\.[1-9]\./.test(spec.openapi)) throw new Error('OpenAPI 3.1+ is required');
  // The checker is deliberately self-contained: never fetch remote references/examples.
  function visit(value) {
    if (!value || typeof value !== 'object') return;
    if (value.$ref && !value.$ref.startsWith('#/')) throw new Error(`External reference: ${value.$ref}`);
    if (value.externalValue) throw new Error('External examples are not supported');
    Object.values(value).forEach(visit);
  }
  visit(spec);
  function resolve(value) {
    const seen = new Set();
    while (value?.$ref) {
      const ref = value.$ref;
      if (seen.has(ref)) throw new Error(`Reference cycle: ${ref}`);
      seen.add(ref);
      value = ref.slice(2).split('/').reduce((node, key) => node?.[key.replace(/~1/g, '/').replace(/~0/g, '~')], spec);
      if (!value) throw new Error(`Unresolved reference: ${ref}`);
    }
    return value;
  }
  const config = await createConfig({
    extends: ['recommended'],
    rules: {
      // This private project has no published license declaration.
      'info-license': 'off',
      // Only a local development server is advertised.
      'no-server-example.com': 'off',
      'no-unused-components': 'off',
      'no-invalid-media-type-examples': { severity: 'error', allowAdditionalProperties: true },
      'no-invalid-parameter-examples': 'error',
      'no-invalid-schema-examples': 'error',
      'no-required-schema-properties-undefined': 'error',
      'no-schema-type-mismatch': 'error',
      'operation-operationId': 'error',
      'operation-4xx-response': 'error',
    },
  });
  // Redocly's example rules use its AJV JSON Schema 2020-12 validator (OpenAPI 3.1).
  const problems = await lintFromString({ source, absoluteRef: specPath, config });
  for (const problem of problems) {
    console.error(`${problem.severity}: ${problem.ruleId}: ${problem.message}`);
  }
  if (problems.length) throw new Error(`${problems.length} OpenAPI lint problem(s)`);
  const ajv = new Ajv2020({ strict: false, allErrors: true, validateFormats: true });
  addFormats(ajv, { mode: 'full' });
  // OAS annotations; value types are constrained separately by the schemas.
  for (const format of ['int32', 'int64', 'float', 'double', 'password', 'binary', 'byte']) ajv.addFormat(format, true);
  const validators = new Map();
  function validator(schema) {
    const key = JSON.stringify(schema);
    if (!validators.has(key)) validators.set(key, ajv.compile({ ...schema, components: { schemas: spec.components.schemas } }));
    return validators.get(key);
  }
  for (const name of Object.keys(spec.components.schemas)) validator({ $ref: `#/components/schemas/${name}` });
  let operations = 0;
  let examples = 0;
  function checkContent(content, label) {
    for (const [mime, media] of Object.entries(content || {})) {
      if (!media.schema) throw new Error(`Missing schema: ${label} ${mime}`);
      const values = 'example' in media ? [media.example] : Object.values(media.examples || {}).map(example => {
        const resolved = resolve(example);
        if (!Object.hasOwn(resolved, 'value')) throw new Error(`Missing example value: ${label}`);
        return resolved.value;
      });
      if (!values.length) throw new Error(`Missing example: ${label} ${mime}`);
      const validate = validator(media.schema);
      for (const value of values) {
        if (!validate(value)) throw new Error(`Invalid example: ${label} ${mime}: ${ajv.errorsText(validate.errors)}`);
      }
      examples += values.length;
    }
  }
  for (const [url, item] of Object.entries(spec.paths)) {
    if (!/^\/api\/v[1-9][0-9]*\//.test(url)) throw new Error(`Unversioned path: ${url}`);
    for (const [method, operation] of Object.entries(item)) {
      if (!methods.has(method)) continue;
      operations++;
      const label = `${method.toUpperCase()} ${url}`;
      checkContent(resolve(operation.requestBody)?.content, `${label} request`);
      for (const [status, value] of Object.entries(operation.responses)) {
        const response = resolve(value);
        if (status === '204' && response.content) throw new Error(`204 must have no body: ${label}`);
        checkContent(response.content, `${label} response ${status}`);
      }
    }
  }
  console.log(`OpenAPI lint and schema/example checks passed: ${operations} operations, ${examples} request/response examples.`);
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
