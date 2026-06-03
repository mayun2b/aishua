import { onBeforeUnmount } from 'vue'

export default function useAutoReload(reload) {
  let timer = null
  let suppressionDepth = 0

  const isReloadSuppressed = () => suppressionDepth > 0

  const clearScheduledReload = () => {
    if (!timer) {
      return
    }
    clearTimeout(timer)
    timer = null
  }

  const scheduleReload = ({ delay = 0 } = {}) => {
    if (isReloadSuppressed()) {
      return
    }

    clearScheduledReload()
    timer = setTimeout(() => {
      timer = null
      reload()
    }, delay)
  }

  const runWithoutAutoReload = async (operation) => {
    suppressionDepth += 1
    clearScheduledReload()
    try {
      return await operation()
    } finally {
      suppressionDepth = Math.max(0, suppressionDepth - 1)
    }
  }

  onBeforeUnmount(() => {
    clearScheduledReload()
  })

  return {
    clearScheduledReload,
    isReloadSuppressed,
    runWithoutAutoReload,
    scheduleReload
  }
}
