package zysy.iflytek.aishua.modules.ai.grading.support;

/**
 * AI 评分模块常量。
 */
public final class AiGradingConstants {
    public static final int SHORT_ANSWER_TYPE = 5;

    public static final String BIZ_TYPE_PRACTICE = "PRACTICE";
    public static final String BIZ_TYPE_EXAM = "EXAM";

    public static final String STATUS_NOT_REQUIRED = "NOT_REQUIRED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PARTIAL_FAILED = "PARTIAL_FAILED";

    public static final String TRIGGER_SUBMIT = "SUBMIT";
    public static final String TRIGGER_MANUAL_RETRY = "MANUAL_RETRY";

    private AiGradingConstants() {
    }

    public static boolean isShortAnswer(Integer questionType) {
        return Integer.valueOf(SHORT_ANSWER_TYPE).equals(questionType);
    }

    public static boolean isPendingStatus(String status) {
        return STATUS_PENDING.equals(status) || STATUS_PROCESSING.equals(status);
    }
}
