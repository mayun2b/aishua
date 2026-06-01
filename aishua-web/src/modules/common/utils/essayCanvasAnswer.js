const CANVAS_MARKER_PREFIX = '[[canvas:'
const CANVAS_MARKER_SUFFIX = ']]'

const normalizeString = (value) => (value == null ? '' : String(value).trim())

export const parseEssayAnswerPayload = (value) => {
  const normalized = normalizeString(value)
  if (!normalized) {
    return {
      text: '',
      canvasObjectName: ''
    }
  }

  const markerStart = normalized.lastIndexOf(CANVAS_MARKER_PREFIX)
  const markerEnd = normalized.lastIndexOf(CANVAS_MARKER_SUFFIX)
  if (markerStart < 0 || markerEnd < markerStart) {
    return {
      text: normalized,
      canvasObjectName: ''
    }
  }

  const markerContent = normalized
    .slice(markerStart + CANVAS_MARKER_PREFIX.length, markerEnd)
    .trim()

  const text = normalized.slice(0, markerStart).trim()
  return {
    text,
    canvasObjectName: markerContent
  }
}

export const buildEssayAnswerPayload = ({ text, canvasObjectName }) => {
  const safeText = normalizeString(text)
  const safeCanvasObjectName = normalizeString(canvasObjectName)
  if (!safeCanvasObjectName) {
    return safeText
  }
  const marker = `${CANVAS_MARKER_PREFIX}${safeCanvasObjectName}${CANVAS_MARKER_SUFFIX}`
  return safeText ? `${safeText}\n${marker}` : marker
}

export const stripEssayCanvasMarker = (value) => parseEssayAnswerPayload(value).text

export const hasEssayCanvasMarker = (value) => {
  const { canvasObjectName } = parseEssayAnswerPayload(value)
  return canvasObjectName.length > 0
}

export const essayCanvasMarkerPrefix = CANVAS_MARKER_PREFIX
export const essayCanvasMarkerSuffix = CANVAS_MARKER_SUFFIX
