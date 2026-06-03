const assert = require('assert')

const {
  parseQuestionImageRefs,
  resolveQuestionImagePreviewUrl
} = require('../src/modules/common/utils/questionImages')

assert.deepStrictEqual(
  parseQuestionImageRefs(' 2026-06-02/a.png,https://cdn.example.com/b.png,,2026-06-02/a.png,/static/c.png '),
  [
    { key: '2026-06-02/a.png', objectName: '2026-06-02/a.png', url: '' },
    { key: 'https://cdn.example.com/b.png', objectName: '', url: 'https://cdn.example.com/b.png' },
    { key: '/static/c.png', objectName: '', url: '/static/c.png' }
  ]
)

assert.deepStrictEqual(parseQuestionImageRefs(null), [])
assert.deepStrictEqual(parseQuestionImageRefs(''), [])

const mixedRefs = parseQuestionImageRefs('questions/q043.png, https://example.com/a.png, questions/q043.png')
assert.strictEqual(mixedRefs.length, 2)
assert.strictEqual(mixedRefs[0].objectName, 'questions/q043.png')
assert.strictEqual(mixedRefs[1].url, 'https://example.com/a.png')

assert.strictEqual(resolveQuestionImagePreviewUrl({ url: 'https://minio.example.com/a.png' }), 'https://minio.example.com/a.png')
assert.strictEqual(resolveQuestionImagePreviewUrl({ data: { url: 'https://minio.example.com/b.png' } }), 'https://minio.example.com/b.png')
assert.strictEqual(resolveQuestionImagePreviewUrl({ message: 'ok' }), '')

console.log('questionImages tests passed')
