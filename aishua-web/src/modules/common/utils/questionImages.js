const normalizeQuestionImageValue = (value) => {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const isDirectImageUrl = (value) => {
  return /^(https?:\/\/|data:|blob:|\/)/i.test(value)
}

const parseQuestionImageRefs = (imageUrls) => {
  if (imageUrls == null) {
    return []
  }

  const result = []
  const seen = new Set()
  const values = String(imageUrls).split(',')

  for (const value of values) {
    const normalized = normalizeQuestionImageValue(value)
    if (!normalized || seen.has(normalized)) {
      continue
    }

    seen.add(normalized)
    result.push({
      key: normalized,
      objectName: isDirectImageUrl(normalized) ? '' : normalized,
      url: isDirectImageUrl(normalized) ? normalized : ''
    })
  }

  return result
}

const resolveQuestionImagePreviewUrl = (response) => {
  return normalizeQuestionImageValue(response?.url ?? response?.data?.url)
}

module.exports = {
  parseQuestionImageRefs,
  resolveQuestionImagePreviewUrl
}
