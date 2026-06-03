package zysy.iflytek.aishua.modules.directory.service;

import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.directory.entity.dto.DirectoryTagRelationUpsertDTO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTagRelationVO;

/**
 * 目录-考点关系服务接口。
 */
public interface DirectoryTagRelationService {
    PageResult<DirectoryTagRelationVO> listRelations(
            Long subjectId,
            Long directoryId,
            Long tagId,
            Integer isEnabled,
            String keyword,
            Integer pageNum,
            Integer pageSize
    );

    DirectoryTagRelationVO createRelation(DirectoryTagRelationUpsertDTO upsertDTO);

    DirectoryTagRelationVO updateRelation(Long id, DirectoryTagRelationUpsertDTO upsertDTO);

    DirectoryTagRelationVO updateEnabled(Long id, Integer enabled);

    void deleteRelation(Long id);
}
