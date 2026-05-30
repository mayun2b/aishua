<template>
  <div class="directory-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">学科目录</p>
        <h1>{{ pageTitle }}</h1>
        <p class="description">
          左侧选择教材目录，右侧查看该目录关联的考点；点击“考点练习”即可一键进入对应知识点训练。
        </p>
      </div>
      <div class="hero-actions">
        <router-link class="ghost" to="/my-subjects">返回我的学科</router-link>
        <router-link class="ghost" :to="`/practice?subjectId=${subjectId}`">通用练习页</router-link>
      </div>
    </section>

    <section class="workspace-card">
      <div class="workspace-grid">
        <div class="panel">
          <div class="panel-head">
            <h2>目录树</h2>
            <span v-if="!loadingDirectories">共 {{ flatDirectories.length }} 个目录</span>
          </div>
          <div v-if="loadingDirectories" class="empty-state">正在加载目录...</div>
          <div v-else-if="!flatDirectories.length" class="empty-state">当前学科暂无目录</div>
          <div v-else class="directory-list">
            <div
              v-for="item in visibleDirectories"
              :key="item.id"
              :class="['directory-row', selectedDirectoryId === item.id ? 'active' : '']"
              :style="{ paddingLeft: `${12 + item.depth * 18}px` }"
              @click="selectDirectory(item)"
            >
              <button
                v-if="item.hasChildren"
                type="button"
                class="expand-btn"
                @click.stop="toggleExpand(item.id)"
              >
                {{ isExpanded(item.id) ? '▾' : '▸' }}
              </button>
              <span v-else class="expand-placeholder"></span>
              <strong>{{ item.name }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <h2>目录考点</h2>
            <span v-if="selectedDirectoryName">{{ selectedDirectoryName }}</span>
          </div>

          <div class="practice-config">
            <label>
              练习题量
              <select v-model.number="questionCount">
                <option :value="5">5 题</option>
                <option :value="10">10 题</option>
                <option :value="20">20 题</option>
                <option :value="30">30 题</option>
              </select>
            </label>
          </div>

          <div v-if="loadingTags" class="empty-state">正在加载目录考点...</div>
          <div v-else-if="!selectedDirectoryId" class="empty-state">请先选择左侧目录</div>
          <div v-else-if="!tags.length" class="empty-state">该目录暂无已关联考点</div>
          <div v-else class="tag-list">
            <article v-for="tag in tags" :key="tag.tagId" class="tag-item">
              <div class="tag-main">
                <strong>{{ tag.tagName }}</strong>
                <p>
                  类型：{{ resolveRelationType(tag.relationType) }} ｜ 重要度：{{ resolveImportanceLevel(tag.importanceLevel) }} ｜
                  题量：{{ tag.questionCount ?? 0 }}
                </p>
              </div>
              <button
                type="button"
                :disabled="startingTagId === tag.tagId"
                @click="startTagPractice(tag)"
              >
                {{ startingTagId === tag.tagId ? '启动中...' : '考点练习' }}
              </button>
            </article>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import subjectApi from '../api/subject'
import practiceApi from '../../practice/api/practice'

const route = useRoute()
const router = useRouter()

const loadingDirectories = ref(false)
const loadingTags = ref(false)
const startingTagId = ref(null)

const directories = ref([])
const tags = ref([])
const selectedDirectoryId = ref(null)
const questionCount = ref(10)
const expandedDirectoryIds = ref([])

const subjectId = computed(() => Number(route.params.subjectId || 0))
const subjectName = computed(() => String(route.query.subjectName || '').trim())
const pageTitle = computed(() => {
  if (subjectName.value) {
    return `${subjectName.value} · 目录考点训练`
  }
  return `学科 #${subjectId.value || '-'} · 目录考点训练`
})

const flatDirectories = computed(() => {
  const result = []
  const walk = (nodes, depth = 0, parentId = 0) => {
    (nodes || []).forEach((node) => {
      const children = Array.isArray(node.children) ? node.children : []
      result.push({
        id: Number(node.id),
        name: node.name || `目录 ${node.id}`,
        depth,
        parentId,
        hasChildren: children.length > 0
      })
      if (children.length) {
        walk(children, depth + 1, Number(node.id))
      }
    })
  }
  walk(directories.value, 0, 0)
  return result
})

const directoryMap = computed(() => {
  return flatDirectories.value.reduce((map, item) => {
    map.set(item.id, item)
    return map
  }, new Map())
})

const visibleDirectories = computed(() => {
  const expandedSet = new Set(expandedDirectoryIds.value)
  return flatDirectories.value.filter((item) => {
    let parentId = item.parentId
    while (parentId && parentId > 0) {
      if (!expandedSet.has(parentId)) {
        return false
      }
      parentId = directoryMap.value.get(parentId)?.parentId || 0
    }
    return true
  })
})

const selectedDirectoryName = computed(() => {
  if (!selectedDirectoryId.value) {
    return ''
  }
  return directoryMap.value.get(selectedDirectoryId.value)?.name || ''
})

const isExpanded = (directoryId) => expandedDirectoryIds.value.includes(Number(directoryId))

const toggleExpand = (directoryId) => {
  const targetId = Number(directoryId)
  if (!targetId) {
    return
  }
  if (isExpanded(targetId)) {
    expandedDirectoryIds.value = expandedDirectoryIds.value.filter((id) => id !== targetId)
    return
  }
  expandedDirectoryIds.value = [...expandedDirectoryIds.value, targetId]
}

const loadDirectories = async () => {
  if (!subjectId.value || subjectId.value <= 0) {
    showToast('学科参数不合法')
    return
  }

  loadingDirectories.value = true
  try {
    const response = await subjectApi.listDirectories(subjectId.value)
    directories.value = response.data || []
    expandedDirectoryIds.value = []

    const firstDirectory = flatDirectories.value[0]
    if (firstDirectory) {
      selectDirectory(firstDirectory, { autoExpand: true })
    } else {
      selectedDirectoryId.value = null
      tags.value = []
    }
  } catch (error) {
    showToast(error.message || '加载学科目录失败')
    directories.value = []
    selectedDirectoryId.value = null
    tags.value = []
  } finally {
    loadingDirectories.value = false
  }
}

const loadDirectoryTags = async (directoryId) => {
  if (!subjectId.value || !directoryId) {
    tags.value = []
    return
  }
  loadingTags.value = true
  try {
    const response = await subjectApi.listDirectoryTags(subjectId.value, directoryId)
    tags.value = response.data || []
  } catch (error) {
    tags.value = []
    showToast(error.message || '加载目录考点失败')
  } finally {
    loadingTags.value = false
  }
}

const selectDirectory = async (item, options = {}) => {
  if (!item?.id) {
    return
  }
  selectedDirectoryId.value = item.id
  if (options.autoExpand && item.hasChildren && !isExpanded(item.id)) {
    toggleExpand(item.id)
  }
  await loadDirectoryTags(item.id)
}

const resolveRelationType = (relationType) => {
  if (relationType === 1) {
    return '核心考点'
  }
  if (relationType === 2) {
    return '关联考点'
  }
  if (relationType === 3) {
    return '拓展考点'
  }
  return '-'
}

const resolveImportanceLevel = (importanceLevel) => {
  const level = Number(importanceLevel)
  if (level === 1) {
    return '低'
  }
  if (level === 2) {
    return '中'
  }
  if (level === 3) {
    return '高'
  }
  return '-'
}

const normalizeQuestionCount = () => {
  const count = Number(questionCount.value)
  if (!Number.isFinite(count) || count < 1) {
    return 10
  }
  return Math.min(50, Math.floor(count))
}

const startTagPractice = async (tag) => {
  const tagId = Number(tag?.tagId || 0)
  if (!subjectId.value || !tagId) {
    showToast('考点参数不合法')
    return
  }

  startingTagId.value = tagId
  try {
    const response = await practiceApi.start({
      subjectId: subjectId.value,
      practiceMode: 3,
      questionCount: normalizeQuestionCount(),
      tagIds: [tagId]
    })
    const sessionId = Number(response?.data?.sessionId || 0)
    if (!sessionId) {
      throw new Error('创建练习会话失败')
    }

    await router.push({
      name: 'PracticeSession',
      params: { sessionId },
      query: {
        subjectId: String(subjectId.value),
        mode: '3'
      }
    })
  } catch (error) {
    showToast(error.message || '启动考点练习失败')
  } finally {
    startingTagId.value = null
  }
}

onMounted(() => {
  loadDirectories()
})
</script>

<style scoped>
.directory-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #eef4ea 0%, #e1edf8 100%);
}

