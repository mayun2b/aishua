package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PracticeBatchSubmitDTO {
    /**
     * Client-side draft base version for optimistic concurrency control.
     */
    private Integer baseVersion;

    @Valid
    private List<PracticeAnswerItemDTO> answers = new ArrayList<>();
}
