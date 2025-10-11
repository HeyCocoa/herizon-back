package org.example.herizon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.herizon.dto.UserRegistrationRequest;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService集成测试类
 * <p>
 * 与单元测试的区别：
 * - 启动完整的Spring Boot容器
 * - 连接真实的MySQL数据库
 * - 执行真实的数据库插入操作
 * - 使用@Transactional注解，测试后自动回滚（保持数据库干净）
 * <p>
 * 测试场景：
 * - 真实注册流程（数据库插入）
 * - 用户名唯一性验证（数据库查询）
 * - 邮箱唯一性验证（数据库查询）
 *
 * @author Kokoa
 */
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private UserRegistrationRequest validRequest;
    private BCryptPasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Initialize test data with a random suffix to avoid conflicts
        String uniqueSuffix = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        validRequest = new UserRegistrationRequest();
        validRequest.setUsername("test_user_" + uniqueSuffix);
        validRequest.setEmail("test_" + uniqueSuffix + "@example.com");
        validRequest.setPassword("herizon");
        validRequest.setQuestionnaireData(null);

        passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 测试场景：真实注册流程（数据库插入）
     * <p>
     * 验证点：
     * 1. 数据成功插入数据库
     * 2. 自动生成主键ID
     * 3. 密码使用BCrypt加密
     * 4. 默认角色为0（体验用户）
     * 5. 返回对象不包含密码
     * 6. 可以从数据库查询到新用户
     */
    @Test
    void testRegister_RealDatabaseInsert() {
        // When: 执行真实注册
        User result = userService.register(validRequest);

        // Then: 验证返回结果
        assertNotNull(result, "注册应返回用户对象");
        assertNotNull(result.getId(), "用户ID应自动生成");
        assertEquals(validRequest.getUsername(), result.getUsername(), "用户名应匹配");
        assertEquals(validRequest.getEmail(), result.getEmail(), "邮箱应匹配");
        assertEquals(0, result.getRole(), "新用户默认角色应为0");
        assertNull(result.getPasswordHash(), "返回对象不应包含密码");

        // 验证数据库中的数据
        User dbUser = userMapper.selectOneById(result.getId());
        assertNotNull(dbUser, "应能从数据库查询到新用户");
        assertEquals(validRequest.getUsername(), dbUser.getUsername(), "数据库中的用户名应匹配");
        assertEquals(validRequest.getEmail(), dbUser.getEmail(), "数据库中的邮箱应匹配");
        assertNotNull(dbUser.getPasswordHash(), "数据库中应存储加密后的密码");
        assertNotEquals(validRequest.getPassword(), dbUser.getPasswordHash(), "密码不应明文存储");

        // 验证密码加密正确性
        assertTrue(passwordEncoder.matches(validRequest.getPassword(), dbUser.getPasswordHash()),
                "加密密码应能通过BCrypt验证");

        System.out.println("✅ 注册成功，用户ID: " + result.getId());
        System.out.println("   用户名: " + dbUser.getUsername());
        System.out.println("   邮箱: " + dbUser.getEmail());
        System.out.println("   角色: " + dbUser.getRole());
        System.out.println("   注: 测试结束后数据将自动回滚");
    }

    /**
     * 测试场景：用户名重复验证（真实数据库查询）
     * <p>
     * 验证点：
     * 1. 第一次注册成功
     * 2. 使用相同用户名第二次注册失败
     * 3. 抛出明确的异常信息
     */
    @Test
    void testRegister_DuplicateUsername_RealDatabase() {
        // Given: 第一次注册成功
        User firstUser = userService.register(validRequest);
        assertNotNull(firstUser.getId(), "第一次注册应成功");

        // When & Then: 使用相同用户名再次注册
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setUsername(validRequest.getUsername()); // 相同用户名
        duplicateRequest.setEmail("different_" + System.currentTimeMillis() + "@example.com"); // 不同邮箱
        duplicateRequest.setPassword("password456");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(duplicateRequest),
                "用户名重复应抛出异常"
        );

        assertEquals("用户名已存在", exception.getMessage());

        System.out.println("✅ 用户名唯一性验证通过");
    }

    /**
     * 测试场景：邮箱重复验证（真实数据库查询）
     * <p>
     * 验证点：
     * 1. 第一次注册成功
     * 2. 使用相同邮箱第二次注册失败
     * 3. 抛出明确的异常信息
     */
    @Test
    void testRegister_DuplicateEmail_RealDatabase() {
        // Given: 第一次注册成功
        User firstUser = userService.register(validRequest);
        assertNotNull(firstUser.getId(), "第一次注册应成功");

        // When & Then: 使用相同邮箱再次注册
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setUsername("different_user_" + System.currentTimeMillis()); // 不同用户名
        duplicateRequest.setEmail(validRequest.getEmail()); // 相同邮箱
        duplicateRequest.setPassword("password456");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(duplicateRequest),
                "邮箱重复应抛出异常"
        );

        assertEquals("邮箱已被注册", exception.getMessage());

        System.out.println("✅ 邮箱唯一性验证通过");
    }

    /**
     * 测试场景：包含问卷数据的真实注册
     * <p>
     * 验证点：
     * 1. 问卷数据成功保存到数据库
     * 2. 可以从数据库查询到问卷数据
     */
    @Test
    void testRegister_WithQuestionnaireData_RealDatabase() throws Exception {
        // Given: 包含问卷数据
        String questionnaireJson = "{\"gender\":\"female\",\"occupation\":\"engineer\",\"education\":\"bachelor\"}";
        validRequest.setQuestionnaireData(questionnaireJson);

        // When: 执行注册
        User result = userService.register(validRequest);

        // Then: 验证数据库中的问卷数据
        User dbUser = userMapper.selectOneById(result.getId());
        assertNotNull(dbUser.getQuestionnaireData(), "问卷数据不应为null");
        assertEquals(
                objectMapper.readTree(questionnaireJson),
                objectMapper.readTree(dbUser.getQuestionnaireData()),
                "Questionnaire data should match regardless of formatting"
        );

        System.out.println("✅ 问卷数据保存成功");
        System.out.println("   问卷内容: " + dbUser.getQuestionnaireData());
    }

    /**
     * 测试场景：验证时间戳字段（真实数据库）
     * <p>
     * 验证点：
     * 1. created_at字段自动设置
     * 2. updated_at字段自动设置
     * 3. 时间戳存储到数据库
     */
    @Test
    void testRegister_TimestampFields_RealDatabase() {
        // When: 执行注册
        User result = userService.register(validRequest);

        // Then: 验证数据库中的时间戳
        User dbUser = userMapper.selectOneById(result.getId());
        assertNotNull(dbUser.getCreatedAt(), "创建时间不应为null");
        assertNotNull(dbUser.getUpdatedAt(), "更新时间不应为null");

        System.out.println("✅ 时间戳字段验证通过");
        System.out.println("   创建时间: " + dbUser.getCreatedAt());
        System.out.println("   更新时间: " + dbUser.getUpdatedAt());
    }

    /**
     * 测试场景：验证deleted标记（逻辑删除）
     * <p>
     * 验证点：
     * 1. 新用户的deleted字段为0（未删除）
     * 2. 逻辑删除标记正确存储到数据库
     */
    @Test
    void testRegister_DeletedFlag_RealDatabase() {
        // When: 执行注册
        User result = userService.register(validRequest);

        // Then: 验证deleted标记
        User dbUser = userMapper.selectOneById(result.getId());
        assertEquals(0, dbUser.getDeleted(), "新用户的deleted标记应为0");

        System.out.println("✅ 逻辑删除标记验证通过");
    }
}
