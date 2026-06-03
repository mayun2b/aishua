<template>
  <div v-if="imageItems.length" class="question-image-list">
    <div v-if="canAnnotate" class="image-toolbar" aria-label="题图标注工具">
      <button type="button" :class="{ active: selectedTool === 'line' }" @click="selectedTool = 'line'">
        直线
      </button>
      <button type="button" :class="{ active: selectedTool === 'pen' }" @click="selectedTool = 'pen'">
        画笔
      </button>
    </div>

    <figure v-for="item in imageItems" :key="item.key" class="question-image-item">
      <p v-if="item.loading" class="image-state">图片加载中...</p>
      <p v-else-if="item.error" class="image-state error-text">{{ item.error }}</p>
      <div v-else-if="item.url" class="image-stage">
        <img
          :ref="(el) => setImageRef(item.key, el)"
          :src="item.url"
          :alt="imageAltText(item)"
          @load="syncCanvasSize(item)"
        >
        <img
          v-if="annotationLayerUrl(item)"
          class="annotation-layer"
          :src="annotationLayerUrl(item)"
          alt="作答辅助线"
        >
        <canvas
          v-if="canAnnotate"
          :ref="(el) => setCanvasRef(item.key, el)"
          class="annotation-canvas"
          @pointerdown="handlePointerDown(item, $event)"
          @pointermove="handlePointerMove(item, $event)"
          @pointerup="handlePointerUp(item, $event)"
          @pointercancel="handlePointerUp(item, $event)"
          @pointerleave="handlePointerUp(item, $event)"
        />
      </div>
      <div v-if="canAnnotate && item.url" class="image-actions">
        <button type="button" @click="undoImageAnnotation(item)">撤销</button>
        <button type="button" @click="clearImageAnnotation(item)">清空</button>
      </div>
    </figure>

    <p v-if="imageDesc" class="image-desc">{{ imageDesc }}</p>
  </div>
</template>

<script setup>
/* global defineEmits, defineProps */
import { computed, nextTick, ref, watch } from 'vue'
import fileApi from '../modules/common/api/file'
import {
  parseQuestionImageRefs,
  resolveQuestionImagePreviewUrl
} from '../modules/common/utils/questionImages'

const props = defineProps({
  imageUrls: {
    type: String,
    default: ''
  },
  imageDesc: {
    type: String,
    default: ''
  },
  modelValue: {
    type: Object,
    default: () => ({})
  },
  annotationObjectNames: {
    type: Object,
    default: () => ({})
  },
  readonly: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'dirty'])

const imageItems = ref([])
const annotationPreviewUrls = ref({})
const imageRefs = ref({})
const canvasRefs = ref({})
const drawingState = ref({})
const historyStacks = ref({})
const restoredDraftSources = ref({})
const selectedTool = ref('line')
let requestVersion = 0
let annotationRequestVersion = 0

const canAnnotate = computed(() => !props.readonly && !props.disabled)

const isDirectUrl = (value) => /^(https?:\/\/|data:|blob:|\/)/i.test(String(value || ''))

const imageKeyOf = (item) => item.objectName || item.key

const imageAltText = (item) => {
  return props.imageDesc || item.objectName || item.url || '题目配图'
}

const setImageRef = (key, el) => {
  if (el) {
    imageRefs.value[key] = el
  } else {
    delete imageRefs.value[key]
  }
}

const setCanvasRef = (key, el) => {
  if (el) {
    canvasRefs.value[key] = el
  } else {
    delete canvasRefs.value[key]
  }
}

const getCanvasContext = (item) => {
  const canvas = canvasRefs.value[item.key]
  if (!canvas) {
    return null
  }
  return canvas.getContext('2d')
}

