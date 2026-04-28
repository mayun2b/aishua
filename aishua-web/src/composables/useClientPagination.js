import { computed, ref, watch } from 'vue'

export default function useClientPagination(source, pageSize = 10) {
  const currentPage = ref(1)

  const total = computed(() => source.value.length)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
  const pagedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return source.value.slice(start, start + pageSize)
  })

  const resetPage = () => {
    currentPage.value = 1
  }

  watch(totalPages, (nextTotalPages) => {
    if (currentPage.value > nextTotalPages) {
      currentPage.value = nextTotalPages
    }
  })

  return {
    currentPage,
    pageSize,
    pagedItems,
    resetPage,
    total,
    totalPages
  }
}
