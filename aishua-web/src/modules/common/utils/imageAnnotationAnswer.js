const IMAGE_ANNOTATION_MARKER_PREFIX = '[[image-annotations:'
const IMAGE_ANNOTATION_MARKER_SUFFIX = ']]'

const normalizeText = (value) => (value == null ? '' : String(value))

const isWhitespace = (value) => /\s/.test(value)

const skipWhitespace = (value, startIndex) => {
  let index = startIndex
  while (index < value.length && isWhitespace(value[index])) {
    index += 1
  }
  return index
}

const findFallbackMarkerSuffixIndex = (value, startIndex) => {
  let fromIndex = startIndex
  while (fromIndex < value.length) {
    const suffixIndex = value.indexOf(IMAGE_ANNOTATION_MARKER_SUFFIX, fromIndex)
    if (suffixIndex < 0) {
      return -1
    }
    const suffixEndIndex = suffixIndex + IMAGE_ANNOTATION_MARKER_SUFFIX.length
    if (suffixEndIndex >= value.length || isWhitespace(value[suffixEndIndex])) {
      return suffixIndex
    }
    fromIndex = suffixIndex + 1
  }
  return -1
}

const findJsonArrayEnd = (value, startIndex) => {
  let index = skipWhitespace(value, startIndex)
  if (value[index] !== '[') {
    return -1
  }

  let depth = 0
  let inString = false
  let isEscaped = false

  for (; index < value.length; index += 1) {
    const char = value[index]
    if (inString) {
      if (isEscaped) {
        isEscaped = false
        continue
      }
      if (char === '\\') {
        isEscaped = true
        continue
      }
      if (char === '"') {
        inString = false
      }
      continue
    }

    if (char === '"') {
      inString = true
      continue
    }
    if (char === '[' || char === '{') {
      depth += 1
      continue
    }
    if (char === ']' || char === '}') {
      depth -= 1
      if (depth === 0) {
        return index + 1
      }
      if (depth < 0) {
        return -1
      }
    }
  }

  return -1
}

const findImageAnnotationMarkerEnd = (value, jsonStartIndex) => {
  const jsonEndIndex = findJsonArrayEnd(value, jsonStartIndex)
  if (jsonEndIndex >= 0) {
    const suffixStartIndex = skipWhitespace(value, jsonEndIndex)
    if (value.startsWith(IMAGE_ANNOTATION_MARKER_SUFFIX, suffixStartIndex)) {
      return {
        endIndex: suffixStartIndex + IMAGE_ANNOTATION_MARKER_SUFFIX.length,
        jsonText: value.slice(jsonStartIndex, jsonEndIndex)
      }
    }
  }

  const fallbackSuffixIndex = findFallbackMarkerSuffixIndex(value, jsonStartIndex)
  if (fallbackSuffixIndex < 0) {
    return null
  }
  return {
    endIndex: fallbackSuffixIndex + IMAGE_ANNOTATION_MARKER_SUFFIX.length,
    jsonText: value.slice(jsonStartIndex, fallbackSuffixIndex)
  }
}

const findImageAnnotationMarkers = (value) => {
  const markers = []
  const lowerValue = value.toLowerCase()
  const lowerPrefix = IMAGE_ANNOTATION_MARKER_PREFIX.toLowerCase()
  let fromIndex = 0

  while (fromIndex < value.length) {
    const startIndex = lowerValue.indexOf(lowerPrefix, fromIndex)
    if (startIndex < 0) {
      break
    }

    const jsonStartIndex = startIndex + IMAGE_ANNOTATION_MARKER_PREFIX.length
    const markerEnd = findImageAnnotationMarkerEnd(value, jsonStartIndex)
    if (!markerEnd) {
      fromIndex = jsonStartIndex
      continue
    }

    markers.push({
      startIndex,
      endIndex: markerEnd.endIndex,
      jsonText: markerEnd.jsonText
    })
    fromIndex = markerEnd.endIndex
  }

  return markers
}

const stripMarkers = (value, markers) => {
  if (!markers.length) {
    return value
  }

  let result = ''
  let fromIndex = 0
  for (const marker of markers) {
    result += value.slice(fromIndex, marker.startIndex)
    fromIndex = marker.endIndex
    if (
      result.length &&
      fromIndex < value.length &&
      isWhitespace(result[result.length - 1]) &&
      isWhitespace(value[fromIndex])
    ) {
      fromIndex = skipWhitespace(value, fromIndex)
    }
  }
  return (result + value.slice(fromIndex)).trim()
}

const normalizeAnnotation = (item) => {
  const imageObjectName = String(item?.imageObjectName || '').trim()
  const annotationObjectName = String(item?.annotationObjectName || '').trim()
  if (!imageObjectName || !annotationObjectName) {
    return null
  }
  return { imageObjectName, annotationObjectName }
}

const normalizeAnnotations = (annotations) => {
  if (!Array.isArray(annotations)) {
    return []
  }
  const seen = new Set()
  const result = []
  for (const item of annotations) {
    const normalized = normalizeAnnotation(item)
    if (!normalized) {
      continue
    }
    const key = `${normalized.imageObjectName}\n${normalized.annotationObjectName}`
    if (seen.has(key)) {
      continue
    }
    seen.add(key)
    result.push(normalized)
  }
  return result
}

const buildImageAnnotationMarker = (annotations) => {
  const normalized = normalizeAnnotations(annotations)
  if (!normalized.length) {
    return ''
  }
  return `${IMAGE_ANNOTATION_MARKER_PREFIX}${JSON.stringify(normalized)}${IMAGE_ANNOTATION_MARKER_SUFFIX}`
}

const stripImageAnnotationMarkers = (value) => {
  const raw = normalizeText(value)
  return stripMarkers(raw, findImageAnnotationMarkers(raw))
}

const parseImageAnnotationPayload = (value) => {
  const raw = normalizeText(value)
  const markers = findImageAnnotationMarkers(raw)
  const annotationItems = []
  for (const marker of markers) {
    try {
      const parsed = JSON.parse(marker.jsonText)
      if (Array.isArray(parsed)) {
        annotationItems.push(...parsed)
      }
    } catch (error) {
      continue
    }
  }
  return {
    text: stripMarkers(raw, markers),
    annotations: normalizeAnnotations(annotationItems)
  }
}

const buildImageAnnotationPayload = (text, annotations) => {
  const rawText = normalizeText(text)
  const marker = buildImageAnnotationMarker(annotations)
  if (!marker) {
    return rawText
  }
  const normalizedText = rawText.trim()
  return normalizedText ? `${normalizedText}\n${marker}` : marker
}

module.exports = {
  IMAGE_ANNOTATION_MARKER_PREFIX,
  IMAGE_ANNOTATION_MARKER_SUFFIX,
  buildImageAnnotationMarker,
  buildImageAnnotationPayload,
  parseImageAnnotationPayload,
  stripImageAnnotationMarkers
}
