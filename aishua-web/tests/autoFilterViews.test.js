const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const readView = (relativePath) => fs.readFileSync(
  path.join(__dirname, '../src/modules', relativePath),
  'utf8'
)

const assertNoManualFilterButton = (source, patterns, viewName) => {
  for (const pattern of patterns) {
    assert.doesNotMatch(source, pattern, `${viewName} should auto-apply filters instead of using ${pattern}`)
  }
}

const serviceFilterViews = [
  ['admin/views/AdminSubjectManagementView.vue', [/>\s*查询\s*<\/button>/]],
  ['admin/views/AdminTagManagementView.vue', [/>\s*查询\s*<\/button>/]],
  ['admin/views/AdminQuestionManagementView.vue', [/>\s*查询\s*<\/button>/, /@keyup\.enter="loadQuestions/]],
  ['admin/views/AdminExamManagementView.vue', [/>\s*查询\s*<\/button>/]],
  ['practice/views/PracticeRecordsView.vue', [/>\s*查询\s*<\/button>/]],
  ['practice/views/WrongQuestionsView.vue', [/>\s*查询\s*<\/button>/]],
  ['exam/views/ExamRecordListView.vue', [/>\s*刷新\s*<\/button>/]]
]

for (const [relativePath, manualPatterns] of serviceFilterViews) {
  const source = readView(relativePath)
  assertNoManualFilterButton(source, manualPatterns, relativePath)
  assert.match(source, /watch\(/, `${relativePath} should watch filter changes`)
}

const examCenter = readView('exam/views/ExamCenterView.vue')
assertNoManualFilterButton(examCenter, [/>\s*刷新试卷\s*<\/button>/], 'exam/views/ExamCenterView.vue')

const examQuestionConfig = readView('admin/views/AdminExamQuestionConfigView.vue')
assertNoManualFilterButton(
  examQuestionConfig,
  [/按知识点\/考点选题/, /@keyup\.enter="searchQuestionBank"/],
  'admin/views/AdminExamQuestionConfigView.vue'
)
assert.match(
  examQuestionConfig,
  /watch\(\s*\(\)\s*=>\s*questionBankFilters\.keyword[\s\S]*loadQuestionBank\(\{ resetPage: true/,
  'AdminExamQuestionConfigView should auto-search when keyword changes'
)

console.log('autoFilterViews tests passed')
