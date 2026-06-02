# Question Image Annotation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let students draw auxiliary lines on question images, save those drawings to MinIO with answers, and replay them in practice/exam record detail pages.

**Architecture:** Keep original question images immutable and store only MinIO object names. The browser draws on a transparent canvas over each image, uploads the transparent annotation PNG before draft save or submission, and appends a structured marker to `userAnswer`; backend judgement strips the marker while record detail keeps it for replay.

**Tech Stack:** Vue 3 SFC, existing `fileApi.upload/getPreviewUrl`, existing Spring Boot/MyBatis services, Lombok VOs, JUnit 5, plain Node tests for frontend utilities.

---

## File Structure

- Create `aishua-web/src/modules/common/utils/imageAnnotationAnswer.js`: build, parse, and strip `[[image-annotations:...]]` markers.
- Create `aishua-web/tests/imageAnnotationAnswer.test.js`: Node tests for marker behavior.
- Modify `aishua-web/src/components/QuestionImageList.vue`: compact image display plus editable/read-only annotation overlay.
- Modify `aishua-web/src/modules/practice/views/PracticeSessionView.vue`: maintain per-question annotation drafts/object names, upload before draft save/submit, append markers to answers.
- Modify `aishua-web/src/modules/exam/views/ExamSessionView.vue`: maintain annotation drafts/object names in client state, upload before submit, append markers to answers.
- Modify `aishua-web/src/modules/practice/views/PracticeRecordDetailView.vue`: show record images in read-only mode and hide markers from answer text.
- Modify `aishua-web/src/modules/exam/views/ExamRecordDetailView.vue`: show record images in read-only mode and hide markers from answer text.
- Modify `aishua-web/src/modules/exam/utils/examHelpers.js`: strip image annotation markers from displayed answers and token parsing.
- Modify `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupport.java`: strip image annotation markers before judgement.
- Create `aishua-master/src/test/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupportTest.java`: backend judgement compatibility tests.
- Modify `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/entity/vo/PracticeExerciseRecordVO.java`: add `imageUrls` and `imageDesc`.
- Modify `aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/entity/vo/ExamRecordQuestionVO.java`: add `imageUrls` and `imageDesc`.
- Modify `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/service/impl/PracticeServiceImpl.java`: fill image fields in record VOs.
- Modify `aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/service/impl/ExamServiceImpl.java`: fill image fields in record VOs.

---

### Task 1: Frontend Marker Utility

**Files:**
- Create: `aishua-web/src/modules/common/utils/imageAnnotationAnswer.js`
- Create: `aishua-web/tests/imageAnnotationAnswer.test.js`

- [ ] **Step 1: Write the failing utility test**

Create `aishua-web/tests/imageAnnotationAnswer.test.js`:

```js
const assert = require('node:assert/strict')
const {
  IMAGE_ANNOTATION_MARKER_PREFIX,
  buildImageAnnotationPayload,
  parseImageAnnotationPayload,
  stripImageAnnotationMarkers
} = require('../src/modules/common/utils/imageAnnotationAnswer')

const payload = buildImageAnnotationPayload('A', [
  { imageObjectName: 'questions/q043.png', annotationObjectName: 'answers/a043.png' },
  { imageObjectName: 'questions/q043.png', annotationObjectName: '' },
  { imageObjectName: '', annotationObjectName: 'answers/ignored.png' }
])

assert.ok(payload.includes(IMAGE_ANNOTATION_MARKER_PREFIX))
assert.equal(stripImageAnnotationMarkers(payload), 'A')

const parsed = parseImageAnnotationPayload(payload)
assert.equal(parsed.text, 'A')
assert.deepEqual(parsed.annotations, [
  { imageObjectName: 'questions/q043.png', annotationObjectName: 'answers/a043.png' }
])

const onlyMarker = buildImageAnnotationPayload('', [
  { imageObjectName: 'q.png', annotationObjectName: 'anno.png' }
])
assert.equal(parseImageAnnotationPayload(onlyMarker).text, '')
assert.equal(stripImageAnnotationMarkers('B').trim(), 'B')
assert.equal(parseImageAnnotationPayload('bad [[image-annotations:not-json]]').text, 'bad')

console.log('imageAnnotationAnswer tests passed')
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
cd aishua-web
node tests/imageAnnotationAnswer.test.js
```

