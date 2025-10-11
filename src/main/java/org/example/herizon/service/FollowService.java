package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.FollowUserDTO;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.User;
import org.example.herizon.entity.UserFollow;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserFollowMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Transactional
    public boolean toggleFollow(Long followerId, Long followeeId) {
        if (Objects.equals(followerId, followeeId)) {
            throw new RuntimeException("不能关注自己");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(UserFollow::getFollowerId).eq(followerId)
            .and(UserFollow::getFolloweeId).eq(followeeId);

        UserFollow existing = userFollowMapper.selectOneByQuery(queryWrapper);
        if (existing != null) {
            userFollowMapper.deleteById(existing.getId());
            return false;
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);
        return true;
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null || Objects.equals(followerId, followeeId)) {
            return false;
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("1")
            .from("user_follow")
            .where(UserFollow::getFollowerId).eq(followerId)
            .and(UserFollow::getFolloweeId).eq(followeeId)
            .limit(1);

        return userFollowMapper.selectObjectByQuery(queryWrapper) != null;
    }

    public long countFollowing(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("COUNT(*)")
            .from("user_follow")
            .where(UserFollow::getFollowerId).eq(userId);

        Object count = userFollowMapper.selectObjectByQuery(queryWrapper);
        return count == null ? 0L : ((Number) count).longValue();
    }

    public long countFollowers(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("COUNT(*)")
            .from("user_follow")
            .where(UserFollow::getFolloweeId).eq(userId);

        Object count = userFollowMapper.selectObjectByQuery(queryWrapper);
        return count == null ? 0L : ((Number) count).longValue();
    }

    public PageResult<FollowUserDTO> getFollowing(Long ownerId, Long viewerId, Integer current, Integer size) {
        Page<UserFollow> page = Page.of(current, size);
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(UserFollow::getFollowerId).eq(ownerId)
            .orderBy(UserFollow::getCreatedAt, false);

        Page<UserFollow> followPage = userFollowMapper.paginate(page, queryWrapper);
        List<FollowUserDTO> records = buildFollowUserList(followPage.getRecords(), viewerId, ownerId, false);
        return PageResult.of(records,
            followPage.getTotalRow(),
            (long) followPage.getPageNumber(),
            (long) followPage.getPageSize());
    }

    public PageResult<FollowUserDTO> getFollowers(Long ownerId, Long viewerId, Integer current, Integer size) {
        Page<UserFollow> page = Page.of(current, size);
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(UserFollow::getFolloweeId).eq(ownerId)
            .orderBy(UserFollow::getCreatedAt, false);

        Page<UserFollow> followPage = userFollowMapper.paginate(page, queryWrapper);
        List<FollowUserDTO> records = buildFollowUserList(followPage.getRecords(), viewerId, ownerId, true);
        return PageResult.of(records,
            followPage.getTotalRow(),
            (long) followPage.getPageNumber(),
            (long) followPage.getPageSize());
    }

    private List<FollowUserDTO> buildFollowUserList(List<UserFollow> relations,
                                                    Long viewerId,
                                                    Long ownerId,
                                                    boolean asFollowersList) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Long> targetIdSet = relations.stream()
            .map(relation -> asFollowersList ? relation.getFollowerId() : relation.getFolloweeId())
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Long> targetUserIds = new ArrayList<>(targetIdSet);
        if (targetUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = loadUsers(targetUserIds);
        Map<Long, Long> followersCountMap = loadFollowersCount(targetUserIds);
        Map<Long, Long> followingCountMap = loadFollowingCount(targetUserIds);
        Map<Long, Long> postsCountMap = loadPostsCount(targetUserIds);

        Set<Long> ownerFollowingTargets = asFollowersList
            ? findFollowTargets(ownerId, targetUserIds)
            : Collections.emptySet();

        Set<Long> targetsFollowOwner = asFollowersList
            ? Collections.emptySet()
            : findFollowersOfOwner(ownerId, targetUserIds);

        Set<Long> viewerFollowingTargets;
        if (viewerId == null) {
            viewerFollowingTargets = Collections.emptySet();
        } else if (Objects.equals(viewerId, ownerId)) {
            if (asFollowersList) {
                viewerFollowingTargets = ownerFollowingTargets;
            } else {
                viewerFollowingTargets = new HashSet<>(targetUserIds);
            }
        } else {
            viewerFollowingTargets = findFollowTargets(viewerId, targetUserIds);
        }

        List<FollowUserDTO> result = new ArrayList<>(relations.size());
        for (UserFollow relation : relations) {
            Long targetUserId = asFollowersList ? relation.getFollowerId() : relation.getFolloweeId();
            if (targetUserId == null) {
                continue;
            }

            User user = userMap.get(targetUserId);
            if (user == null) {
                continue;
            }

            FollowUserDTO dto = new FollowUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            dto.setAvatar(user.getAvatar());
            dto.setRole(user.getRole());
            dto.setIsVerified(user.getRole() != null && user.getRole() > 0);
            dto.setFollowTime(relation.getCreatedAt());
            dto.setIsSelf(viewerId != null && Objects.equals(viewerId, user.getId()));
            dto.setFollowersCount(followersCountMap.getOrDefault(user.getId(), 0L));
            dto.setFollowingCount(followingCountMap.getOrDefault(user.getId(), 0L));
            dto.setPostsCount(postsCountMap.getOrDefault(user.getId(), 0L));

            boolean ownerFollowsTarget = asFollowersList ? ownerFollowingTargets.contains(user.getId()) : true;
            boolean targetFollowsOwner = asFollowersList || targetsFollowOwner.contains(user.getId());
            dto.setIsMutualFollow(ownerFollowsTarget && targetFollowsOwner);

            boolean viewerFollowsTarget = false;
            if (viewerId != null && !Objects.equals(viewerId, user.getId())) {
                if (Objects.equals(viewerId, ownerId)) {
                    viewerFollowsTarget = asFollowersList ? ownerFollowsTarget : true;
                } else {
                    viewerFollowsTarget = viewerFollowingTargets.contains(user.getId());
                }
            }
            dto.setIsFollowing(viewerFollowsTarget);

            result.add(dto);
        }

        return result;
    }

    private Map<Long, User> loadUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(User::getId).in(userIds);
        List<User> users = userMapper.selectListByQuery(queryWrapper);
        return users.stream()
            .filter(user -> user.getId() != null)
            .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Map<Long, Long> loadFollowersCount(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("followee_id AS userId")
            .select("COUNT(*) AS followersCount")
            .from("user_follow")
            .where(UserFollow::getFolloweeId).in(userIds)
            .groupBy("followee_id");
        List<Row> rows = userFollowMapper.selectRowsByQuery(queryWrapper);
        return buildCountMap(rows, "userId", "followersCount");
    }

    private Map<Long, Long> loadFollowingCount(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("follower_id AS userId")
            .select("COUNT(*) AS followingCount")
            .from("user_follow")
            .where(UserFollow::getFollowerId).in(userIds)
            .groupBy("follower_id");
        List<Row> rows = userFollowMapper.selectRowsByQuery(queryWrapper);
        return buildCountMap(rows, "userId", "followingCount");
    }

    private Map<Long, Long> loadPostsCount(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("user_id AS userId")
            .select("COUNT(*) AS postsCount")
            .from("posts")
            .where(Post::getUserId).in(userIds)
            .and(Post::getDeleted).eq(0)
            .and(Post::getStatus).eq(0)
            .groupBy("user_id");
        List<Row> rows = postMapper.selectRowsByQuery(queryWrapper);
        return buildCountMap(rows, "userId", "postsCount");
    }

    private Set<Long> findFollowTargets(Long followerId, List<Long> candidateUserIds) {
        if (followerId == null || candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Collections.emptySet();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("followee_id")
            .from("user_follow")
            .where(UserFollow::getFollowerId).eq(followerId)
            .and(UserFollow::getFolloweeId).in(candidateUserIds);
        List<Object> rows = userFollowMapper.selectObjectListByQuery(queryWrapper);
        return rows.stream()
            .map(this::toLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<Long> findFollowersOfOwner(Long ownerId, List<Long> candidateUserIds) {
        if (ownerId == null || candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Collections.emptySet();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("follower_id")
            .from("user_follow")
            .where(UserFollow::getFolloweeId).eq(ownerId)
            .and(UserFollow::getFollowerId).in(candidateUserIds);
        List<Object> rows = userFollowMapper.selectObjectListByQuery(queryWrapper);
        return rows.stream()
            .map(this::toLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Map<Long, Long> buildCountMap(List<Row> rows, String userIdAlias, String countAlias) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>();
        for (Row row : rows) {
            if (row == null) {
                continue;
            }
            Long userId = toLong(resolveColumnValue(row, userIdAlias));
            Long count = toLong(resolveColumnValue(row, countAlias));
            if (userId != null && count != null) {
                result.put(userId, count);
            }
        }
        return result;
    }

    private Object resolveColumnValue(Row row, String alias) {
        if (row == null || alias == null) {
            return null;
        }
        Object value = row.get(alias);
        if (value != null) {
            return value;
        }
        String upper = alias.toUpperCase(Locale.ROOT);
        value = row.get(upper);
        if (value != null) {
            return value;
        }
        String snakeUpper = toUpperSnake(alias);
        return row.get(snakeUpper);
    }

    private String toUpperSnake(String alias) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < alias.length(); i++) {
            char ch = alias.charAt(i);
            if (Character.isUpperCase(ch)) {
                sb.append('_').append(ch);
            } else {
                sb.append(Character.toUpperCase(ch));
            }
        }
        return sb.toString();
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
