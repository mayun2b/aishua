const assert = require('node:assert/strict')

const {
  getNavigationSections,
  isNavigationItemActive
} = require('../src/router/navigation')

const studentSections = getNavigationSections(false)
assert.ok(studentSections.some((section) => section.key === 'learn'))
assert.ok(studentSections.flatMap((section) => section.items).some((item) => item.to === '/dashboard'))
assert.equal(studentSections.some((section) => section.key === 'admin'), false)

const adminSections = getNavigationSections(true)
assert.ok(adminSections.some((section) => section.key === 'admin'))
assert.equal(adminSections.some((section) => section.key === 'learn'), false)
assert.equal(adminSections.some((section) => section.key === 'review'), false)
assert.ok(adminSections.flatMap((section) => section.items).some((item) => item.to === '/admin/questions'))
assert.ok(adminSections.flatMap((section) => section.items).some((item) => item.to === '/admin/directory-tags'))

assert.equal(isNavigationItemActive('/practice-records/12', { to: '/practice-records', match: ['/practice-records'] }), true)
assert.equal(isNavigationItemActive('/practice/session/12', { to: '/practice', match: ['/practice'] }), false)
assert.equal(isNavigationItemActive('/admin/exams/8/questions', { to: '/admin/exams', match: ['/admin/exams'] }), true)
assert.equal(isNavigationItemActive('/admin/directory-tags', { to: '/admin/directory-tags', match: ['/admin/directory-tags'] }), true)
assert.equal(isNavigationItemActive('/admin/directories', { to: '/admin', match: ['/admin'] }), false)
assert.equal(isNavigationItemActive('/dashboard/screen', { to: '/dashboard', match: ['/dashboard'] }), false)

console.log('appNavigation tests passed')
