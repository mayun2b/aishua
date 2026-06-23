const OPTION_LABEL_REGEX = /^\s*([A-Za-z])(?:[.\u3001)\]:：]\s*|\s+)(.+)$/
const STRUCTURED_OPTION_KEYS = new Set([
  'optionKey',
  'key',
  'label',
  'code',
  'optionValue',
  'value',
  'optionText',
  'text',
  'content',
  'title',
  'desc',
  'description',
  'name'
])

function safeJsonParse(value) {
  if (typeof value !== 'string') {
    return value
  }

  const text = value.trim()
  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch (error) {
    return value
  }
}

export function normalizeOptionField(value) {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

export function normalizeOptionKey(value, index) {
  const fallback = String.fromCharCode(65 + index)
  const text = normalizeOptionField(value)
  if (!text) {
    return fallback
  }
  return text.toUpperCase()
}

function createOption(optionKey, optionText) {
  return {
    optionKey,
    optionLabel: optionKey,
    optionValue: optionKey,
    optionText
  }
}

function resolveStructuredOptionText(value) {
  const normalized = normalizeOptionField(
    value?.optionText
    ?? value?.text
    ?? value?.content
    ?? value?.title
    ?? value?.desc
    ?? value?.description
    ?? value?.name
    ?? value?.value
  )
  if (normalized) {
    return normalized
  }
  return normalizeOptionField(value)
}

function isStructuredOptionObject(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return false
  }
  return Object.keys(value).some((key) => STRUCTURED_OPTION_KEYS.has(key))
}

function parseStringOption(value, index) {
  const text = normalizeOptionField(value)
  if (!text) {
    return createOption(normalizeOptionKey('', index), '')
  }

  const matched = text.match(OPTION_LABEL_REGEX)
  if (matched) {
    const optionKey = normalizeOptionKey(matched[1], index)
    return createOption(optionKey, matched[2].trim())
  }

  return createOption(normalizeOptionKey('', index), text)
}

function parseStructuredOption(value, index) {
  const optionKey = normalizeOptionKey(
    value?.optionKey ?? value?.key ?? value?.label ?? value?.code,
    index
  )
  return createOption(optionKey, resolveStructuredOptionText(value))
}

function parseLegacyOptionMap(value, startIndex = 0) {
  return Object.entries(value || {}).map(([key, rawValue], offset) => {
    const index = startIndex + offset
    const optionKey = normalizeOptionKey(key, index)
    const optionText = rawValue != null && typeof rawValue === 'object' && !Array.isArray(rawValue)
      ? resolveStructuredOptionText(rawValue)
      : normalizeOptionField(rawValue)
    return createOption(optionKey, optionText)
  })
}

export function parseQuestionOptions(rawOptions) {
  const parsed = safeJsonParse(rawOptions)
  if (!parsed) {
    return []
  }

  if (Array.isArray(parsed)) {
    const result = []
    parsed.forEach((item) => {
      if (item != null && typeof item === 'object' && !Array.isArray(item)) {
        if (isStructuredOptionObject(item)) {
          result.push(parseStructuredOption(item, result.length))
        } else {
          result.push(...parseLegacyOptionMap(item, result.length))
        }
        return
      }
      result.push(parseStringOption(item, result.length))
    })
    return result.filter((item) => item.optionText || item.optionKey)
  }

  if (typeof parsed === 'object') {
    const result = isStructuredOptionObject(parsed)
      ? [parseStructuredOption(parsed, 0)]
      : parseLegacyOptionMap(parsed, 0)
    return result.filter((item) => item.optionText || item.optionKey)
  }

  if (typeof parsed === 'string') {
    return [parseStringOption(parsed, 0)].filter((item) => item.optionText || item.optionKey)
  }

  return []
}

function normalizeChoiceAnswerToken(rawValue, options) {
  const text = normalizeOptionField(rawValue)
  if (!text) {
    return ''
  }

  const normalizedUpper = text.toUpperCase()
  const directMatch = options.find(
    (item) => normalizeOptionField(item.optionKey).toUpperCase() === normalizedUpper
  )
  if (directMatch) {
    return directMatch.optionKey
  }

  const matched = text.match(OPTION_LABEL_REGEX)
  if (matched) {
    const optionKey = normalizeOptionKey(matched[1], 0)
    if (options.some((item) => item.optionKey === optionKey)) {
      return optionKey
    }
  }

  const textMatch = options.find(
    (item) => normalizeOptionField(item.optionText).toUpperCase() === normalizedUpper
  )
  if (textMatch) {
    return textMatch.optionKey
  }

  return text
}

function splitChoiceAnswerCandidates(value) {
  if (Array.isArray(value)) {
    return value
  }

  const text = normalizeOptionField(value)
  if (!text) {
    return []
  }

  if (text.startsWith('[') && text.endsWith(']')) {
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch (error) {
      // ignore malformed json-like answers and fall back to text parsing
    }
  }

  const normalized = text
    .replaceAll('\uFF0C', ',')
    .replaceAll(';', ',')
    .replaceAll('|', ',')
  if (normalized.includes(',')) {
    return normalized.split(',').map((item) => item.trim()).filter(Boolean)
  }
  return [normalized]
}

export function normalizeChoiceAnswer(value, rawOptions, { multiple = false } = {}) {
  const options = Array.isArray(rawOptions) ? rawOptions : parseQuestionOptions(rawOptions)
  const normalizedTokens = splitChoiceAnswerCandidates(value)
    .map((item) => normalizeChoiceAnswerToken(item, options))
    .filter(Boolean)

  if (multiple) {
    return [...new Set(normalizedTokens)].sort()
  }
  return normalizedTokens[0] || ''
}