const getPointerPosition = (item, event) => {
  const canvas = canvasRefs.value[item.key]
  const rect = canvas.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

const captureCanvas = (item) => {
  const canvas = canvasRefs.value[item.key]
  if (!canvas || !canvas.width || !canvas.height) {
    return ''
  }
  try {
    return canvas.toDataURL('image/png')
  } catch (error) {
    return ''
  }
}

const pushHistory = (item) => {
  const snapshot = captureCanvas(item)
  const stack = historyStacks.value[item.key] || []
  historyStacks.value = {
    ...historyStacks.value,
    [item.key]: [...stack.slice(-9), snapshot]
  }
}

const emitDraft = (item, dataUrl = captureCanvas(item)) => {
  const imageKey = imageKeyOf(item)
  emit('update:modelValue', {
    ...(props.modelValue || {}),
    [imageKey]: dataUrl
  })
  emit('dirty', imageKey)
}

const clearCanvas = (item) => {
  const canvas = canvasRefs.value[item.key]
  const context = getCanvasContext(item)
  if (!canvas || !context) {
    return
  }
  context.clearRect(0, 0, canvas.width, canvas.height)
}

const restoreDraftToCanvas = (item) => {
  if (!canAnnotate.value) {
    return
  }
  const canvas = canvasRefs.value[item.key]
  const context = getCanvasContext(item)
  const imageKey = imageKeyOf(item)
  const draftSource = String(props.modelValue?.[imageKey] || '').trim()
  const restoreKey = `${draftSource}|${canvas?.width || 0}|${canvas?.height || 0}`
  if (!canvas || !context || !draftSource || restoredDraftSources.value[item.key] === restoreKey) {
    return
  }

  const image = new Image()
  image.onload = () => {
    context.clearRect(0, 0, canvas.width, canvas.height)
    context.drawImage(image, 0, 0, canvas.width, canvas.height)
    restoredDraftSources.value = {
      ...restoredDraftSources.value,
      [item.key]: restoreKey
    }
  }
  image.src = draftSource
}

const syncCanvasSize = async (item) => {
  await nextTick()
  if (!canAnnotate.value) {
    return
  }
  const image = imageRefs.value[item.key]
  const canvas = canvasRefs.value[item.key]
  if (!image || !canvas) {
    return
  }

  const rect = image.getBoundingClientRect()
  const width = Math.max(Math.round(rect.width), 1)
  const height = Math.max(Math.round(rect.height), 1)
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width
    canvas.height = height
  }
  restoreDraftToCanvas(item)
}

const handlePointerDown = (item, event) => {
  if (!canAnnotate.value) {
    return
  }
  const context = getCanvasContext(item)
  if (!context) {
    return
  }
  event.preventDefault()
  event.currentTarget.setPointerCapture?.(event.pointerId)
  pushHistory(item)
  const position = getPointerPosition(item, event)
  drawingState.value = {
    ...drawingState.value,
    [item.key]: {
      drawing: true,
      start: position,
      last: position,
      baseImageData: context.getImageData(0, 0, context.canvas.width, context.canvas.height)
    }
  }
  context.lineCap = 'round'
  context.lineJoin = 'round'
  context.strokeStyle = '#17324d'
  context.lineWidth = 3
  if (selectedTool.value === 'pen') {
    context.beginPath()
    context.moveTo(position.x, position.y)
  }
}

const handlePointerMove = (item, event) => {
  const state = drawingState.value[item.key]
  const context = getCanvasContext(item)
  if (!canAnnotate.value || !state?.drawing || !context) {
    return
  }
  event.preventDefault()
  const position = getPointerPosition(item, event)
  context.strokeStyle = '#17324d'
  context.lineWidth = 3
  context.lineCap = 'round'
  context.lineJoin = 'round'

  if (selectedTool.value === 'line') {
    context.putImageData(state.baseImageData, 0, 0)
    context.beginPath()
    context.moveTo(state.start.x, state.start.y)
    context.lineTo(position.x, position.y)
    context.stroke()
  } else {
    context.beginPath()
    context.moveTo(state.last.x, state.last.y)
    context.lineTo(position.x, position.y)
    context.stroke()
  }

  drawingState.value = {
    ...drawingState.value,
    [item.key]: {
      ...state,
      last: position
    }
  }
}

const handlePointerUp = (item, event) => {
  const state = drawingState.value[item.key]
  if (!state?.drawing) {
    return
  }
  event?.currentTarget?.releasePointerCapture?.(event.pointerId)
  drawingState.value = {
    ...drawingState.value,
    [item.key]: {
      ...state,
      drawing: false
    }
  }
  emitDraft(item)
}

const undoImageAnnotation = (item) => {
  const stack = historyStacks.value[item.key] || []
  if (!stack.length) {
    clearImageAnnotation(item)
    return
  }
  const previous = stack[stack.length - 1]
  historyStacks.value = {
    ...historyStacks.value,
    [item.key]: stack.slice(0, -1)
  }
  const canvas = canvasRefs.value[item.key]
  const context = getCanvasContext(item)
  if (!canvas || !context) {
    return
  }
  context.clearRect(0, 0, canvas.width, canvas.height)
  if (!previous) {
    emitDraft(item, '')
    return
  }
  const image = new Image()
  image.onload = () => {
    context.drawImage(image, 0, 0, canvas.width, canvas.height)
    emitDraft(item)
  }
  image.src = previous
}