Expected: fail with `Cannot find module '../src/modules/common/utils/imageAnnotationAnswer'`.

- [ ] **Step 3: Implement the utility**

Create `aishua-web/src/modules/common/utils/imageAnnotationAnswer.js`:

```js
const IMAGE_ANNOTATION_MARKER_PREFIX = '[[image-annotations:'
const IMAGE_ANNOTATION_MARKER_SUFFIX = ']]'
const IMAGE_ANNOTATION_MARKER_PATTERN = /\[\[image-annotations:([\s\S]*?)]]/gi

const normalizeText = (value) => (value == null ? '' : String(value))

const normalizeAnnotation = (item) => {
  const imageObjectName = String(item?.imageObjectName || '').trim()
  const annotationObjectName = String(item?.annotationObjectName || '').trim()
  if (!imageObjectName || !annotationObjectName) {
    return null
  }
  return { imageObjectName, annotationObjectName }
}

const normalizeAnnotations = (annotations) => {
  if (!Array.isArray(annotations)) {
    return []
  }
  const seen = new Set()
  const result = []
  for (const item of annotations) {
    const normalized = normalizeAnnotation(item)
    if (!normalized) {
      continue
    }
    const key = `${normalized.imageObjectName}\n${normalized.annotationObjectName}`
    if (seen.has(key)) {
      continue
    }
    seen.add(key)
    result.push(normalized)
  }
  return result
}

const buildImageAnnotationMarker = (annotations) => {
  const normalized = normalizeAnnotations(annotations)
  if (!normalized.length) {
    return ''
  }
  return `${IMAGE_ANNOTATION_MARKER_PREFIX}${JSON.stringify(normalized)}${IMAGE_ANNOTATION_MARKER_SUFFIX}`
}

const stripImageAnnotationMarkers = (value) => {
  return normalizeText(value).replace(IMAGE_ANNOTATION_MARKER_PATTERN, '').trim()
}

const parseImageAnnotationPayload = (value) => {
  const raw = normalizeText(value)
  const annotations = []
  raw.replace(IMAGE_ANNOTATION_MARKER_PATTERN, (match, jsonText) => {
    try {
      annotations.push(...normalizeAnnotations(JSON.parse(jsonText)))
    } catch (error) {
      return ''
    }
    return ''
  })
  return {
    text: stripImageAnnotationMarkers(raw),
    annotations
  }
}

const buildImageAnnotationPayload = (text, annotations) => {
  const normalizedText = normalizeText(text).trim()
  const marker = buildImageAnnotationMarker(annotations)
  if (!marker) {
    return normalizedText
  }
  return normalizedText ? `${normalizedText}\n${marker}` : marker
}

module.exports = {
  IMAGE_ANNOTATION_MARKER_PREFIX,
  IMAGE_ANNOTATION_MARKER_SUFFIX,
  buildImageAnnotationMarker,
  buildImageAnnotationPayload,
  parseImageAnnotationPayload,
  stripImageAnnotationMarkers
}
```

- [ ] **Step 4: Run the utility test**

Run:

```powershell
cd aishua-web
node tests/imageAnnotationAnswer.test.js
```

Expected: `imageAnnotationAnswer tests passed`.

- [ ] **Step 5: Commit**

```powershell
git add aishua-web/src/modules/common/utils/imageAnnotationAnswer.js aishua-web/tests/imageAnnotationAnswer.test.js
git commit -m "feat: add image annotation answer markers"
```

