package org.example.herizon.mapper;

import com.mybatisflex.core.BaseMapper;
import org.example.herizon.entity.PollOption;

/**
 * 投票选项Mapper接口
 * <p>
 * 继承MyBatis-Flex的BaseMapper，提供基础的CRUD操作
 * <p>
 * 常用查询场景：
 * 1. 根据帖子ID查询所有投票选项：
 *    QueryWrapper.create()
 *        .where("post_id = ?", postId)
 *        .and("deleted = 0")
 *        .orderBy("display_order ASC")
 * <p>
 * 2. 根据选项ID查询选项详情：
 *    selectOneById(optionId)
 * <p>
 * 3. 批量插入投票选项：
 *    insertBatch(pollOptionList)
 * <p>
 * 4. 更新投票选项文本：
 *    update(pollOption)
 * <p>
 * 5. 逻辑删除投票选项：
 *    QueryWrapper.create()
 *        .set("deleted", 1)
 *        .where("id = ?", optionId)
 *
 * @author Kokoa
 * @since 2025-10-01
 */
public interface PollOptionMapper extends BaseMapper<PollOption> {
    // MyBatis-Flex的BaseMapper已提供所有基础CRUD方法
    // 如需自定义SQL查询，可在此添加方法并使用@Select/@Insert等注解
}
