const assert = require('node:assert/strict')

const {
  resolveImportanceLevelLabel,
  resolveRelationTypeLabel
} = require('../src/modules/admin/utils/directoryTagRelationLabels')

assert.equal(resolveImportanceLevelLabel(1), '低')
assert.equal(resolveImportanceLevelLabel('2'), '中')
assert.equal(resolveImportanceLevelLabel(3), '高')
assert.equal(resolveImportanceLevelLabel(4), '很高')
assert.equal(resolveImportanceLevelLabel(5), '极高')
assert.equal(resolveImportanceLevelLabel(null), '-')
assert.equal(resolveImportanceLevelLabel(99), '-')

assert.equal(resolveRelationTypeLabel(1), '直接关联')
assert.equal(resolveRelationTypeLabel('2'), '前置关联')
assert.equal(resolveRelationTypeLabel(3), '拓展关联')
assert.equal(resolveRelationTypeLabel(null), '-')

console.log('directoryTagRelationLabels tests passed')
