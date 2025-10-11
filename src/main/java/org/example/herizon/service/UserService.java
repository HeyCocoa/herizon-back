package org.example.herizon.service;

import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.dto.*;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserActionMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户管理服务类
 * <p>
 * 提供用户相关的核心业务功能：
 * - 用户注册和身份验证
 * - 用户登录和会话管理
 * - 用户资料查询和更新
 * - 用户角色管理和权限验证
 *
 * @author Kokoa
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private FollowService followService;

    /**
     * 密码加密器，使用BCrypt算法
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     * <p>
     * 创建新用户账户，默认角色为体验用户(role=0)
     * 注册时会验证用户名和邮箱的唯一性
     *
     * @param request 注册请求，包含用户基本信息和问卷数据
     * @return 注册成功的用户实体
     * @throws RuntimeException 当用户名或邮箱已存在时抛出
     */
    @Transactional
    public User register(UserRegistrationRequest request) {
        // 验证用户名唯一性
        if (!isUsernameAvailable(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 验证邮箱唯一性
        if (!isEmailAvailable(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // 使用BCrypt加密密码，保证安全性
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(0); // 新用户默认为体验用户
        user.setQuestionnaireData(request.getQuestionnaireData());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);

        // 保存到数据库
        userMapper.insert(user);

        // 返回用户信息（不包含密码）
        user.setPasswordHash(null);
        return user;
    }

    /**
     * 用户登录
     * <p>
     * 验证用户凭证并生成会话token
     * 支持使用用户名或邮箱登录
     *
     * @param request 登录请求，包含登录标识和密码
     * @return 登录成功的用户资料DTO，包含会话token
     * @throws RuntimeException 当凭证无效时抛出
     */
    public UserProfileDTO login(UserLoginRequest request) {
        // 根据登录标识查找用户（支持用户名或邮箱）
        User user = findUserByIdentifier(request.getActualIdentifier());

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        // 生成会话token（简单实现，实际项目中应使用JWT等标准方案）
        String token = generateToken(user.getId());

        // 转换为用户资料DTO并设置token
        UserProfileDTO profile = convertToProfileDTO(user, true);
        profile.setToken(token);

        return profile;
    }

    /**
     * 获取用户公开资料
     * <p>
     * 查询指定用户的公开资料信息，不包含敏感数据
     *
     * @param userId 用户ID
     * @return 用户公开资料DTO，如果用户不存在返回null
     */
    public UserProfileDTO getUserProfile(Long userId, Long viewerUserId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            return null;
        }
        UserProfileDTO profile = convertToProfileDTO(user, false);
        if (viewerUserId != null && !viewerUserId.equals(userId)) {
            profile.setIsFollowing(followService.isFollowing(viewerUserId, userId));
            profile.setIsMutualFollow(followService.isFollowing(userId, viewerUserId));
        }
        return profile;
    }

    /**
     * 获取当前用户完整资料
     * <p>
     * 查询当前登录用户的完整资料，包含私有信息
     *
     * @param currentUserId 当前用户ID
     * @return 当前用户的完整资料DTO
     */
    public UserProfileDTO getCurrentUserProfile(Long currentUserId) {
        User user = userMapper.selectOneById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToProfileDTO(user, true);
    }

    /**
     * 更新用户资料
     * <p>
     * 用户更新自己的基本信息，不能修改敏感字段如角色
     *
     * @param currentUserId 当前用户ID
     * @param request       更新请求
     * @return 更新后的用户资料DTO
     */
    @Transactional
    public UserProfileDTO updateUserProfile(Long currentUserId, UpdateProfileRequest request) {
        User user = userMapper.selectOneById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean changed = false;

        if (request.getNickname() != null) {
            String trimmedNickname = request.getNickname().trim();
            if (!trimmedNickname.isEmpty() && !trimmedNickname.equals(user.getNickname())) {
                if (trimmedNickname.length() < 2 || trimmedNickname.length() > 20) {
                    throw new RuntimeException("昵称长度需要在2-20个字符之间");
                }
                user.setNickname(trimmedNickname);
                changed = true;
            }
        }

        if (request.getAvatar() != null) {
            String trimmedAvatar = request.getAvatar().trim();
            String currentAvatar = user.getAvatar() == null ? "" : user.getAvatar();
            if (!trimmedAvatar.equals(currentAvatar)) {
                user.setAvatar(trimmedAvatar.isEmpty() ? null : trimmedAvatar);
                changed = true;
            }
        }

        if (changed) {
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }

        return convertToProfileDTO(user, true);
    }

    /**
     * 申请身份认证
     * <p>
     * 体验用户提交身份认证申请，更新问卷数据
     * 实际审核流程需要管理员手动处理
     *
     * @param currentUserId     当前用户ID
     * @param questionnaireData 身份认证问卷数据
     */
    @Transactional
    public void applyVerification(Long currentUserId, String questionnaireData) {
        User user = userMapper.selectOneById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getRole() != 0) {
            throw new RuntimeException("只有体验用户可以申请身份认证");
        }

        // 更新问卷数据，等待管理员审核
        user.setQuestionnaireData(questionnaireData);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 检查用户名是否可用
     *
     * @param username 待检查的用户名
     * @return true=可用，false=已占用
     */
    public boolean isUsernameAvailable(String username) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("username = ?", username)
                .and("deleted = 0");
        return userMapper.selectCountByQuery(queryWrapper) == 0;
    }

    /**
     * 检查邮箱是否可用
     *
     * @param email 待检查的邮箱
     * @return true=可用，false=已注册
     */
    public boolean isEmailAvailable(String email) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("email = ?", email)
                .and("deleted = 0");
        return userMapper.selectCountByQuery(queryWrapper) == 0;
    }

    /**
     * 根据登录标识查找用户
     * <p>
     * 支持用户名或邮箱作为登录标识
     *
     * @param identifier 登录标识（用户名或邮箱）
     * @return 用户实体，如果不存在返回null
     */
    private User findUserByIdentifier(String identifier) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("(username = ? OR email = ?)", identifier, identifier)
                .and("deleted = 0");
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 生成会话token
     * <p>
     * 简单token实现
     *
     * @param userId 用户ID
     * @return 会话token
     */
    private String generateToken(Long userId) {
        return "token_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成简单Token（用于微信登录）
     */
    private String generateSimpleToken(Long userId) {
        return "wx_" + userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // 简单验证：检查token格式
        return token.startsWith("wx_") || token.startsWith("token_");
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String oldToken) {
        if (!validateToken(oldToken)) {
            return null;
        }

        // 从token中提取用户ID
        try {
            String[] parts = oldToken.split("_");
            if (parts.length >= 2) {
                Long userId = Long.parseLong(parts[1]);
                return generateSimpleToken(userId);
            }
        } catch (Exception e) {
            System.err.println("Token解析失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 根据Token获取当前用户信息
     */
    public WechatLoginResponse.UserInfo getCurrentUserByToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        try {
            String[] parts = token.split("_");
            if (parts.length >= 2) {
                Long userId = Long.parseLong(parts[1]);
                return getCurrentUserInfo(userId);
            }
        } catch (Exception e) {
            System.err.println("从Token获取用户信息失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 将用户实体转换为资料DTO
     *
     * @param user        用户实体
     * @param isFullProfile 是否返回完整资料（包含私有信息）
     * @return 用户资料DTO
     */
    private UserProfileDTO convertToProfileDTO(User user, boolean isFullProfile) {
        UserProfileDTO dto = new UserProfileDTO();
        BeanUtils.copyProperties(user, dto);

        // 设置是否为完整资料标识
        dto.setIsFullProfile(isFullProfile);

        // 隐藏密码
        dto.setToken(null);

        // 如果不是完整资料，隐藏私有信息
        if (!isFullProfile) {
            dto.setEmail(null);
            dto.setQuestionnaireData(null);
        }

        // 计算用户发帖数量
        QueryWrapper postCountQuery = QueryWrapper.create()
                .where("user_id = ?", user.getId())
                .and("status = 0")
                .and("deleted = 0");
        int postCount = (int) postMapper.selectCountByQuery(postCountQuery);
        dto.setPostCount(postCount);

        // 计算用户获得的总点赞数
        // 重要：MyBatis-Flex不支持表别名，必须使用完整表名
        QueryWrapper likeCountQuery = QueryWrapper.create()
                .select("COUNT(*)")
                .from("user_actions")
                .leftJoin("posts").on("user_actions.target_id = posts.id")
                .where("posts.user_id = ?", user.getId())
                .and("user_actions.target_type = 'post'")
                .and("user_actions.action_type = 0")
                .and("user_actions.deleted = 0");
        int totalLikes = (int) userActionMapper.selectCountByQuery(likeCountQuery);
        dto.setTotalLikes(totalLikes);

        // ������ע/��˿����
        dto.setFollowingCount(followService.countFollowing(user.getId()));
        dto.setFollowersCount(followService.countFollowers(user.getId()));
        dto.setIsFollowing(Boolean.FALSE);
        dto.setIsMutualFollow(Boolean.FALSE);

        return dto;
    }

    /**
     * 微信登录
     * <p>
     * 通过微信小程序登录，支持自动注册新用户
     * 流程：
     * 1. 调用微信API验证code，获取openid和session_key
     * 2. 根据openid查找用户，如果不存在则创建新用户
     * 3. 更新用户的session_key和基本信息
     * 4. 生成会话token并返回用户资料
     *
     * @param request 微信登录请求，包含code和用户基本信息
     * @return 登录成功的用户资料DTO，包含会话token
     * @throws RuntimeException 当微信验证失败或其他错误时抛出
     */
    @Transactional
    public UserProfileDTO wechatLogin(WechatLoginRequest request) {
        // 验证请求参数
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("微信登录凭证不能为空");
        }

        try {
            // 步骤1: 调用微信API验证code
            Code2SessionResponse wechatResponse = wechatService.code2Session(request.getCode());

            // 步骤2: 根据openid查找用户
            User existingUser = findUserByWechatOpenid(wechatResponse.getOpenid());

            User user;
            if (existingUser != null) {
                // 用户已存在，更新信息
                user = updateWechatUserInfo(existingUser, wechatResponse, request);
            } else {
                // 用户不存在，创建新用户
                user = createWechatUser(wechatResponse, request);
            }

            // 步骤3: 生成会话token
            String token = generateToken(user.getId());

            // 步骤4: 转换为用户资料DTO并返回
            UserProfileDTO profile = convertToProfileDTO(user, true);
            profile.setToken(token);

            return profile;

        } catch (Exception e) {
            // 记录错误日志
            System.err.println("微信登录失败: " + e.getMessage());

            // 根据异常类型提供友好的错误信息
            if (e instanceof RuntimeException) {
                throw e;
            } else {
                throw new RuntimeException("微信登录失败，请重试", e);
            }
        }
    }

    /**
     * 绑定微信账号
     * <p>
     * 已有用户绑定微信账号，用于后续的微信登录
     * 验证用户身份后，将微信信息关联到用户账户
     *
     * @param currentUserId 当前登录用户ID
     * @param request       微信绑定请求，包含微信code
     * @throws RuntimeException 当绑定失败时抛出
     */
    @Transactional
    public void bindWechat(Long currentUserId, WechatLoginRequest request) {
        // 验证用户是否存在
        User user = userMapper.selectOneById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证微信code
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("微信登录凭证不能为空");
        }

        try {
            // 调用微信API验证code
            Code2SessionResponse wechatResponse = wechatService.code2Session(request.getCode());

            // 检查该微信账号是否已绑定其他用户
            User existingWechatUser = findUserByWechatOpenid(wechatResponse.getOpenid());
            if (existingWechatUser != null && !existingWechatUser.getId().equals(currentUserId)) {
                throw new RuntimeException("该微信账号已绑定其他用户");
            }

            // 更新用户的微信信息
            user.setWechatOpenid(wechatResponse.getOpenid());
            user.setWechatUnionid(wechatResponse.getUnionid());
            user.setWechatSessionKey(wechatResponse.getSessionKey());

            // 更新用户基本信息（如果提供）
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname());
            }
            if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
                user.setAvatar(request.getAvatar());
            }

            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);

        } catch (Exception e) {
            // 记录错误日志
            System.err.println("微信绑定失败: " + e.getMessage());

            if (e instanceof RuntimeException) {
                throw e;
            } else {
                throw new RuntimeException("微信绑定失败，请重试", e);
            }
        }
    }

    /**
     * 根据微信openid查找用户
     * <p>
     * 通过微信openid查询用户，用于判断用户是否已存在
     *
     * @param openid 微信用户openid
     * @return 用户实体，如果不存在返回null
     */
    private User findUserByWechatOpenid(String openid) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("wechat_openid = ?", openid)
                .and("deleted = 0");
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 更新已有微信用户的信息
     * <p>
     * 用户已存在时，更新其session_key和基本信息
     *
     * @param user           已有用户实体
     * @param wechatResponse 微信API响应
     * @param request        登录请求
     * @return 更新后的用户实体
     */
    private User updateWechatUserInfo(User user, Code2SessionResponse wechatResponse, WechatLoginRequest request) {
        // 更新微信相关信息
        user.setWechatSessionKey(wechatResponse.getSessionKey());
        if (wechatResponse.getUnionid() != null) {
            user.setWechatUnionid(wechatResponse.getUnionid());
        }

        // 更新用户基本信息（如果提供且当前为空）
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname());
            }
        }

        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar(request.getAvatar());
            }
        }

        // 更新问卷数据（如果提供）
        if (request.getQuestionnaireData() != null && !request.getQuestionnaireData().trim().isEmpty()) {
            user.setQuestionnaireData(request.getQuestionnaireData());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        return user;
    }

    /**
     * 创建新的微信用户
     * <p>
     * 用户不存在时，根据微信信息创建新用户账户
     *
     * @param wechatResponse 微信API响应
     * @param request        登录请求
     * @return 新创建的用户实体
     */
    private User createWechatUser(Code2SessionResponse wechatResponse, WechatLoginRequest request) {
        User user = new User();

        // 设置微信相关信息
        user.setWechatOpenid(wechatResponse.getOpenid());
        user.setWechatUnionid(wechatResponse.getUnionid());
        user.setWechatSessionKey(wechatResponse.getSessionKey());

        // 设置用户基本信息
        String nickname = request.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            // 如果没有提供昵称，生成默认昵称
            nickname = "微信用户" + System.currentTimeMillis();
        }
        user.setNickname(nickname);

        // 生成唯一的用户名（微信用户可能没有用户名）
        user.setUsername("wx_" + wechatResponse.getOpenid().substring(0, Math.min(10, wechatResponse.getOpenid().length())));

        // 设置头像
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }

        // 设置注册来源
        user.setRegisterSource(request.getRegisterSource() != null ? request.getRegisterSource() : 2);

        // 设置默认角色为体验用户
        user.setRole(0);

        // 设置问卷数据
        if (request.getQuestionnaireData() != null && !request.getQuestionnaireData().trim().isEmpty()) {
            user.setQuestionnaireData(request.getQuestionnaireData());
        }

        // 设置时间戳
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);

        // 注意：微信用户没有密码和邮箱，这些字段保持为null

        // 保存到数据库
        userMapper.insert(user);

        return user;
    }

    /**
     * 微信登录
     * <p>
     * 基于微信登录规范的完整微信登录实现
     * 支持自动注册新用户并返回标准化的响应格式
     *
     * @param openid 微信用户openid
     * @param sessionKey 微信会话密钥
     * @param unionid 微信开放平台unionid（可选）
     * @param request 登录请求信息
     * @return 标准化的微信登录响应
     */
    @Transactional
    public WechatLoginResponse wechatLogin(String openid, String sessionKey, String unionid, WechatLoginRequest request) {
        try {
            System.out.println("开始处理微信登录，openid: " + openid);

            // 根据openid查找用户
            User existingUser = findUserByOpenid(openid);

            User user;
            boolean isNewUser;

            if (existingUser != null) {
                // 老用户登录，更新session_key
                user = updateWechatUserInfo(existingUser, sessionKey, unionid, request);
                isNewUser = false;
                System.out.println("老用户登录，用户ID: " + user.getId());
            } else {
                // 新用户注册
                user = createWechatUser(openid, sessionKey, unionid, request);
                isNewUser = true;
                System.out.println("新用户注册，用户ID: " + user.getId());
            }

            // 生成简单Token
            String token = generateSimpleToken(user.getId());

            // 构建响应
            WechatLoginResponse response = new WechatLoginResponse();
            response.setToken(token);
            response.setIsNewUser(isNewUser);
            response.setLoginTime(System.currentTimeMillis());

            // 计算token过期时间
            long expirationTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L; // 7天
            response.setTokenExpiration(expirationTime);

            // 设置用户信息
            WechatLoginResponse.UserInfo userInfo = convertToUserInfo(user);
            response.setUserInfo(userInfo);

            System.out.println("微信登录成功，生成Token");
            return response;

        } catch (Exception e) {
            System.err.println("微信登录处理失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("微信登录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取当前用户信息（用于AuthController）
     * <p>
     * 根据用户ID获取用户信息，用于Token验证后的用户信息获取
     *
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    public WechatLoginResponse.UserInfo getCurrentUserInfo(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            return null;
        }
        return convertToUserInfo(user);
    }

    /**
     * 根据openid查找用户（优化版）
     * <p>
     * 使用正确的字段名查询微信用户
     *
     * @param openid 微信openid
     * @return 用户实体，如果不存在返回null
     */
    private User findUserByOpenid(String openid) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("wechat_openid = ?", openid)
                .and("deleted = 0");
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 更新微信用户信息（优化版）
     * <p>
     * 更新已有用户的微信相关信息
     *
     * @param user 已有用户实体
     * @param sessionKey 新的会话密钥
     * @param unionid 微信开放平台unionid
     * @param request 登录请求
     * @return 更新后的用户实体
     */
    private User updateWechatUserInfo(User user, String sessionKey, String unionid, WechatLoginRequest request) {
        // 更新会话密钥
        user.setWechatSessionKey(sessionKey);

        // 更新unionid（如果有）
        if (unionid != null && !unionid.trim().isEmpty()) {
            user.setWechatUnionid(unionid);
        }

        // 更新用户基本信息（如果提供且当前为空）
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname());
            }
        }

        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar(request.getAvatar());
            }
        }

        // 更新问卷数据（如果提供）
        if (request.getQuestionnaireData() != null && !request.getQuestionnaireData().trim().isEmpty()) {
            user.setQuestionnaireData(request.getQuestionnaireData());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        return user;
    }

    /**
     * 创建新的微信用户（优化版）
     * <p>
     * 根据微信信息创建新用户账户
     *
     * @param openid 微信openid
     * @param sessionKey 会话密钥
     * @param unionid 微信开放平台unionid
     * @param request 登录请求
     * @return 新创建的用户实体
     */
    private User createWechatUser(String openid, String sessionKey, String unionid, WechatLoginRequest request) {
        User user = new User();

        // 设置微信相关信息
        user.setWechatOpenid(openid);
        user.setWechatSessionKey(sessionKey);
        user.setWechatUnionid(unionid);

        // 设置用户基本信息
        String nickname = request.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "微信用户" + System.currentTimeMillis();
        }
        user.setNickname(nickname);

        // 生成唯一的用户名
        user.setUsername("wx_" + openid.substring(0, Math.min(10, openid.length())));

        // 设置头像
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }

        // 设置注册来源
        user.setRegisterSource(request.getRegisterSource() != null ? request.getRegisterSource() : 2);

        // 设置默认角色为体验用户
        user.setRole(0);

        // 设置问卷数据
        if (request.getQuestionnaireData() != null && !request.getQuestionnaireData().trim().isEmpty()) {
            user.setQuestionnaireData(request.getQuestionnaireData());
        }

        // 设置时间戳
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);

        // 保存到数据库
        userMapper.insert(user);

        return user;
    }

    /**
     * 将用户实体转换为UserInfo DTO
     * <p>
     * 转换用户信息为前端需要的格式，包含脱敏处理
     *
     * @param user 用户实体
     * @return UserInfo DTO
     */
    private WechatLoginResponse.UserInfo convertToUserInfo(User user) {
        WechatLoginResponse.UserInfo userInfo = new WechatLoginResponse.UserInfo();

        userInfo.setUserId(user.getId());
        userInfo.setId(user.getId()); // 前端兼容性：设置id字段
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail()); // 设置邮箱（可能为空）
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setRegisterSource(user.getRegisterSource());
        userInfo.setStatus(0); // 假设用户状态正常

        // 设置认证状态：role > 0 表示已认证
        userInfo.setIsVerified(user.getRole() > 0);

        // 设置问卷完成状态：有问卷数据表示已完成
        userInfo.setQuestionnaireCompleted(user.getQuestionnaireData() != null && !user.getQuestionnaireData().trim().isEmpty());

        // 脱敏处理openid
        if (user.getWechatOpenid() != null) {
            userInfo.setOpenidMasked(user.getWechatOpenid());
        }

        // 设置角色描述
        userInfo.setRoleDesc(user.getRole());

        // 设置注册时间
        if (user.getCreatedAt() != null) {
            userInfo.setRegistrationTime(user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000);
        }

        return userInfo;
    }

    /**
     * 获取用户统计数据
     * <p>
     * 统计用户的发帖数、关注数、粉丝数、获赞数、被收藏数
     * 所有计数都通过实时查询获取，确保数据准确性
     *
     * @param userId 用户ID
     * @return 用户统计数据DTO
     * @throws RuntimeException 当用户不存在时抛出
     */
    public UserStatsDTO getUserStats(Long userId) {
        // Step 1: 验证用户存在
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // Step 2: 统计发帖数量
        QueryWrapper postsQuery = QueryWrapper.create()
                .select("COUNT(*)")
                .from("posts")
                .where("user_id = ? AND deleted = 0", userId);
        Object postsCountObj = postMapper.selectObjectByQuery(postsQuery);
        Long postsCount = postsCountObj != null ? ((Number) postsCountObj).longValue() : 0L;

        // Step 3: 统计关注数量（用户关注的其他人）
        Long followingCount = followService.countFollowing(userId);
        Long followersCount = followService.countFollowers(userId);
// Step 5: 统计获赞总数（用户的帖子被点赞的总数）
        QueryWrapper likesQuery = QueryWrapper.create()
                .select("COUNT(*)")
                .from("user_actions")
                .where("target_id IN (SELECT id FROM posts WHERE user_id = ? AND deleted = 0) " +
                       "AND action_type = 0 AND deleted = 0 AND target_type = 'post'", userId);
        Object likesCountObj = userActionMapper.selectObjectByQuery(likesQuery);
        Long likesCount = likesCountObj != null ? ((Number) likesCountObj).longValue() : 0L;

        // Step 6: 统计被收藏总数（用户的帖子被收藏的总数）
        QueryWrapper collectsQuery = QueryWrapper.create()
                .select("COUNT(*)")
                .from("user_actions")
                .where("target_id IN (SELECT id FROM posts WHERE user_id = ? AND deleted = 0) " +
                       "AND action_type = 1 AND deleted = 0 AND target_type = 'post'", userId);
        Object collectsCountObj = userActionMapper.selectObjectByQuery(collectsQuery);
        Long collectsCount = collectsCountObj != null ? ((Number) collectsCountObj).longValue() : 0L;

        // Step 7: 构建并返回统计DTO
        UserStatsDTO stats = new UserStatsDTO();
        stats.setUserId(userId);
        stats.setPostsCount(postsCount);
        stats.setFollowingCount(followingCount);
        stats.setFollowersCount(followersCount);
        stats.setLikesCount(likesCount);
        stats.setCollectsCount(collectsCount);

        return stats;
    }
}
