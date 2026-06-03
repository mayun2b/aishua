package zysy.iflytek.aishua.common.result;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果对象，统一列表接口的分页响应结构。
 */
@Data
public class PageResult<T> {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records == null ? Collections.emptyList() : records);
        result.setTotal(total == null ? 0L : Math.max(total, 0L));
        result.setPageNum(normalizePageNum(pageNum));
        result.setPageSize(normalizePageSize(pageSize));
        return result;
    }

    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return of(Collections.emptyList(), 0L, pageNum, pageSize);
    }

    public static int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    public static int normalizePageSize(Integer pageSize) {
        return normalizePageSize(pageSize, 100);
    }

    public static int normalizePageSize(Integer pageSize, int maxPageSize) {
        int safeMax = Math.max(maxPageSize, 1);
        if (pageSize == null || pageSize <= 0) {
            return Math.min(10, safeMax);
        }
        return Math.min(pageSize, safeMax);
    }

    public static int offset(int pageNum, int pageSize) {
        return Math.max(pageNum - 1, 0) * Math.max(pageSize, 1);
    }
}
