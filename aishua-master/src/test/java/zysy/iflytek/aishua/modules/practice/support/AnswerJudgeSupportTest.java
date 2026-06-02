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
