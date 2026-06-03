const assert = require('node:assert/strict')

const {
  normalizePageResult
} = require('../src/modules/common/utils/pageResult')

assert.deepEqual(
  normalizePageResult({
    records: [{ id: 1 }, { id: 2 }],
    total: 22,
    pageNum: 2,
    pageSize: 2
  }),
  {
    records: [{ id: 1 }, { id: 2 }],
    total: 22,
    pageNum: 2,
    pageSize: 2
  }
)

assert.deepEqual(
  normalizePageResult({
    records: [{ id: 3 }],
    total: '9',
    page: '3',
    size: '1'
  }),
  {
    records: [{ id: 3 }],
    total: 9,
    pageNum: 3,
    pageSize: 1
  }
)

assert.deepEqual(
  normalizePageResult([{ id: 4 }]),
  {
    records: [{ id: 4 }],
    total: 1,
    pageNum: 1,
    pageSize: 1
  }
)

assert.deepEqual(
  normalizePageResult(null, { pageNum: 5, pageSize: 20 }),
  {
    records: [],
    total: 0,
    pageNum: 5,
    pageSize: 20
  }
)

console.log('pageResult tests passed')
