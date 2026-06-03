const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const source = fs.readFileSync(
  path.join(__dirname, '../src/modules/admin/views/AdminDirectoryTagRelationView.vue'),
  'utf8'
)

assert.match(
  source,
  /resolveImportanceLevelLabel\(relation\.importanceLevel\)/,
  'relation list should render importanceLevel through enum label conversion'
)

assert.doesNotMatch(
  source,
  /@click="loadRelations\(\{ resetPage: true \}\)"/,
  'filter card should not require a manual query button to apply filters'
)

assert.match(
  source,
  /watch\(\s*\(\)\s*=>\s*filters\.subjectId[\s\S]*scheduleFilterReload\(\)/,
  'subject filter changes should schedule relation reload'
)

assert.match(
  source,
  /watch\(\s*\(\)\s*=>\s*\[filters\.directoryId,\s*filters\.isEnabled\][\s\S]*scheduleFilterReload\(\)/,
  'directory and status filter changes should schedule relation reload'
)

assert.match(
  source,
  /watch\(\s*\(\)\s*=>\s*filters\.keyword[\s\S]*scheduleFilterReload\(\{ delay: 300 \}\)/,
  'keyword filter changes should debounce relation reload'
)

console.log('adminDirectoryTagRelationView tests passed')