---

### Task 2: Backend Judgement and Record Image Fields

**Files:**
- Modify: `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupport.java`
- Create: `aishua-master/src/test/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupportTest.java`
- Modify: `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/entity/vo/PracticeExerciseRecordVO.java`
- Modify: `aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/entity/vo/ExamRecordQuestionVO.java`
- Modify: `aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/service/impl/PracticeServiceImpl.java`
- Modify: `aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/service/impl/ExamServiceImpl.java`

- [ ] **Step 1: Write the failing backend test**

Create `aishua-master/src/test/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupportTest.java`:

```java
package zysy.iflytek.aishua.modules.practice.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerJudgeSupportTest {

    private final AnswerJudgeSupport support = new AnswerJudgeSupport();

    @Test
    void stripsImageAnnotationMarkersForSingleChoice() {
        String userAnswer = "A\n[[image-annotations:[{\"imageObjectName\":\"q.png\",\"annotationObjectName\":\"anno.png\"}]]]";
        assertTrue(support.isCorrect(1, "A", userAnswer));
    }

    @Test
    void stripsImageAnnotationMarkersForMultipleChoice() {
        String userAnswer = "B,A\n[[image-annotations:[{\"imageObjectName\":\"q.png\",\"annotationObjectName\":\"anno.png\"}]]]";
        assertTrue(support.isCorrect(2, "A,B", userAnswer));
    }

    @Test
    void keepsExistingCanvasMarkerCompatibility() {
        assertTrue(support.isCorrect(5, "证明略", "证明略\n[[canvas:answers/essay.png]]"));
    }
}
```

- [ ] **Step 2: Run the backend test to verify it fails**

Run:

```powershell
cd aishua-master
mvn -Dtest=AnswerJudgeSupportTest test
```

Expected: fail because image annotation marker is not stripped.

- [ ] **Step 3: Strip image markers in judgement**

Modify `AnswerJudgeSupport.java`:

```java
private static final Pattern CANVAS_MARKER_PATTERN = Pattern.compile("\\[\\[canvas:[^\\]]*]]", Pattern.CASE_INSENSITIVE);
private static final Pattern IMAGE_ANNOTATION_MARKER_PATTERN = Pattern.compile("\\[\\[image-annotations:[\\s\\S]*?]]", Pattern.CASE_INSENSITIVE);
```

Replace the private stripping method with:

```java
private String stripAnswerMarkers(String value) {
    if (value == null || value.isBlank()) {
        return value == null ? "" : value;
    }
    String withoutImageAnnotations = IMAGE_ANNOTATION_MARKER_PATTERN.matcher(value).replaceAll(" ");
    return CANVAS_MARKER_PATTERN.matcher(withoutImageAnnotations).replaceAll(" ");
}
```

In `normalizeSimpleText`, replace `stripCanvasMarker(value)` with `stripAnswerMarkers(value)`.

In `normalizeAnswerTokens`, replace the initial `String trimmed = value.trim();` with:

```java
String trimmed = stripAnswerMarkers(value).trim();
```

- [ ] **Step 4: Add image fields to record VOs**

In `PracticeExerciseRecordVO.java`, add after `difficulty`:

```java
private String imageUrls;

private String imageDesc;
```

In `ExamRecordQuestionVO.java`, add after `difficulty`:

```java
private String imageUrls;
private String imageDesc;
```

- [ ] **Step 5: Fill image fields in services**

In both loops that build `PracticeExerciseRecordVO` in `PracticeServiceImpl.java`, add after `recordVO.setDifficulty(...)`:

```java
recordVO.setImageUrls(question == null ? null : question.getImageUrls());
recordVO.setImageDesc(question == null ? null : question.getImageDesc());
```

In `ExamServiceImpl.java` inside `buildRecordQuestionVOList`, add after `questionVO.setDifficulty(question.getDifficulty());`:

