package org.example.herizon.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.herizon.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User>{
}