export function parseIdList(value: string): number[] {
  return value
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((id) => Number.isInteger(id) && id > 0);
}

