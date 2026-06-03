const STUDENT_SECTIONS = [
  {
    key: 'learn',
    title: '学习',
    items: [
      {
        key: 'dashboard',
        label: '学习工作台',
        to: '/dashboard',
        icon: '⌂',
        match: ['/dashboard']
      },
      {
        key: 'subjects',
        label: '我的学科',
        to: '/my-subjects',
        icon: '□',
        match: ['/my-subjects', '/subjects']
      },
      {
        key: 'practice',
        label: '专项练习',
        to: '/practice',
        icon: '✎',
        match: ['/practice']
      },
      {
        key: 'wrong',
        label: '错题巩固',
        to: '/wrong-questions',
        icon: '!',
        match: ['/wrong-questions']
      }
    ]
  },
  {
    key: 'review',
    title: '复盘',
    items: [
      {
        key: 'practice-records',
        label: '练习记录',
        to: '/practice-records',
        icon: '◷',
        match: ['/practice-records']
      },
      {
        key: 'exam',
        label: '模拟考试',
        to: '/exercise/exam',
        icon: '◇',
        match: ['/exercise/exam']
      },
      {
        key: 'exam-records',
        label: '考试记录',
        to: '/exercise/exam/records',
        icon: '≡',
        match: ['/exercise/exam/records']
      },
      {
        key: 'analysis',
        label: '学情分析',
        to: '/learning-analysis',
        icon: '∑',
        match: ['/learning-analysis', '/dashboard/screen']
      }
    ]
  }
]

const ADMIN_SECTION = {
  key: 'admin',
  title: '管理',
  items: [
    {
      key: 'admin-home',
      label: '管理总览',
      to: '/admin',
      icon: '⌘',
      match: ['/admin']
    },
    {
      key: 'admin-subjects',
      label: '学科管理',
      to: '/admin/subjects',
      icon: 'S',
      match: ['/admin/subjects']
    },
    {
      key: 'admin-directories',
      label: '目录管理',
      to: '/admin/directories',
      icon: 'T',
      match: ['/admin/directories']
    },
    {
      key: 'admin-tags',
      label: '考点标签',
      to: '/admin/tags',
      icon: '#',
      match: ['/admin/tags']
    },
    {
      key: 'admin-directory-tags',
      label: '目录-考点',
      to: '/admin/directory-tags',
      icon: '↔',
      match: ['/admin/directory-tags']
    },
    {
      key: 'admin-questions',
      label: '题库管理',
      to: '/admin/questions',
      icon: 'Q',
      match: ['/admin/questions']
    },
    {
      key: 'admin-exams',
      label: '试卷管理',
      to: '/admin/exams',
      icon: 'E',
      match: ['/admin/exams']
    }
  ]
}

const cloneSections = (sections) => {
  return sections.map((section) => ({
    ...section,
    items: section.items.map((item) => ({ ...item, match: [...(item.match || [])] }))
  }))
}

const getNavigationSections = (isAdmin = false) => {
  const sections = cloneSections(STUDENT_SECTIONS)
  if (isAdmin) {
    sections.push(...cloneSections([ADMIN_SECTION]))
  }
  return sections
}

const isNavigationItemActive = (currentPath, item) => {
  const normalizedPath = String(currentPath || '').split('?')[0]
  const itemPath = String(item?.to || '')
  const matches = item?.match?.length ? item.match : [itemPath]

  return matches.some((matchPath) => {
    if (!matchPath) {
      return false
    }
    if (normalizedPath === matchPath) {
      return true
    }
    if (matchPath === '/practice') {
      return normalizedPath === '/practice'
    }
    if (matchPath === '/exercise/exam') {
      return normalizedPath === '/exercise/exam'
    }
    return normalizedPath.startsWith(`${matchPath}/`)
  })
}

module.exports = {
  getNavigationSections,
  isNavigationItemActive
}