```java
questionVO.setImageUrls(question.getImageUrls());
questionVO.setImageDesc(question.getImageDesc());
```

- [ ] **Step 6: Run backend test**

Run:

```powershell
cd aishua-master
mvn -Dtest=AnswerJudgeSupportTest test
```

Expected: test passes.

- [ ] **Step 7: Commit**

```powershell
git add aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupport.java `
  aishua-master/src/test/java/zysy/iflytek/aishua/modules/practice/support/AnswerJudgeSupportTest.java `
  aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/entity/vo/PracticeExerciseRecordVO.java `
  aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/entity/vo/ExamRecordQuestionVO.java `
  aishua-master/src/main/java/zysy/iflytek/aishua/modules/practice/service/impl/PracticeServiceImpl.java `
  aishua-master/src/main/java/zysy/iflytek/aishua/modules/exam/service/impl/ExamServiceImpl.java
git commit -m "feat: preserve image annotations without affecting judgement"
```

---

### Task 3: Compact Image Annotation Component

**Files:**
- Modify: `aishua-web/src/components/QuestionImageList.vue`
- Test: `aishua-web/tests/questionImages.test.js`

- [ ] **Step 1: Extend the existing image parsing test**

Append to `aishua-web/tests/questionImages.test.js`:

```js
const refs = parseQuestionImageRefs('questions/q043.png, https://example.com/a.png, questions/q043.png')
assert.equal(refs.length, 2)
assert.equal(refs[0].objectName, 'questions/q043.png')
assert.equal(refs[1].url, 'https://example.com/a.png')
```

Run:

```powershell
cd aishua-web
node tests/questionImages.test.js
```

Expected: pass; this guards existing image parsing before component work.

- [ ] **Step 2: Add component props and emits**

In `QuestionImageList.vue`, replace the props block with:

```js
const props = defineProps({
  imageUrls: { type: String, default: '' },
  imageDesc: { type: String, default: '' },
  modelValue: { type: Object, default: () => ({}) },
  annotationObjectNames: { type: Object, default: () => ({}) },
  readonly: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'dirty'])
```

- [ ] **Step 3: Add per-image drawing state**

Add refs and helper methods in the component script:

```js
const canvasRefs = ref({})
const imageRefs = ref({})
const drawingState = ref({})
const selectedTool = ref('line')
const strokeColor = ref('#17324d')
const lineWidth = ref(3)

const hasEditableCanvas = computed(() => !props.readonly && !props.disabled)

const setImageRef = (key, el) => {
  if (el) imageRefs.value[key] = el
}

const setCanvasRef = (key, el) => {
  if (el) canvasRefs.value[key] = el
}

const emitDraft = (imageKey, dataUrl) => {
  emit('update:modelValue', {
    ...(props.modelValue || {}),
    [imageKey]: dataUrl
  })
  emit('dirty', imageKey)
}
```

The implementation must size each canvas to the rendered image dimensions on image load, draw either freehand or a straight line, and call `emitDraft(item.objectName || item.key, canvas.toDataURL('image/png'))` on pointer up.

- [ ] **Step 4: Replace the template with compact image cards**

Use this structure:

```vue
<div v-if="imageItems.length" class="question-image-list">
  <div class="image-toolbar" v-if="!readonly">
    <button type="button" :class="{ active: selectedTool === 'line' }" @click="selectedTool = 'line'">直线</button>
    <button type="button" :class="{ active: selectedTool === 'pen' }" @click="selectedTool = 'pen'">画笔</button>
  </div>
  <figure v-for="item in imageItems" :key="item.key" class="question-image-item">
    <p v-if="item.loading" class="image-state">图片加载中...</p>
    <p v-else-if="item.error" class="image-state error-text">{{ item.error }}</p>
    <div v-else-if="item.url" class="image-stage">
      <img :ref="(el) => setImageRef(item.key, el)" :src="item.url" :alt="imageAltText(item)" @load="syncCanvasSize(item)" />
      <canvas
        :ref="(el) => setCanvasRef(item.key, el)"
        class="annotation-canvas"
        @pointerdown="handlePointerDown(item, $event)"
        @pointermove="handlePointerMove(item, $event)"
        @pointerup="handlePointerUp(item, $event)"
        @pointercancel="handlePointerUp(item, $event)"
      />
      <img v-if="readonlyAnnotationUrl(item)" class="annotation-layer" :src="readonlyAnnotationUrl(item)" alt="作答辅助线" />
    </div>
    <div v-if="!readonly && item.url" class="image-actions">
      <button type="button" @click="undoImageAnnotation(item)">撤销</button>
      <button type="button" @click="clearImageAnnotation(item)">清空</button>
    </div>
  </figure>
  <p v-if="imageDesc" class="image-desc">{{ imageDesc }}</p>
