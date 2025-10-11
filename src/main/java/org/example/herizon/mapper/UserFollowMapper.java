package org.example.herizon.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.herizon.entity.UserFollow;

/**
 * 用户关注关系Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
