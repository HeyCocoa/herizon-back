package org.example.herizon.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.herizon.entity.Feedback;

/**
 * 反馈数据访问接口
 *
 * @author Claude Code
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}