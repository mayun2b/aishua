const normalizeImageObjectName = (value) => {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const normalizeImageObjectNames = (values) => {
  const result = []
  const seen = new Set()

  for (const value of values || []) {
    const objectName = normalizeImageObjectName(value)
    if (!objectName || seen.has(objectName)) {
      continue
    }
    seen.add(objectName)
    result.push(objectName)
  }

  return result
}

const parseImageObjectNames = (imageUrls) => {
  if (imageUrls == null) {
    return []
  }
  return normalizeImageObjectNames(String(imageUrls).split(','))
}

const appendImageObjectNames = (currentNames, nextNames) => {
  return normalizeImageObjectNames([...(currentNames || []), ...(nextNames || [])])
}

const buildImageObjectNamesText = (objectNames) => {
  const normalized = normalizeImageObjectNames(objectNames)
  return normalized.length ? normalized.join(',') : null
}

const resolveUploadObjectName = (response) => {
  return normalizeImageObjectName(response?.objectName ?? response?.data?.objectName)
}

module.exports = {
  appendImageObjectNames,
  buildImageObjectNamesText,
  parseImageObjectNames,
  resolveUploadObjectName
}
