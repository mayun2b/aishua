import request from '../../../core/http/request'

export default {
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  download(objectName) {
    return request.get('/files/download', {
      params: { objectName },
      responseType: 'blob'
    })
  },
  getPreviewUrl(objectName) {
    return request.get('/files/preview-url', {
      params: { objectName }
    })
  }
}