.hero-card,
.workspace-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card {
  padding: 30px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #7a8793;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 12px 0 0;
  color: #17324d;
  font-size: 32px;
}

.description {
  margin: 14px 0 0;
  max-width: 780px;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.ghost {
  border: 0;
  border-radius: 14px;
  padding: 11px 16px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  background: #e5edf6;
  color: #17324d;
}

.workspace-card {
  margin-top: 18px;
  padding: 24px;
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: 16px;
}

.panel {
  border: 1px solid #dce5ef;
  border-radius: 18px;
  background: #f9fcff;
  min-height: 420px;
  display: flex;
  flex-direction: column;
}

.panel-head {
  padding: 16px 16px 14px;
  border-bottom: 1px solid #e3ebf4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 18px;
}

.panel-head span {
  color: #6c7a8d;
  font-size: 13px;
}

.directory-list {
  padding: 10px;
  overflow: auto;
}

.directory-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  border-radius: 10px;
  color: #17324d;
  cursor: pointer;
}

.directory-row:hover {
  background: #edf4fc;
}

.directory-row.active {
  background: #dfeffd;
}

.expand-btn {
  width: 22px;
  height: 22px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #55708b;
  cursor: pointer;
}

.expand-placeholder {
  width: 22px;
  height: 22px;
  display: inline-block;
}

.practice-config {
  padding: 12px 16px 0;
}

.practice-config label {
  color: #4f6072;
  font-size: 13px;
}

.practice-config select {
  margin-left: 10px;
  border: 1px solid #cad7e3;
  border-radius: 10px;
  padding: 6px 10px;
  background: #fff;
}

.tag-list {
  padding: 12px 16px 16px;
  display: grid;
  gap: 10px;
  overflow: auto;
}

.tag-item {
  border: 1px solid #d9e4ef;
  border-radius: 14px;
  background: #fff;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.tag-main {
  min-width: 0;
}

.tag-main strong {
  color: #17324d;
}

.tag-main p {
  margin: 8px 0 0;
  color: #617386;
  font-size: 13px;
  line-height: 1.5;
}

.tag-item button {
  border: 0;
  border-radius: 10px;
  padding: 9px 12px;
  background: #17324d;
  color: #fff;
  cursor: pointer;
  white-space: nowrap;
}

.tag-item button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.empty-state {
  margin: 14px;
  padding: 24px 16px;
  text-align: center;
  color: #6c7a8d;
  background: #f2f7fc;
  border-radius: 12px;
}

@media (max-width: 900px) {
  .directory-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .workspace-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 26px;
  }

  .workspace-grid {
    grid-template-columns: 1fr;
  }
}
</style>
