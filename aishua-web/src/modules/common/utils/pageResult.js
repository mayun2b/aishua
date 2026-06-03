const toPositiveInteger = (value, fallback) => {
  const normalized = Number(value)
  if (!Number.isFinite(normalized) || normalized <= 0) {
    return fallback
  }
  return Math.floor(normalized)
}

const normalizePageResult = (payload, fallback = {}) => {
  if (Array.isArray(payload)) {
    return {
      records: payload,
      total: payload.length,
      pageNum: toPositiveInteger(fallback.pageNum, 1),
      pageSize: toPositiveInteger(fallback.pageSize, payload.length || 1)
    }
  }

  const records = Array.isArray(payload?.records) ? payload.records : []
  return {
    records,
    total: Math.max(0, Number(payload?.total ?? records.length) || 0),
    pageNum: toPositiveInteger(payload?.pageNum ?? payload?.page ?? fallback.pageNum, 1),
    pageSize: toPositiveInteger(payload?.pageSize ?? payload?.size ?? fallback.pageSize, records.length || 1)
  }
}

module.exports = {
  normalizePageResult
}
