const assert = require('assert')

const {
  appendImageObjectNames,
  buildImageObjectNamesText,
  parseImageObjectNames,
  resolveUploadObjectName
} = require('../src/modules/admin/utils/questionImageObjects')

assert.deepStrictEqual(
  parseImageObjectNames(' 2026-06-02/a.png, ,2026-06-02/b.png,2026-06-02/a.png '),
  ['2026-06-02/a.png', '2026-06-02/b.png']
)

assert.deepStrictEqual(
  appendImageObjectNames(['old/a.png'], ['new/b.png', 'old/a.png', '  ']),
  ['old/a.png', 'new/b.png']
)

assert.strictEqual(
  buildImageObjectNamesText([' old/a.png ', '', 'new/b.png', 'old/a.png']),
  'old/a.png,new/b.png'
)

assert.strictEqual(buildImageObjectNamesText([]), null)
assert.strictEqual(resolveUploadObjectName({ objectName: '2026-06-02/file.png' }), '2026-06-02/file.png')
assert.strictEqual(resolveUploadObjectName({ data: { objectName: 'wrapped/file.png' } }), 'wrapped/file.png')
assert.strictEqual(resolveUploadObjectName({ message: 'ok' }), '')

console.log('questionImageObjects tests passed')