</div>
```

- [ ] **Step 5: Replace styles with compact sizing**

Use max dimensions that avoid the previous large blank panel:

```css
.question-image-list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
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

.image-stage img:first-child {
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
```

- [ ] **Step 6: Run frontend utility tests**

Run:

```powershell
cd aishua-web
node tests/questionImages.test.js
node tests/imageAnnotationAnswer.test.js
```

Expected: both pass.

- [ ] **Step 7: Commit**

```powershell
git add aishua-web/src/components/QuestionImageList.vue aishua-web/tests/questionImages.test.js
git commit -m "feat: add drawable question image component"
```

---

### Task 4: Practice Session Save and Submit Integration

**Files:**
- Modify: `aishua-web/src/modules/practice/views/PracticeSessionView.vue`

- [ ] **Step 1: Import annotation utilities**

Add:

```js
import {
  buildImageAnnotationPayload,
  parseImageAnnotationPayload
} from '../../common/utils/imageAnnotationAnswer'
import { parseQuestionImageRefs } from '../../common/utils/questionImages'
```

- [ ] **Step 2: Add reactive state**

Near existing essay canvas state, add:

```js
const imageAnnotationDrafts = ref({})
const imageAnnotationObjectNames = ref({})
const imageAnnotationUploading = ref(false)
```

Reset these in initialization with:

```js
imageAnnotationDrafts.value = {}
imageAnnotationObjectNames.value = {}
```

- [ ] **Step 3: Wire the image component**

Replace the practice page `QuestionImageList` usage with:

```vue
<QuestionImageList
  v-model="currentImageAnnotationDrafts"
  :image-urls="currentQuestion.imageUrls"
  :image-desc="currentQuestion.imageDesc"
  :annotation-object-names="currentImageAnnotationObjectNames"
  :readonly="isSubmitted"
  @dirty="markCurrentImageAnnotationDirty"
/>
```

Add computed helpers:

```js
const currentImageAnnotationDrafts = computed({
  get: () => imageAnnotationDrafts.value[currentQuestion.value?.questionId] || {},
  set: (value) => {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId) return
    imageAnnotationDrafts.value = { ...imageAnnotationDrafts.value, [questionId]: value || {} }
    markDraftChanged(questionId)
  }
})

const currentImageAnnotationObjectNames = computed(() => {
  return imageAnnotationObjectNames.value[currentQuestion.value?.questionId] || {}
})

const markCurrentImageAnnotationDirty = () => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (questionId) markDraftChanged(questionId)
}
```

- [ ] **Step 4: Upload annotation PNGs before draft save/submit**

Add:

```js
const buildImageAnnotationUploadFileName = (questionId, imageKey) => {
  const safeKey = String(imageKey || 'image').replace(/[^a-zA-Z0-9_.-]/g, '-').slice(-80)
  return `practice-image-annotation-${sessionId.value}-${questionId}-${Date.now()}-${safeKey}.png`
}

