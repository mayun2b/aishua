package zysy.iflytek.aishua.modules.practice.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class AnswerJudgeSupport {
    public boolean isCorrect(Integer questionType, String standardAnswer, String userAnswer) {
        if (questionType == null) {
            return false;
        }

        return switch (questionType) {
            case 1 -> judgeSingleChoice(standardAnswer, userAnswer);
            case 2 -> judgeMultipleChoice(standardAnswer, userAnswer);
            case 3 -> judgeJudgement(standardAnswer, userAnswer);
            case 4, 5 -> judgeText(standardAnswer, userAnswer);
            default -> false;
        };
    }

    private boolean judgeSingleChoice(String standardAnswer, String userAnswer) {
        return normalizeSimpleText(standardAnswer).equals(normalizeSimpleText(userAnswer));
    }

    private boolean judgeMultipleChoice(String standardAnswer, String userAnswer) {
        List<String> standardTokens = normalizeAnswerTokens(standardAnswer);
        List<String> userTokens = normalizeAnswerTokens(userAnswer);
        return standardTokens.equals(userTokens);
    }

    private boolean judgeJudgement(String standardAnswer, String userAnswer) {
        return normalizeBooleanText(standardAnswer).equals(normalizeBooleanText(userAnswer));
    }

    private boolean judgeText(String standardAnswer, String userAnswer) {
        return normalizeSimpleText(standardAnswer).equals(normalizeSimpleText(userAnswer));
    }

    private String normalizeSimpleText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace('\u3000', ' ')
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeBooleanText(String value) {
        String normalized = normalizeSimpleText(value);
        return switch (normalized) {
            case "true", "t", "1", "对", "正确", "是", "y", "yes" -> "true";
            case "false", "f", "0", "错", "错误", "否", "n", "no" -> "false";
            default -> normalized;
        };
    }

    private List<String> normalizeAnswerTokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        Set<String> tokens = new LinkedHashSet<>();
        String trimmed = value.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                JSONArray jsonArray = JSON.parseArray(trimmed);
                for (Object item : jsonArray) {
                    if (item != null) {
                        addToken(tokens, String.valueOf(item));
                    }
                }
            } catch (Exception ignored) {
                splitAndAddTokens(tokens, trimmed);
            }
        } else {
            splitAndAddTokens(tokens, trimmed);
        }

        List<String> result = new ArrayList<>(tokens);
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private void splitAndAddTokens(Set<String> tokens, String value) {
        String normalized = value
                .replace('，', ',')
                .replace('；', ',')
                .replace(';', ',')
                .replace('|', ',');
        for (String item : normalized.split("[,\\s]+")) {
            addToken(tokens, item);
        }
    }

    private void addToken(Set<String> tokens, String raw) {
        if (raw == null) {
            return;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!normalized.isEmpty()) {
            tokens.add(normalized);
        }
    }
}