const clearImageAnnotation = (item) => {
  pushHistory(item)
  clearCanvas(item)
  emitDraft(item, '')
}

const annotationLayerUrl = (item) => {
  const imageKey = imageKeyOf(item)
  if (!props.readonly) {
    return ''
  }
  return annotationPreviewUrls.value[imageKey] || ''
}

const loadAnnotationPreviews = async () => {
  const version = annotationRequestVersion + 1
  annotationRequestVersion = version
  const entries = Object.entries(props.annotationObjectNames || {})
    .map(([imageKey, objectName]) => [imageKey, String(objectName || '').trim()])
    .filter(([, objectName]) => objectName)

  if (!entries.length) {
    annotationPreviewUrls.value = {}
    return
  }

  const nextUrls = {}
  for (const [imageKey, objectName] of entries) {
    try {
      if (isDirectUrl(objectName)) {
        nextUrls[imageKey] = objectName
      } else {
        const response = await fileApi.getPreviewUrl(objectName)
        const url = resolveQuestionImagePreviewUrl(response)
        if (url) {
          nextUrls[imageKey] = url
        }
      }
    } catch (error) {
      nextUrls[imageKey] = ''
    }
  }

  if (version === annotationRequestVersion) {
    annotationPreviewUrls.value = nextUrls
  }
}

const loadQuestionImages = async () => {
  const version = requestVersion + 1
  requestVersion = version

  const refs = parseQuestionImageRefs(props.imageUrls)
  imageItems.value = refs.map((item) => ({
    ...item,
    loading: Boolean(item.objectName),
    error: ''
  }))

  for (const item of refs) {
    if (!item.objectName) {
      continue
    }

    try {
      const response = await fileApi.getPreviewUrl(item.objectName)
      const url = resolveQuestionImagePreviewUrl(response)
      if (!url) {
        throw new Error('图片预览地址生成失败')
      }
      if (version !== requestVersion) {
        return
      }
      imageItems.value = imageItems.value.map((current) => (
        current.key === item.key
          ? { ...current, url, loading: false, error: '' }
          : current
      ))
    } catch (error) {
      if (version !== requestVersion) {
        return
      }
      imageItems.value = imageItems.value.map((current) => (
        current.key === item.key
          ? { ...current, loading: false, error: error.message || '题目图片加载失败' }
          : current
      ))
    }
  }
}

watch(
  () => props.imageUrls,
  () => {
    void loadQuestionImages()
  },
  { immediate: true }
)

watch(
  () => props.annotationObjectNames,
  () => {
    void loadAnnotationPreviews()
  },
  { immediate: true, deep: true }
)

watch(
  () => props.modelValue,
  () => {
    for (const item of imageItems.value) {
      void syncCanvasSize(item)
    }
  },
  { deep: true }
)
</script>

<style scoped>
.question-image-list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.image-toolbar,
.image-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.image-toolbar button,
.image-actions button {
  border: 1px solid #cbd9e8;
  border-radius: 999px;
  padding: 6px 13px;
  background: #f6f9fd;
  color: #17324d;
  cursor: pointer;
}

.image-toolbar button.active {
  border-color: #17324d;
  background: #17324d;
  color: #fff;
}

.question-image-item {
  margin: 0;
  width: fit-content;
  max-width: 100%;
  padding: 10px;
  border: 1px solid #d9e3ee;
  border-radius: 12px;
  background: #f8fbff;
}

.image-stage {
  position: relative;
  display: inline-block;
  max-width: min(640px, 100%);
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.image-stage > img:first-child {
  display: block;
  max-width: min(640px, 100%);
  max-height: 420px;
  object-fit: contain;
}

.annotation-canvas,
.annotation-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.annotation-canvas {
  touch-action: none;
  cursor: crosshair;
}

.annotation-layer {
  pointer-events: none;
}

.image-actions {
  margin-top: 8px;
}

.image-state {
  margin: 0;
  color: #617386;
  font-size: 13px;
}

.error-text {
  color: #b42318;
}

.image-desc {
  margin: 0;
  color: #607285;
  line-height: 1.7;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .question-image-item {
    width: 100%;
  }

  .image-stage,
  .image-stage > img:first-child {
    max-width: 100%;
  }
}
</style>
