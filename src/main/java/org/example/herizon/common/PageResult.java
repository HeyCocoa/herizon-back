package org.example.herizon.common;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装类
 * <p>
 * 用于封装分页查询的返回结果，包含数据列表和分页信息：
 * {
 * "records": [],      // 当前页数据列表
 * "total": 100,       // 总记录数
 * "current": 1,       // 当前页码
 * "size": 10,         // 每页大小
 * "pages": 10         // 总页数
 * }
 *
 * @param <T> 数据记录的泛型类型
 * @author Kokoa
 */
@Data
public class PageResult<T>{
    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 创建分页结果对象
     *
     * @param records 当前页数据列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     * @param <T>     数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size){
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(total);
        pageResult.setCurrent(current);
        pageResult.setSize(size);
        // 计算总页数：向上取整 (total + size - 1) / size
        pageResult.setPages((total + size - 1)/size);
        return pageResult;
    }
}