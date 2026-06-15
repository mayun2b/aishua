import request from '../../../core/http/request'

const VIDEO_SEARCH_TIMEOUT = 30000

export default {
  /**
   * 搜索薄弱知识点教学视频
   * @param {Object} data - 请求参数
   * @param {string} data.knowledgePoint - 知识点名称
   * @param {string} [data.subject] - 学科名称
   * @param {number} [data.subjectId] - 学科 ID
   * @param {string} [data.grade] - 年级
   * @param {number} [data.limit] - 返回数量
   */
  search(data) {
    return request.post('/ai/video-search', data, { timeout: VIDEO_SEARCH_TIMEOUT })
  }
}