const uploadImageAnnotationsForQuestion = async (question) => {
  const questionId = Number(question?.questionId || 0)
  if (!questionId) return {}
  const drafts = imageAnnotationDrafts.value[questionId] || {}
  const uploaded = imageAnnotationObjectNames.value[questionId] || {}
  const refs = parseQuestionImageRefs(question.imageUrls)
  const nextUploaded = { ...uploaded }

  for (const item of refs) {
    const imageKey = item.objectName || item.key
    if (!imageKey || nextUploaded[imageKey]) continue
    const dataUrl = String(drafts[imageKey] || '').trim()
    if (!dataUrl) continue
    const file = dataUrlToFile(dataUrl, buildImageAnnotationUploadFileName(questionId, imageKey))
    if (!file) continue
    const response = await fileApi.upload(file)
    const objectName = String(response?.objectName || response?.data?.objectName || '').trim()
    if (!objectName) throw new Error('图片辅助线保存失败，请稍后重试')
    nextUploaded[imageKey] = objectName
  }

  imageAnnotationObjectNames.value = {
    ...imageAnnotationObjectNames.value,
    [questionId]: nextUploaded
  }
  return nextUploaded
}

const ensureImageAnnotationsUploaded = async ({ questionIds = [], silent = true, throwOnError = false } = {}) => {
  const ids = questionIds.length
    ? questionIds.map((item) => Number(item)).filter((item) => item > 0)
    : questions.value.map((question) => Number(question.questionId))
  imageAnnotationUploading.value = true
  try {
    for (const questionId of ids) {
      const question = questions.value.find((item) => Number(item.questionId) === questionId)
      await uploadImageAnnotationsForQuestion(question)
    }
  } catch (error) {
    if (!silent) showToast(error.message || '图片辅助线保存失败')
    if (throwOnError) throw error
  } finally {
    imageAnnotationUploading.value = false
  }
}
```

- [ ] **Step 5: Append image annotation markers in `buildUserAnswer`**

Wrap each branch result:

```js
const buildQuestionImageAnnotationPayload = (question, text) => {
  const questionId = Number(question?.questionId || 0)
  const objectMap = imageAnnotationObjectNames.value[questionId] || {}
  const annotations = Object.entries(objectMap).map(([imageObjectName, annotationObjectName]) => ({
    imageObjectName,
    annotationObjectName
  }))
  return buildImageAnnotationPayload(text, annotations)
}
```

At the end of `buildUserAnswer`, return `buildQuestionImageAnnotationPayload(question, answerText)` instead of raw `answerText`.

- [ ] **Step 6: Restore uploaded annotation object names from drafts**

When loading saved draft answers, call:

```js
const restoreImageAnnotationsFromAnswer = (questionId, userAnswer) => {
  const { annotations } = parseImageAnnotationPayload(userAnswer)
  if (!annotations.length) return
  imageAnnotationObjectNames.value = {
    ...imageAnnotationObjectNames.value,
    [questionId]: annotations.reduce((result, item) => {
      result[item.imageObjectName] = item.annotationObjectName
      return result
    }, {})
  }
}
```

Call it in the existing draft snapshot loop immediately after loading each `userAnswer`.

- [ ] **Step 7: Call upload before draft save and submit**

In `saveDraft`, before building payload:

```js
await ensureEssayCanvasUploaded({ questionIds, silent, throwOnError: true })
await ensureImageAnnotationsUploaded({ questionIds, silent, throwOnError: true })
```

In `submitAllAnswers`, before building payload:

```js
await ensureEssayCanvasUploaded({ silent: false, throwOnError: true })
await ensureImageAnnotationsUploaded({ silent: false, throwOnError: true })
```

- [ ] **Step 8: Commit**

```powershell
git add aishua-web/src/modules/practice/views/PracticeSessionView.vue
git commit -m "feat: save practice image annotations"
```

---

### Task 5: Exam Session Save and Submit Integration

**Files:**
- Modify: `aishua-web/src/modules/exam/views/ExamSessionView.vue`

- [ ] **Step 1: Import utilities**

Add:

```js
import {
  buildImageAnnotationPayload,
  parseImageAnnotationPayload
} from '../../common/utils/imageAnnotationAnswer'
import { parseQuestionImageRefs } from '../../common/utils/questionImages'
```

- [ ] **Step 2: Add client-state fields**

Add refs:

```js
const imageAnnotationDrafts = ref({})
const imageAnnotationObjectNames = ref({})
const imageAnnotationUploading = ref(false)
```

Include these fields in existing client cache save/load:

```js
imageAnnotationDrafts: imageAnnotationDrafts.value,
imageAnnotationObjectNames: imageAnnotationObjectNames.value
```

- [ ] **Step 3: Wire component in the exam template**

Replace the exam page `QuestionImageList` usage with:

```vue
<QuestionImageList
  v-model="currentImageAnnotationDrafts"
  :image-urls="currentQuestion.imageUrls"
  :image-desc="currentQuestion.imageDesc"
  :annotation-object-names="currentImageAnnotationObjectNames"
  @dirty="saveClientState"
