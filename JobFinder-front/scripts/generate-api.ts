import { execSync } from 'node:child_process';
import { resolve } from 'node:path';

const root = resolve(new URL('.', import.meta.url).pathname, '..');
const input = resolve(root, '../docs/swagger.yml');
const output = resolve(root, 'src/shared/api/generated/openapi.ts');

execSync(`npx openapi-typescript "${input}" -o "${output}"`, { stdio: 'inherit' });
console.log(`Generated: ${output}`);

