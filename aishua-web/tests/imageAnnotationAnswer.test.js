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
assert.equal(buildImageAnnotationPayload('  B  ', []), '  B  ')
assert.equal(stripImageAnnotationMarkers('B'), 'B')
assert.equal(stripImageAnnotationMarkers(' B '), ' B ')
assert.equal(parseImageAnnotationPayload('bad [[image-annotations:not-json]]').text, 'bad')
assert.equal(
  stripImageAnnotationMarkers('bad [[image-annotations:not]]]json]] tail'),
  'bad tail'
)
assert.equal(
  parseImageAnnotationPayload('bad [[image-annotations:not]]]json]] tail').text,
  'bad tail'
)

const bracketPayload = buildImageAnnotationPayload('C', [
  { imageObjectName: 'q]]]x.png', annotationObjectName: 'anno]]]x.png' }
])
assert.equal(stripImageAnnotationMarkers(bracketPayload), 'C')
assert.deepEqual(parseImageAnnotationPayload(bracketPayload), {
  text: 'C',
  annotations: [
    { imageObjectName: 'q]]]x.png', annotationObjectName: 'anno]]]x.png' }
  ]
})

const duplicateMarker = buildImageAnnotationPayload('', [
  { imageObjectName: 'q.png', annotationObjectName: 'anno.png' }
])
const duplicatePayload = `D\n${duplicateMarker}\n${duplicateMarker}`
assert.deepEqual(parseImageAnnotationPayload(duplicatePayload).annotations, [
  { imageObjectName: 'q.png', annotationObjectName: 'anno.png' }
])

console.log('imageAnnotationAnswer tests passed')