/>
```

Add computed helpers identical to practice, but call `saveClientState()` instead of `markDraftChanged`.

- [ ] **Step 4: Upload annotations before submit**

Add `uploadImageAnnotationsForQuestion` and `ensureImageAnnotationsUploaded` with exam file names:

```js
const buildImageAnnotationUploadFileName = (questionId, imageKey) => {
  const safeKey = String(imageKey || 'image').replace(/[^a-zA-Z0-9_.-]/g, '-').slice(-80)
  return `exam-image-annotation-${recordId.value}-${questionId}-${Date.now()}-${safeKey}.png`
}
```

The rest of the upload logic matches Task 4, using `questions.value`, `fileApi.upload`, and `dataUrlToFile`.

- [ ] **Step 5: Append image annotation markers in `buildUserAnswer`**

Use:

```js
const buildQuestionImageAnnotationPayload = (question, text) => {
  const questionId = Number(question?.questionId || 0)
  const objectMap = imageAnnotationObjectNames.value[questionId] || {}
  const annotations = Object.entries(objectMap).map(([imageObjectName, annotationObjectName]) => ({
    imageObjectName,
    annotationObjectName
  }))
  return buildImageAnnotationPayload(text, annotations)
}
```

Return the wrapped value from `buildUserAnswer`.

- [ ] **Step 6: Restore object names after local cache load**

If cached answers already include image annotation markers, parse them with `parseImageAnnotationPayload` and initialize `imageAnnotationObjectNames`.

- [ ] **Step 7: Call upload in `submitExam`**

Before building the payload:

```js
await ensureEssayCanvasUploaded({ silent: false, throwOnError: true })
await ensureImageAnnotationsUploaded({ silent: false, throwOnError: true })
```

- [ ] **Step 8: Commit**

```powershell
git add aishua-web/src/modules/exam/views/ExamSessionView.vue
git commit -m "feat: save exam image annotations"
```

---

### Task 6: Record Detail Replay

**Files:**
- Modify: `aishua-web/src/modules/practice/views/PracticeRecordDetailView.vue`
- Modify: `aishua-web/src/modules/exam/views/ExamRecordDetailView.vue`
- Modify: `aishua-web/src/modules/exam/utils/examHelpers.js`

- [ ] **Step 1: Import the image component and parser in practice record detail**

Add:

```js
import QuestionImageList from '@/components/QuestionImageList.vue'
import {
  parseImageAnnotationPayload,
  stripImageAnnotationMarkers
} from '../../common/utils/imageAnnotationAnswer'
```

- [ ] **Step 2: Display read-only practice image annotations**

Inside the practice record title cell, after the title/type block, add:

```vue
<QuestionImageList
  v-if="record.imageUrls"
  :image-urls="record.imageUrls"
  :image-desc="record.imageDesc"
  :annotation-object-names="resolveImageAnnotationObjectNames(record.userAnswer)"
  readonly
