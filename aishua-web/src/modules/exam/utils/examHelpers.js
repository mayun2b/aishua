import { hasEssayCanvasMarker, stripEssayCanvasMarker } from '../../common/utils/essayCanvasAnswer'
import { stripImageAnnotationMarkers } from '../../common/utils/imageAnnotationAnswer'

export { parseQuestionOptions } from '../../common/utils/questionOptions'
const BOOLEAN_TEXT_REGEX =
  /^(true|false|yes|no|y|n|1|0|dui|cuo|\u5bf9|\u9519|\u6b63\u786e|\u9519\u8bef|\u662f|\u5426)$/i

const QUESTION_TYPE_MAP = {
  1: '\u5355\u9009\u9898',
  2: '\u591a\u9009\u9898',
  3: '\u5224\u65ad\u9898',
  4: '\u586b\u7a7a\u9898',
  5: '\u7b80\u7b54\u9898'
}

const DIFFICULTY_MAP = {
  1: '\u7b80\u5355',
  2: '\u4e2d\u7b49',
  3: '\u56f0\u96be'
}

export function resolveTypeLabel(type) {
  return QUESTION_TYPE_MAP[type] || (type ? `\u9898\u578b ${type}` : '-')
}

export function resolveDifficultyLabel(difficulty) {
  return DIFFICULTY_MAP[difficulty] || (difficulty ? `\u96be\u5ea6 ${difficulty}` : '-')
}

export function resolveStatusLabel(status) {
  return Number(status) === 2 ? '\u5df2\u5b8c\u6210' : '\u8fdb\u884c\u4e2d'
}

export function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value).replace('T', ' ')
  }

  return date.toLocaleString('zh-CN', { hour12: false })
}

export function formatScore(value) {
  const score = Number(value)
  if (Number.isNaN(score)) {
    return '-'
  }

  if (Number.isInteger(score)) {
    return `${score}`
  }

  return score.toFixed(2).replace(/\.?0+$/, '')
}

export function formatDurationMinutes(value) {
  const duration = Number(value || 0)
  if (!duration || duration < 0) {
    return '0 \u5206\u949f'
  }
  return `${duration} \u5206\u949f`
}

export function toAnswerTokens(answer) {
  if (answer == null) {
    return []
  }

  if (Array.isArray(answer)) {
    return answer
      .map((item) => String(item).trim().toUpperCase())
      .filter(Boolean)
  }

  const text = stripImageAnnotationMarkers(answer).trim()
  if (!text) {
    return []
  }

  if (text.startsWith('[') && text.endsWith(']')) {
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        return parsed
          .map((item) => String(item).trim().toUpperCase())
          .filter(Boolean)
      }
    } catch (error) {
      // ignore malformed json-like text
    }
  }

  return text
    .replaceAll('\uFF0C', ',')
    .replaceAll(';', ',')
    .replaceAll('|', ',')
    .split(/[,\s]+/)
    .map((item) => item.trim().toUpperCase())
    .filter(Boolean)
}

export function formatAnswerDisplay(answer) {
  const answerWithoutImageAnnotations = stripImageAnnotationMarkers(answer)
  const hasCanvasDraft = hasEssayCanvasMarker(answerWithoutImageAnnotations)
  const plainAnswer = stripEssayCanvasMarker(answerWithoutImageAnnotations)
  const tokens = toAnswerTokens(plainAnswer)
  if (!tokens.length) {
    const raw = String(plainAnswer ?? '').trim()
    if (raw) {
      return raw
    }
    return hasCanvasDraft ? '已提交手写作答' : '\u672a\u4f5c\u7b54'
  }
  return tokens.join(', ')
}

export function isLikelyBooleanText(value) {
  const text = String(value ?? '').trim()
  if (!text) {
    return false
  }
  return BOOLEAN_TEXT_REGEX.test(text)
}

export function buildSessionCacheKey(recordId) {
  return `exam:session:${recordId}`
}

export function buildSessionStateKey(recordId) {
  return `exam:session-state:${recordId}`
}
