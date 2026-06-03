# Question Image MinIO Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace manual question image URL entry in the teacher question form with MinIO image upload while saving object names in the existing `imageUrls` field.

**Architecture:** Keep the backend and database unchanged. Add a small frontend utility for parsing, normalizing, and serializing MinIO object names, then wire `AdminQuestionManagementView.vue` to use `fileApi.upload(file)` and manage an object-name list in the form.

**Tech Stack:** Vue 3 Composition API, existing Axios wrapper, existing MinIO file API, Node `assert` for pure function tests, Vue CLI build/lint.

---

## File Structure

- Create: `aishua-web/src/modules/admin/utils/questionImageObjects.js`
  - Owns object-name parsing, de-duplication, serialization, and upload response extraction.
- Create: `aishua-web/tests/questionImageObjects.test.js`
  - Exercises the pure utility functions with Node `assert`.
- Modify: `aishua-web/src/modules/admin/views/AdminQuestionManagementView.vue`
  - Replaces the manual image URL input with upload controls and object-name list UI.
  - Imports `fileApi` and the new utility functions.
  - Blocks submit while image uploads are in progress.
- No backend files change.

---

### Task 1: Add Object-Name Utility Test

**Files:**
- Create: `aishua-web/tests/questionImageObjects.test.js`
- Create later: `aishua-web/src/modules/admin/utils/questionImageObjects.js`

- [ ] **Step 1: Write the failing test**

Create `aishua-web/tests/questionImageObjects.test.js`:

```javascript
const assert = require('assert')

const {
  appendImageObjectNames,
  buildImageObjectNamesText,
  parseImageObjectNames,
  resolveUploadObjectName
} = require('../src/modules/admin/utils/questionImageObjects')

assert.deepStrictEqual(
  parseImageObjectNames(' 2026-06-02/a.png, ,2026-06-02/b.png,2026-06-02/a.png '),
  ['2026-06-02/a.png', '2026-06-02/b.png']
)

assert.deepStrictEqual(
  appendImageObjectNames(['old/a.png'], ['new/b.png', 'old/a.png', '  ']),
  ['old/a.png', 'new/b.png']
)

assert.strictEqual(
  buildImageObjectNamesText([' old/a.png ', '', 'new/b.png', 'old/a.png']),
  'old/a.png,new/b.png'
)

assert.strictEqual(buildImageObjectNamesText([]), null)
assert.strictEqual(resolveUploadObjectName({ objectName: '2026-06-02/file.png' }), '2026-06-02/file.png')
assert.strictEqual(resolveUploadObjectName({ data: { objectName: 'wrapped/file.png' } }), 'wrapped/file.png')
assert.strictEqual(resolveUploadObjectName({ message: 'ok' }), '')

console.log('questionImageObjects tests passed')
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd aishua-web
node tests/questionImageObjects.test.js
```

Expected: FAIL with `Cannot find module '../src/modules/admin/utils/questionImageObjects'`.

- [ ] **Step 3: Implement the utility**

Create `aishua-web/src/modules/admin/utils/questionImageObjects.js`:

```javascript
const normalizeImageObjectName = (value) => {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const normalizeImageObjectNames = (values) => {
  const result = []
  const seen = new Set()

  for (const value of values || []) {
    const objectName = normalizeImageObjectName(value)
    if (!objectName || seen.has(objectName)) {
      continue
    }
    seen.add(objectName)
    result.push(objectName)
  }

  return result
}

const parseImageObjectNames = (imageUrls) => {
  if (imageUrls == null) {
    return []
  }
  return normalizeImageObjectNames(String(imageUrls).split(','))
}

const appendImageObjectNames = (currentNames, nextNames) => {
  return normalizeImageObjectNames([...(currentNames || []), ...(nextNames || [])])
}

const buildImageObjectNamesText = (objectNames) => {
  const normalized = normalizeImageObjectNames(objectNames)
  return normalized.length ? normalized.join(',') : null
}

const resolveUploadObjectName = (response) => {
  return normalizeImageObjectName(response?.objectName ?? response?.data?.objectName)
}

module.exports = {
  appendImageObjectNames,
  buildImageObjectNamesText,
  parseImageObjectNames,
  resolveUploadObjectName
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd aishua-web
node tests/questionImageObjects.test.js
```

Expected: PASS and output `questionImageObjects tests passed`.

---

### Task 2: Wire Utility Into Question Form

**Files:**
- Modify: `aishua-web/src/modules/admin/views/AdminQuestionManagementView.vue`

- [ ] **Step 1: Import upload API and utility functions**

Add imports in the script block:

```javascript
import fileApi from '../../common/api/file'
import {
  appendImageObjectNames,
  buildImageObjectNamesText,
  parseImageObjectNames,
  resolveUploadObjectName
} from '../utils/questionImageObjects'
```

- [ ] **Step 2: Add image upload state**

Add refs near existing form state:

```javascript
const imageObjectNames = ref([])
const imageUploading = ref(false)
```

- [ ] **Step 3: Sync image state from form payload**

Update `fillForm(payload)` so image object names are parsed whenever a form is opened or reset:

```javascript
form.imageUrls = payload.imageUrls
imageObjectNames.value = parseImageObjectNames(payload.imageUrls)
```

- [ ] **Step 4: Add image upload handlers**

Add these functions near other form handlers:

```javascript
const handleImageUpload = async (event) => {
  const files = Array.from(event.target.files || [])
  event.target.value = ''

  if (!files.length) {
    return
  }

  const imageFiles = []
  for (const file of files) {
    if (!file.type || !file.type.startsWith('image/')) {
      showToast('只能上传图片文件')
      continue
    }
    imageFiles.push(file)
  }

  if (!imageFiles.length) {
    return
  }

  imageUploading.value = true
  const uploadedNames = []

  try {
    for (const file of imageFiles) {
      const response = await fileApi.upload(file)
      const objectName = resolveUploadObjectName(response)
      if (!objectName) {
        throw new Error('图片上传失败：未返回对象名')
      }
      uploadedNames.push(objectName)
    }

    imageObjectNames.value = appendImageObjectNames(imageObjectNames.value, uploadedNames)
    form.imageUrls = buildImageObjectNamesText(imageObjectNames.value) || ''
    showToast(uploadedNames.length > 1 ? `已上传 ${uploadedNames.length} 张图片` : '图片上传成功')
  } catch (error) {
    if (uploadedNames.length) {
      imageObjectNames.value = appendImageObjectNames(imageObjectNames.value, uploadedNames)
      form.imageUrls = buildImageObjectNamesText(imageObjectNames.value) || ''
    }
    showToast(error.message || '图片上传失败')
  } finally {
    imageUploading.value = false
  }
}

const removeImageObjectName = (objectName) => {
  imageObjectNames.value = imageObjectNames.value.filter((item) => item !== objectName)
  form.imageUrls = buildImageObjectNamesText(imageObjectNames.value) || ''
}
```

- [ ] **Step 5: Use serialized image object names on submit**

Before building `payload` in `submitForm`, add:

```javascript
if (imageUploading.value) {
  showToast('图片上传中，请稍后提交')
  return
}

const imageUrlsText = buildImageObjectNamesText(imageObjectNames.value)
```

Then set:

```javascript
imageUrls: imageUrlsText,
```

- [ ] **Step 6: Run existing utility test**

Run:

```bash
cd aishua-web
node tests/questionImageObjects.test.js
```

Expected: PASS and output `questionImageObjects tests passed`.

---

### Task 3: Replace Manual Image URL UI

**Files:**
- Modify: `aishua-web/src/modules/admin/views/AdminQuestionManagementView.vue`

- [ ] **Step 1: Replace the old image URL label**

Replace the old image URL `<label>` with:

```vue
<div class="image-upload-field">
  <div class="field-heading">
    <span>题目图片</span>
    <label class="ghost small upload-button">
      {{ imageUploading ? '上传中...' : '上传图片' }}
      <input
        type="file"
        accept="image/*"
        multiple
        :disabled="imageUploading"
        @change="handleImageUpload"
      />
    </label>
  </div>
  <p class="field-tip">图片会上传至 MinIO，题目保存 MinIO 对象名。</p>
  <div v-if="imageObjectNames.length" class="image-object-list">
    <div v-for="objectName in imageObjectNames" :key="objectName" class="image-object-item">
      <span :title="objectName">{{ objectName }}</span>
      <button type="button" class="danger small" @click="removeImageObjectName(objectName)">移除</button>
    </div>
  </div>
  <p v-else class="field-tip">暂未上传题目图片</p>
</div>
```

- [ ] **Step 2: Disable form submit while uploading**

Update the submit button:

```vue
<button type="submit" :disabled="submitting || imageUploading">
```

- [ ] **Step 3: Add scoped styles**

Add styles near existing form field styles:

```css
.image-upload-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.upload-button {
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.upload-button input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.image-object-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-object-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.28);
}

.image-object-item span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

- [ ] **Step 4: Run frontend lint**

Run:

```bash
cd aishua-web
npm run lint
```

Expected: exit 0.

---

### Task 4: Build And Manual Browser Verification

**Files:**
- No new files.

- [ ] **Step 1: Run production build**

Run:

```bash
cd aishua-web
npm run build
```

Expected: exit 0 and generated build output.

- [ ] **Step 2: Start dev server if build passes**

Run:

```bash
cd aishua-web
npm run serve -- --port 8080
```

Expected: dev server starts at `http://localhost:8080/`.

- [ ] **Step 3: Browser smoke check**

Open `http://localhost:8080/admin/questions`.

Expected:
- The question form contains a “题目图片” upload area.
- The submit button is disabled while image uploads are running.
- Existing object names are shown as rows with remove buttons.

---

## Self-Review

- Spec coverage: The plan covers upload entry, MinIO object-name persistence, editing existing values, removal without MinIO delete, upload failure behavior, and no backend/database changes.
- Placeholder scan: No `TBD`, `TODO`, or unspecified implementation steps remain.
- Type consistency: The utility returns arrays of strings or nullable serialized text; Vue state uses `imageObjectNames` as `ref([])` and serializes back into `imageUrls`.
