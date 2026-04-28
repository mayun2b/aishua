package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PracticeBatchSubmitDTO {
    @Valid
    private List<PracticeAnswerItemDTO> answers = new ArrayList<>();
}
