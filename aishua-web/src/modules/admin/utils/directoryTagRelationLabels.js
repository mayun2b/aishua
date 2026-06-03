const RELATION_TYPE_LABELS = {
  1: '直接关联',
  2: '前置关联',
  3: '拓展关联'
}

const IMPORTANCE_LEVEL_LABELS = {
  1: '低',
  2: '中',
  3: '高',
  4: '很高',
  5: '极高'
}

const resolveEnumLabel = (labels, value) => {
  const normalized = Number(value)
  return labels[normalized] || '-'
}

const resolveRelationTypeLabel = (value) => resolveEnumLabel(RELATION_TYPE_LABELS, value)

const resolveImportanceLevelLabel = (value) => resolveEnumLabel(IMPORTANCE_LEVEL_LABELS, value)

module.exports = {
  resolveImportanceLevelLabel,
  resolveRelationTypeLabel
}
