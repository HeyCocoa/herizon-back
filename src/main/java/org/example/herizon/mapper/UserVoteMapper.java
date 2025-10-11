package org.example.herizon.mapper;

import com.mybatisflex.core.BaseMapper;
import org.example.herizon.entity.UserVote;

/**
 * 用户投票记录Mapper接口
 * <p>
 * 继承MyBatis-Flex的BaseMapper，提供基础的CRUD操作
 * <p>
 * 常用查询场景：
 * 1. 查询用户是否已投票：
 *    QueryWrapper.create()
 *        .where("user_id = ?", userId)
 *        .and("post_id = ?", postId)
 *        .and("deleted = 0")
 * <p>
 * 2. 获取用户的投票选择：
 *    QueryWrapper.create()
 *        .select("option_id")
 *        .where("user_id = ?", userId)
 *        .and("post_id = ?", postId)
 *        .and("deleted = 0")
 * <p>
 * 3. 统计某选项的投票数：
 *    QueryWrapper.create()
 *        .where("option_id = ?", optionId)
 *        .and("deleted = 0")
 *    然后调用 selectCountByQuery()
 * <p>
 * 4. 统计投票帖的总投票数：
 *    QueryWrapper.create()
 *        .where("post_id = ?", postId)
 *        .and("deleted = 0")
 *    然后调用 selectCountByQuery()
 * <p>
 * 5. 撤回投票（逻辑删除）：
 *    QueryWrapper updateWrapper = QueryWrapper.create()
 *        .set("deleted", 1)
 *        .where("user_id = ?", userId)
 *        .and("post_id = ?", postId)
 *        .and("deleted = 0")
 * <p>
 * 业务规则：
 * - 唯一约束 uk_user_post(user_id, post_id) 确保每人每帖只能有一个有效投票
 * - 撤回投票：设置deleted=1，允许用户重新投票
 * - 切换投票：先撤回原投票，再创建新投票记录
 *
 * @author Kokoa
 * @since 2025-10-01
 */
public interface UserVoteMapper extends BaseMapper<UserVote> {
    // MyBatis-Flex的BaseMapper已提供所有基础CRUD方法
    // 如需自定义SQL查询（如复杂的统计查询），可在此添加方法并使用@Select注解
}
