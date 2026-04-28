package zysy.iflytek.aishua.modules.directory.service;

import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;

import java.util.List;

public interface DirectoryService {
    List<DirectoryTreeVO> listTreeBySubject(Long subjectId);

    DirectoryTreeVO createDirectory(DirectoryUpsertDTO directoryUpsertDTO);

    DirectoryTreeVO updateDirectory(Long id, DirectoryUpsertDTO directoryUpsertDTO);

    void deleteDirectory(Long id);
}
