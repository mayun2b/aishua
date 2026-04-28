<template>
  <nav v-if="total > pageSize" class="base-pagination" aria-label="Pagination">
    <span class="summary">共 {{ total }} 条，第 {{ currentPage }} / {{ totalPages }} 页</span>

    <div class="actions">
      <button type="button" :disabled="currentPage === 1" @click="updatePage(1)">
        首页
      </button>
      <button type="button" :disabled="currentPage === 1" @click="updatePage(currentPage - 1)">
        上一页
      </button>

      <button
        v-for="page in pageNumbers"
        :key="page"
        type="button"
        :class="{ active: page === currentPage }"
        @click="updatePage(page)"
      >
        {{ page }}
      </button>

      <button type="button" :disabled="currentPage === totalPages" @click="updatePage(currentPage + 1)">
        下一页
      </button>
      <button type="button" :disabled="currentPage === totalPages" @click="updatePage(totalPages)">
        末页
      </button>
    </div>
  </nav>
</template>

<script setup>
/* global defineEmits, defineProps */
import { computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 1
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:modelValue'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const currentPage = computed(() => {
  const rawPage = Number(props.modelValue) || 1
  return Math.min(Math.max(rawPage, 1), totalPages.value)
})

const pageNumbers = computed(() => {
  const maxVisible = 5
  const half = Math.floor(maxVisible / 2)
  let start = Math.max(1, currentPage.value - half)
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})

const updatePage = (page) => {
  const nextPage = Math.min(Math.max(page, 1), totalPages.value)
  if (nextPage !== props.modelValue) {
    emit('update:modelValue', nextPage)
  }
}

watch(
  [() => props.modelValue, totalPages],
  () => {
    if (currentPage.value !== props.modelValue) {
      emit('update:modelValue', currentPage.value)
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.base-pagination {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  align-items: center;
}

.summary {
  color: #6c7a8d;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.actions button {
  border: 1px solid #d5dee8;
  background: #fff;
  color: #17324d;
  border-radius: 12px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
}

.actions button.active {
  background: #17324d;
  color: #fff;
  border-color: #17324d;
}

.actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
