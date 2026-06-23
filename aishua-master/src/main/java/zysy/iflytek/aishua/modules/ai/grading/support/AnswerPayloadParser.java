package zysy.iflytek.aishua.modules.ai.grading.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 解析前端写入 userAnswer 的文本、手写画布和题图标注标记。
 */
@Component
public class AnswerPayloadParser {
    private static final String CANVAS_PREFIX = "[[canvas:";
    private static final String IMAGE_ANNOTATION_PREFIX = "[[image-annotations:";
    private static final String MARKER_SUFFIX = "]]";

    public ParsedAnswer parse(String rawAnswer) {
        String raw = rawAnswer == null ? "" : rawAnswer;
        ImageAnnotationParseResult imageParseResult = parseImageAnnotations(raw);
        CanvasParseResult canvasParseResult = parseCanvas(imageParseResult.text());
        return new ParsedAnswer(
                canvasParseResult.text(),
                canvasParseResult.canvasObjectName(),
                imageParseResult.annotations()
        );
    }

    private ImageAnnotationParseResult parseImageAnnotations(String raw) {
        String value = raw == null ? "" : raw;
        String lowerValue = value.toLowerCase(Locale.ROOT);
        String lowerPrefix = IMAGE_ANNOTATION_PREFIX.toLowerCase(Locale.ROOT);
        StringBuilder stripped = new StringBuilder();
        List<ImageAnnotation> annotations = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int fromIndex = 0;
        int copiedFrom = 0;

        while (fromIndex < value.length()) {
            int startIndex = lowerValue.indexOf(lowerPrefix, fromIndex);
            if (startIndex < 0) {
                break;
            }

            int jsonStartIndex = startIndex + IMAGE_ANNOTATION_PREFIX.length();
            MarkerEnd markerEnd = findImageAnnotationMarkerEnd(value, jsonStartIndex);
            if (markerEnd == null) {
                fromIndex = jsonStartIndex;
                continue;
            }

            stripped.append(value, copiedFrom, startIndex);
            parseAnnotationJson(markerEnd.jsonText(), annotations, seen);
            copiedFrom = markerEnd.endIndex();
            if (stripped.length() > 0
                    && copiedFrom < value.length()
                    && Character.isWhitespace(stripped.charAt(stripped.length() - 1))
                    && Character.isWhitespace(value.charAt(copiedFrom))) {
                copiedFrom = skipWhitespace(value, copiedFrom);
            }
            fromIndex = markerEnd.endIndex();
        }

        stripped.append(value.substring(copiedFrom));
        return new ImageAnnotationParseResult(stripped.toString().trim(), annotations);
    }

    private MarkerEnd findImageAnnotationMarkerEnd(String value, int jsonStartIndex) {
        int jsonEndIndex = findJsonArrayEnd(value, jsonStartIndex);
        if (jsonEndIndex >= 0) {
            int suffixStartIndex = skipWhitespace(value, jsonEndIndex);
            if (value.startsWith(MARKER_SUFFIX, suffixStartIndex)) {
                return new MarkerEnd(
                        suffixStartIndex + MARKER_SUFFIX.length(),
                        value.substring(jsonStartIndex, jsonEndIndex)
                );
            }
        }

        int fallbackSuffixIndex = findFallbackMarkerSuffixIndex(value, jsonStartIndex);
        if (fallbackSuffixIndex < 0) {
            return null;
        }
        return new MarkerEnd(
                fallbackSuffixIndex + MARKER_SUFFIX.length(),
                value.substring(jsonStartIndex, fallbackSuffixIndex)
        );
    }

    private int findJsonArrayEnd(String value, int startIndex) {
        int index = skipWhitespace(value, startIndex);
        if (index >= value.length() || value.charAt(index) != '[') {
            return -1;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (; index < value.length(); index++) {
            char current = value.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (current == '\\') {
                    escaped = true;
                    continue;
                }
                if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }
            if (current == '[' || current == '{') {
                depth++;
                continue;
            }
            if (current == ']' || current == '}') {
                depth--;
                if (depth == 0) {
                    return index + 1;
                }
                if (depth < 0) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private int findFallbackMarkerSuffixIndex(String value, int startIndex) {
        int fromIndex = startIndex;
        while (fromIndex < value.length()) {
            int suffixIndex = value.indexOf(MARKER_SUFFIX, fromIndex);
            if (suffixIndex < 0) {
                return -1;
            }
            int suffixEndIndex = suffixIndex + MARKER_SUFFIX.length();
            if (suffixEndIndex >= value.length() || Character.isWhitespace(value.charAt(suffixEndIndex))) {
                return suffixIndex;
            }
            fromIndex = suffixIndex + 1;
        }
        return -1;
    }

    private int skipWhitespace(String value, int startIndex) {
        int index = startIndex;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private void parseAnnotationJson(String json, List<ImageAnnotation> annotations, Set<String> seen) {
        if (!StringUtils.hasText(json)) {
            return;
        }
        try {
            JSONArray array = JSON.parseArray(json);
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                String imageObjectName = normalize(item.getString("imageObjectName"));
                String annotationObjectName = normalize(item.getString("annotationObjectName"));
                if (!StringUtils.hasText(imageObjectName) || !StringUtils.hasText(annotationObjectName)) {
                    continue;
                }
                String key = imageObjectName + "\n" + annotationObjectName;
                if (seen.add(key)) {
                    annotations.add(new ImageAnnotation(imageObjectName, annotationObjectName));
                }
            }
        } catch (Exception ignored) {
            // 标记损坏时跳过，后续评分仍可使用文本和画布内容。
        }
    }

    private CanvasParseResult parseCanvas(String textWithCanvasMarker) {
        String raw = textWithCanvasMarker == null ? "" : textWithCanvasMarker.trim();
        int markerStart = raw.lastIndexOf(CANVAS_PREFIX);
        int markerEnd = raw.lastIndexOf(MARKER_SUFFIX);
        if (markerStart < 0 || markerEnd < markerStart) {
            return new CanvasParseResult(raw, "");
        }

        String canvasObjectName = raw.substring(markerStart + CANVAS_PREFIX.length(), markerEnd).trim();
        String text = raw.substring(0, markerStart).trim();
        return new CanvasParseResult(text, canvasObjectName);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record ImageAnnotationParseResult(String text, List<ImageAnnotation> annotations) {
    }

    private record MarkerEnd(int endIndex, String jsonText) {
    }

    private record CanvasParseResult(String text, String canvasObjectName) {
    }

    public record ParsedAnswer(String text, String canvasObjectName, List<ImageAnnotation> annotations) {
        public boolean hasEvidence() {
            return StringUtils.hasText(text)
                    || StringUtils.hasText(canvasObjectName)
                    || (annotations != null && !annotations.isEmpty());
        }
    }

    public record ImageAnnotation(String imageObjectName, String annotationObjectName) {
    }
}
