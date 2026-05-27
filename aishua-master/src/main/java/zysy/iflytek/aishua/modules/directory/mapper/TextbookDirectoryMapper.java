package zysy.iflytek.aishua.modules.directory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;

/**
 * 目录数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface TextbookDirectoryMapper extends BaseMapper<TextbookDirectory> {
}
