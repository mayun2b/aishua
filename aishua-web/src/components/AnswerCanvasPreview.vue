<template>
  <div class="answer-canvas-preview">
    <canvas ref="canvasRef" class="canvas-panel"></canvas>
    <p v-if="loading" class="hint-text">手写作答加载中...</p>
    <p v-else-if="errorText" class="hint-text error-text">{{ errorText }}</p>
  </div>
</template>

<script setup>
/* global defineProps */
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import fileApi from '../modules/common/api/file'

const props = defineProps({
  objectName: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 180
  }
})

const canvasRef = ref(null)
const loading = ref(false)
const errorText = ref('')
const currentDataUrl = ref('')

let requestVersion = 0
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

const resolveHeight = () => Math.max(Number(props.height) || 180, 120)

const getContext = () => {
  const canvas = canvasRef.value
  if (!canvas) {
    return null
  }
  return canvas.getContext('2d')
}

const fillCanvasBackground = () => {
  const canvas = canvasRef.value
  const ctx = getContext()
  if (!canvas || !ctx) {
    return
  }
  ctx.save()
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvas.clientWidth, resolveHeight())
  ctx.restore()
}

const drawDataUrlToCanvas = async (dataUrl) => {
  const canvas = canvasRef.value
  const ctx = getContext()
  if (!canvas || !ctx) {
    return
  }

  fillCanvasBackground()
  if (!dataUrl) {
    return
  }

  await new Promise((resolve) => {
    const image = new Image()
    image.onload = () => {
      ctx.drawImage(image, 0, 0, canvas.clientWidth, resolveHeight())
      resolve()
    }
    image.onerror = () => resolve()
    image.src = dataUrl
  })
}

const resizeCanvas = async ({ force = false } = {}) => {
  const canvas = canvasRef.value
  if (!canvas) {
    return
  }

  const width = Math.max(canvas.parentElement?.clientWidth || canvas.clientWidth || 1, 1)
  const height = resolveHeight()
  const dpr = window.devicePixelRatio || 1

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
  await drawDataUrlToCanvas(currentDataUrl.value)
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

const blobToDataUrl = (blob) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = () => reject(new Error('手写作答读取失败'))
    reader.readAsDataURL(blob)
  })
}

const loadCanvas = async () => {
  const objectName = String(props.objectName || '').trim()
  const version = requestVersion + 1
  requestVersion = version
  errorText.value = ''

  if (!objectName) {
    currentDataUrl.value = ''
    loading.value = false
    await resizeCanvas({ force: true })
    return
  }

  loading.value = true
  try {
    const blob = await fileApi.download(objectName)
    if (version !== requestVersion) {
      return
    }

    const dataUrl = await blobToDataUrl(blob)
    if (version !== requestVersion) {
      return
    }

    currentDataUrl.value = dataUrl
    await resizeCanvas({ force: true })
  } catch (error) {
    if (version !== requestVersion) {
      return
    }

    currentDataUrl.value = ''
    errorText.value = error.message || '手写作答加载失败'
    await resizeCanvas({ force: true })
  } finally {
    if (version === requestVersion) {
      loading.value = false
    }
  }
}

const handleWindowResize = () => {
  scheduleResize(false)
}

watch(
  () => props.objectName,
  async () => {
    await loadCanvas()
  }
)

watch(
  () => props.height,
  () => {
    scheduleResize(true)
  }
)

onMounted(async () => {
  await nextTick()
  await loadCanvas()
  window.addEventListener('resize', handleWindowResize)
})

onBeforeUnmount(() => {
  clearResizeFrame()
  window.removeEventListener('resize', handleWindowResize)
})
</script>

<style scoped>
.answer-canvas-preview {
  display: grid;
  gap: 8px;
}

.canvas-panel {
  width: 100%;
  border: 1px solid #cad7e3;
  border-radius: 12px;
  background: #ffffff;
}

.hint-text {
  margin: 0;
  font-size: 12px;
  color: #5f7287;
}

.error-text {
  color: #b42318;
}
</style>
