<template>
  <div class="answer-canvas">
    <canvas
      ref="canvasRef"
      class="canvas-panel"
      :class="{ disabled }"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerUp"
      @pointercancel="handlePointerUp"
      @pointerleave="handlePointerUp"
    ></canvas>

    <div class="canvas-tools">
      <button type="button" class="tool-button" :disabled="disabled || !canUndo" @click="undoLast">
        Undo
      </button>
      <button type="button" class="tool-button" :disabled="disabled || !hasInk" @click="clearCanvas">
        Clear
      </button>
    </div>
  </div>
</template>

<script setup>
/* global defineProps, defineEmits */
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: 260
  },
  lineWidth: {
    type: Number,
    default: 2
  },
  strokeColor: {
    type: String,
    default: '#17324d'
  }
})

const emit = defineEmits(['update:modelValue'])

const canvasRef = ref(null)
const isDrawing = ref(false)
const hasInk = ref(false)
const canUndo = ref(false)

let lastPoint = null
let history = []
let syncingFromModel = false
let resizeFrame = 0
let pendingResizeForce = false
let lastCanvasWidth = 0
let lastCanvasHeight = 0

const clearResizeFrame = () => {
  if (!resizeFrame) {
    return
  }
  window.cancelAnimationFrame(resizeFrame)
  resizeFrame = 0
  pendingResizeForce = false
}

const cssHeight = computed(() => Math.max(Number(props.height) || 260, 120))

const getContext = () => {
  const canvas = canvasRef.value
  if (!canvas) {
    return null
  }
  return canvas.getContext('2d')
}

const fillBackground = () => {
  const canvas = canvasRef.value
  const ctx = getContext()
  if (!canvas || !ctx) {
    return
  }
  ctx.save()
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvas.clientWidth, cssHeight.value)
  ctx.restore()
}

const configureContext = () => {
  const ctx = getContext()
  if (!ctx) {
    return
  }
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.lineWidth = Math.max(Number(props.lineWidth) || 2, 1)
  ctx.strokeStyle = props.strokeColor || '#17324d'
}

const loadDataUrlToCanvas = async (dataUrl) => {
  const canvas = canvasRef.value
  const ctx = getContext()
  if (!canvas || !ctx) {
    return
  }
  fillBackground()
  if (!dataUrl) {
    return
  }
  await new Promise((resolve) => {
    const image = new Image()
    image.onload = () => {
      ctx.drawImage(image, 0, 0, canvas.clientWidth, cssHeight.value)
      resolve()
    }
    image.onerror = () => resolve()
    image.src = dataUrl
  })
}

const buildSnapshot = () => {
  const canvas = canvasRef.value
  if (!canvas) {
    return ''
  }
  return canvas.toDataURL('image/png')
}

const emitSnapshot = () => {
  if (syncingFromModel) {
    return
  }
  emit('update:modelValue', hasInk.value ? buildSnapshot() : '')
}

const updateHistoryState = () => {
  canUndo.value = history.length > 0
  hasInk.value = history.length > 0
}

const resetHistoryBySnapshot = (snapshot) => {
  history = snapshot ? [snapshot] : []
  updateHistoryState()
}

const pushHistorySnapshot = () => {
  const snapshot = buildSnapshot()
  if (!snapshot) {
    return
  }
  if (history.length && history[history.length - 1] === snapshot) {
    updateHistoryState()
    return
  }
  history.push(snapshot)
  if (history.length > 30) {
    history = history.slice(history.length - 30)
  }
  updateHistoryState()
}

const resizeCanvas = async ({ force = false } = {}) => {
  const canvas = canvasRef.value
  if (!canvas) {
    return
  }

  const width = Math.max(canvas.parentElement?.clientWidth || canvas.clientWidth || 1, 1)
  const height = cssHeight.value
  const dpr = window.devicePixelRatio || 1
  const source = history.length ? history[history.length - 1] : props.modelValue
  const targetWidth = Math.floor(width * dpr)
  const targetHeight = Math.floor(height * dpr)

  if (!force && targetWidth === lastCanvasWidth && targetHeight === lastCanvasHeight) {
    return
  }

  lastCanvasWidth = targetWidth
  lastCanvasHeight = targetHeight

  canvas.width = targetWidth
  canvas.height = targetHeight
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  const ctx = getContext()
  if (!ctx) {
    return
  }
  ctx.setTransform(1, 0, 0, 1, 0, 0)
  ctx.scale(dpr, dpr)
  configureContext()
  await loadDataUrlToCanvas(source)
}