/>
```

Add:

```js
const resolveImageAnnotationObjectNames = (userAnswer) => {
  const { annotations } = parseImageAnnotationPayload(userAnswer)
  return annotations.reduce((result, item) => {
    result[item.imageObjectName] = item.annotationObjectName
    return result
  }, {})
}
```

In `formatAnswerDisplay`, call `stripImageAnnotationMarkers(stripEssayCanvasMarker(value))`.

- [ ] **Step 3: Import and display read-only exam image annotations**

In `ExamRecordDetailView.vue`, import:

```js
import QuestionImageList from '@/components/QuestionImageList.vue'
import { parseImageAnnotationPayload } from '../../common/utils/imageAnnotationAnswer'
```

Add after the question header and before options:

```vue
<QuestionImageList
  v-if="item.imageUrls"
  :image-urls="item.imageUrls"
  :image-desc="item.imageDesc"
  :annotation-object-names="resolveImageAnnotationObjectNames(item.userAnswer)"
  readonly
/>
```

Add the same `resolveImageAnnotationObjectNames` helper.

- [ ] **Step 4: Strip image markers in exam helpers**

In `aishua-web/src/modules/exam/utils/examHelpers.js`, require or import `stripImageAnnotationMarkers` according to the file's current module style, then apply it inside `formatAnswerDisplay` and `toAnswerTokens` before parsing.

Use this transformation:

```js
const normalized = stripImageAnnotationMarkers(value)
```

Then continue with existing answer formatting/token logic.

- [ ] **Step 5: Run frontend tests**

Run:

```powershell
cd aishua-web
node tests/imageAnnotationAnswer.test.js
node tests/questionImages.test.js
```

Expected: both pass.

- [ ] **Step 6: Commit**

```powershell
git add aishua-web/src/modules/practice/views/PracticeRecordDetailView.vue `
  aishua-web/src/modules/exam/views/ExamRecordDetailView.vue `
  aishua-web/src/modules/exam/utils/examHelpers.js
git commit -m "feat: replay image annotations in records"
```

---

### Task 7: Full Verification

**Files:**
- Verify all files changed above.

- [ ] **Step 1: Run frontend utility tests**

```powershell
cd aishua-web
node tests/imageAnnotationAnswer.test.js
node tests/questionImages.test.js
node tests/questionImageObjects.test.js
```

Expected: all tests print success messages.

- [ ] **Step 2: Run frontend lint**

```powershell
cd aishua-web
npm run lint
```

Expected: command exits with code 0.

- [ ] **Step 3: Run frontend build**

```powershell
cd aishua-web
npm run build
```

Expected: command exits with code 0; existing asset-size warnings are acceptable.

- [ ] **Step 4: Run backend targeted tests**

```powershell
cd aishua-master
mvn -Dtest=AnswerJudgeSupportTest test
```

Expected: command exits with code 0.

- [ ] **Step 5: Run backend test suite**

```powershell
cd aishua-master
mvn test
```

Expected: command exits with code 0. If this fails because of existing environment/database requirements, record the failure details and keep the targeted test result.

- [ ] **Step 6: Manual browser verification**

Start the app using the project's normal local workflow. Verify:

- A practice question with `imageUrls` shows a compact image, not a full-width blank panel.
- Drawing a line on the image and saving draft uploads an annotation object and keeps the line after refresh.
- Submitting practice preserves the line in practice record detail.
- Submitting exam preserves the line in exam record detail.
- A choice or fill-in question with image annotations still judges correctly.

- [ ] **Step 7: Final status**

Check:

```powershell
git status --short
```

Expected: only intentional changes remain. Report any uncommitted files that pre-existed the worktree and were not part of this task.
