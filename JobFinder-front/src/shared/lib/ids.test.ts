import { describe, expect, it } from 'vitest';
import { parseIdList } from './ids';

describe('parseIdList', () => {
  it('keeps only positive integer ids', () => {
    expect(parseIdList('1, 2,0,-5,abc,3')).toEqual([1, 2, 3]);
  });
});