const scheduleResize = (force = false) => {
  pendingResizeForce = pendingResizeForce || force
  if (resizeFrame) {
    return
  }
  resizeFrame = window.requestAnimationFrame(() => {
    const forceNow = pendingResizeForce
    resizeFrame = 0
    pendingResizeForce = false
    void resizeCanvas({ force: forceNow })
  })
}

const handleWindowResize = () => {
  scheduleResize(false)
}

const resolvePointerPoint = (event) => {
  const canvas = canvasRef.value
  if (!canvas) {
    return null
  }
  const rect = canvas.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

const drawSegment = (from, to) => {
  const ctx = getContext()
  if (!ctx || !from || !to) {
    return
  }
  ctx.beginPath()
  ctx.moveTo(from.x, from.y)
  ctx.lineTo(to.x, to.y)
  ctx.stroke()
}

const drawDot = (point) => {
  const ctx = getContext()
  if (!ctx || !point) {
    return
  }
  const radius = Math.max((Number(props.lineWidth) || 2) / 2, 1)
  ctx.beginPath()
  ctx.arc(point.x, point.y, radius, 0, Math.PI * 2)
  ctx.fillStyle = props.strokeColor || '#17324d'
  ctx.fill()
}

const handlePointerDown = (event) => {
  if (props.disabled) {
    return
  }
  const point = resolvePointerPoint(event)
  if (!point) {
    return
  }
  event.preventDefault()
  isDrawing.value = true
  lastPoint = point
  drawDot(point)
}

const handlePointerMove = (event) => {
  if (!isDrawing.value || props.disabled) {
    return
  }
  const point = resolvePointerPoint(event)
  if (!point) {
    return
  }
  event.preventDefault()
  drawSegment(lastPoint, point)
  lastPoint = point
}

const handlePointerUp = (event) => {
  if (!isDrawing.value) {
    return
  }
  event?.preventDefault?.()
  isDrawing.value = false
  lastPoint = null
  pushHistorySnapshot()
  emitSnapshot()
}

const clearCanvas = async () => {
  if (props.disabled) {
    return
  }
  history = []
  updateHistoryState()
  await loadDataUrlToCanvas('')
  emitSnapshot()
}

const undoLast = async () => {
  if (props.disabled || !history.length) {
    return
  }
  history.pop()
  updateHistoryState()
  const previous = history.length ? history[history.length - 1] : ''
  await loadDataUrlToCanvas(previous)
  emitSnapshot()
}

const applyModelValue = async (value) => {
  syncingFromModel = true
  try {
    const normalized = value ? String(value) : ''
    await loadDataUrlToCanvas(normalized)
    resetHistoryBySnapshot(normalized)
  } finally {
    syncingFromModel = false
  }
}

watch(
  () => props.modelValue,
  async (nextValue) => {
    const currentSnapshot = history.length ? history[history.length - 1] : ''
    const normalized = nextValue ? String(nextValue) : ''
    if (normalized === currentSnapshot) {
      return
    }
    await applyModelValue(normalized)
  }
)

watch(
  () => [props.height, props.lineWidth, props.strokeColor],
  () => {
    scheduleResize(true)
  }
)

onMounted(async () => {
  await nextTick()
  await resizeCanvas({ force: true })
  await applyModelValue(props.modelValue)
  window.addEventListener('resize', handleWindowResize)
})

onBeforeUnmount(() => {
  clearResizeFrame()
  window.removeEventListener('resize', handleWindowResize)
})
</script>

<style scoped>
.answer-canvas {
  display: grid;
  gap: 10px;
}

.canvas-panel {
  width: 100%;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  background: #fff;
  touch-action: none;
  cursor: crosshair;
}

.canvas-panel.disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.canvas-tools {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.tool-button {
  border: 0;
  border-radius: 10px;
  padding: 8px 12px;
  background: #e7eef6;
  color: #17324d;
  cursor: pointer;
}

.tool-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
