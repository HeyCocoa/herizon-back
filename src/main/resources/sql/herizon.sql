create table poll_options
(
    id            bigint auto_increment
        primary key,
    post_id       bigint                             not null comment '帖子ID',
    option_text   varchar(200)                       not null comment '选项名称',
    display_order int                                not null comment '选项显示顺序',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    deleted       tinyint  default 0                 null comment '逻辑删除，0=未删除，1=已删除'
)
    comment '投票选项表' engine = InnoDB;

create index idx_deleted
    on poll_options (deleted);

create index idx_post_id
    on poll_options (post_id);

create table tags
(
    id          bigint auto_increment comment '标签ID，主键自增'
        primary key,
    name        varchar(50)                        not null comment '标签名称，唯一',
    description varchar(200)                       null comment '标签描述',
    post_count  int      default 0                 not null comment '使用该标签的帖子数量',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_name
        unique (name)
)
    comment '标签表：内容主题分类管理' engine = InnoDB;

create index idx_created_at
    on tags (created_at);

create index idx_deleted
    on tags (deleted);

create index idx_post_count
    on tags (post_count);

create index idx_tag_search
    on tags (deleted asc, name asc, post_count desc);

create table user_votes
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                             not null comment '投票用户ID',
    post_id    bigint                             not null comment '帖子ID',
    option_id  bigint                             not null comment '所选选项ID',
    created_at datetime default CURRENT_TIMESTAMP null comment '投票时间',
    deleted    tinyint  default 0                 null comment '逻辑删除',
    constraint uk_user_post
        unique (user_id, post_id) comment '确保每人每帖只能投一次'
)
    comment '用户投票记录表' engine = InnoDB;

-- comment on index uk_user_post not supported: 确保每人每帖只能投一次

create index idx_post_option
    on user_votes (post_id, option_id)
    comment '用于快速统计各选项票数';

-- comment on index idx_post_option not supported: 用于快速统计各选项票数

create table users
(
    id                 bigint auto_increment comment '用户ID，主键自增'
        primary key,
    username           varchar(50)                        not null comment '用户名，唯一标识',
    email              varchar(100)                       not null comment '邮箱地址',
    password_hash      varchar(255)                       not null comment '密码哈希值',
    role               tinyint  default 0                 not null comment '用户角色: 0=体验用户, 1=正式用户, 2=管理员',
    questionnaire_data json                               null comment '身份认证问卷数据(JSON格式)',
    wechat_openid      varchar(64)                        null comment '微信用户唯一标识(openid)',
    wechat_unionid     varchar(64)                        null comment '微信开放平台统一标识(unionid)',
    wechat_session_key varchar(64)                        null comment '微信会话密钥(session_key)',
    nickname           varchar(50)                        null comment '用户昵称(优先使用微信昵称)',
    avatar             varchar(255)                       null comment '用户头像URL(优先使用微信头像)',
    register_source    tinyint  default 1                 null comment '用户注册来源: 1=普通注册, 2=微信小程序, 3=微信App, 4=其他第三方',
    created_at         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted            tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint idx_users_wechat_openid
        unique (wechat_openid),
    constraint uk_email
        unique (email),
    constraint uk_username
        unique (username)
)
    comment '用户表：管理用户信息和角色权限' engine = InnoDB;

create table feedback
(
    id          bigint auto_increment comment '反馈ID，主键自增'
        primary key,
    user_id     bigint                                null comment '提交用户ID，关联users表（可为空，支持匿名反馈）',
    type        varchar(20)                           not null comment '反馈类型: suggestion=建议, bug=问题, complaint=投诉',
    title       varchar(200)                          not null comment '反馈标题',
    content     text                                  not null comment '反馈详细内容',
    contact     varchar(100)                          null comment '联系方式（邮箱或手机号，可选）',
    user_agent  text                                  null comment '用户环境信息（浏览器、系统版本等）',
    status      varchar(20) default 'submitted'       not null comment '处理状态: submitted=已提交, processing=处理中, resolved=已解决, closed=已关闭',
    admin_reply text                                  null comment '管理员回复内容',
    admin_id    bigint                                null comment '处理管理员ID，关联users表',
    priority    tinyint     default 1                 not null comment '优先级: 1=低, 2=中, 3=高, 4=紧急',
    created_at  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint     default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint feedback_ibfk_1
        foreign key (user_id) references users (id)
            on delete set null,
    constraint feedback_ibfk_2
        foreign key (admin_id) references users (id)
            on delete set null
)
    comment '反馈表：用户意见反馈和问题报告管理' engine = InnoDB;

create fulltext index ft_title_content
    on feedback (title, content);

create index idx_admin_id
    on feedback (admin_id);

create index idx_created_at
    on feedback (created_at);

create index idx_deleted
    on feedback (deleted);

create index idx_priority
    on feedback (priority);

create index idx_status
    on feedback (status);

create index idx_type
    on feedback (type);

create index idx_updated_at
    on feedback (updated_at);

create index idx_user_id
    on feedback (user_id);

create table posts
(
    id            bigint auto_increment comment '帖子ID，主键自增'
        primary key,
    user_id       bigint                             not null comment '发帖用户ID，关联users表',
    title         varchar(200)                       not null comment '帖子标题',
    content       text                               not null comment '帖子内容',
    post_type     tinyint  default 0                 not null comment '帖子类型: 0=普通帖, 1=投票帖, 2=违规公示帖',
    view_count    int      default 0                 not null comment '浏览次数',
    like_count    int      default 0                 not null comment '点赞数量',
    share_count   int      default 0                 not null comment '分享数量',
    collect_count int      default 0                 not null comment '收藏数量',
    comment_count int      default 0                 not null comment '评论数量',
    image_urls    json                               null comment '图片URL列表（JSON数组）',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    status        tinyint  default 0                 not null comment '状态: 0=正常, 1=删除, 2=审核中',
    deleted       tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint posts_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '帖子表：存储用户发布的各类内容' engine = InnoDB;

create table comments
(
    id         bigint auto_increment comment '评论ID，主键自增'
        primary key,
    post_id    bigint                             not null comment '所属帖子ID，关联posts表',
    user_id    bigint                             not null comment '评论者用户ID，关联users表',
    parent_id  bigint                             null comment '父评论ID，用于实现嵌套评论，顶级评论为null',
    content    text                               not null comment '评论内容',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    status     tinyint  default 0                 not null comment '状态: 0=正常, 1=删除',
    deleted    tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint comments_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint comments_ibfk_2
        foreign key (user_id) references users (id)
            on delete cascade,
    constraint comments_ibfk_3
        foreign key (parent_id) references comments (id)
            on delete cascade
)
    comment '评论表：支持嵌套回复的评论系统' engine = InnoDB;

create fulltext index ft_content
    on comments (content);

create index idx_created_at
    on comments (created_at);

create index idx_deleted
    on comments (deleted);

create index idx_parent_id
    on comments (parent_id);

create index idx_post_id
    on comments (post_id);

create index idx_status
    on comments (status);

create index idx_user_id
    on comments (user_id);

create table post_tags
(
    id      bigint auto_increment comment '关联记录ID，主键自增'
        primary key,
    post_id bigint            not null comment '帖子ID，关联posts表',
    tag_id  bigint            not null comment '标签ID，关联tags表',
    deleted tinyint default 0 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_post_tag
        unique (post_id, tag_id),
    constraint post_tags_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint post_tags_ibfk_2
        foreign key (tag_id) references tags (id)
            on delete cascade
)
    comment '帖子标签关联表：实现帖子与标签的多对多关系' engine = InnoDB;

create index idx_deleted
    on post_tags (deleted);

create index idx_post_id
    on post_tags (post_id);

create index idx_tag_id
    on post_tags (tag_id);

create fulltext index ft_title_content
    on posts (title, content);

create index idx_created_at
    on posts (created_at);

create index idx_deleted
    on posts (deleted);

create index idx_hot
    on posts (like_count, share_count, collect_count);

create index idx_post_type
    on posts (post_type);

create index idx_search_hot
    on posts (status asc, deleted asc, like_count desc, view_count desc, created_at desc);

create index idx_search_time
    on posts (status asc, deleted asc, created_at desc);

create index idx_status
    on posts (status);

create index idx_user_id
    on posts (user_id);

create table user_actions
(
    id          bigint auto_increment comment '行为记录ID，主键自增'
        primary key,
    user_id     bigint                             not null comment '操作用户ID，关联users表',
    target_id   bigint                             not null comment '目标对象ID（帖子、评论等）',
    target_type varchar(20)                        not null comment '目标对象类型: post=帖子, comment=评论',
    action_type tinyint                            not null comment '行为类型: 0=点赞, 1=收藏, 2=分享, 3=举报',
    extra_data  json                               null comment '额外数据(JSON格式，如举报原因)',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_user_target_action
        unique (user_id, target_id, target_type, action_type, deleted),
    constraint user_actions_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '用户行为表：统一记录点赞、收藏、分享、举报等行为' engine = InnoDB;

create index idx_action_type
    on user_actions (action_type);

create index idx_created_at
    on user_actions (created_at);

create index idx_deleted
    on user_actions (deleted);

create index idx_target
    on user_actions (target_id, target_type);

create table user_follow
(
    id          bigint auto_increment comment '粉丝关系ID'
        primary key,
    follower_id bigint                             not null comment '关注人ID，引用users.id',
    followee_id bigint                             not null comment '被关注人ID，引用users.id',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '关注时间',
    constraint uk_follower_followee
        unique (follower_id, followee_id),
    constraint fk_user_follow_follower
        foreign key (follower_id) references users (id)
            on delete cascade,
    constraint fk_user_follow_followee
        foreign key (followee_id) references users (id)
            on delete cascade
)
    comment '用户关注关系表' engine = InnoDB;

create index idx_followee_id
    on user_follow (followee_id);

create index idx_follower_id
    on user_follow (follower_id);

create index idx_user_id
    on user_actions (user_id);

create index idx_created_at
    on users (created_at);

create index idx_deleted
    on users (deleted);

create index idx_role
    on users (role);

create index idx_username_search
    on users (deleted, username);

create index idx_users_register_source
    on users (register_source);

create index idx_users_wechat_unionid
    on users (wechat_unionid);

create definer = root@`%` view users_wechat_view as
select `herizon`.`users`.`id`                 AS `id`,
       `herizon`.`users`.`username`           AS `username`,
       `herizon`.`users`.`email`              AS `email`,
       `herizon`.`users`.`password_hash`      AS `password_hash`,
       `herizon`.`users`.`role`               AS `role`,
       `herizon`.`users`.`questionnaire_data` AS `questionnaire_data`,
       `herizon`.`users`.`wechat_openid`      AS `wechat_openid`,
       `herizon`.`users`.`wechat_unionid`     AS `wechat_unionid`,
       `herizon`.`users`.`wechat_session_key` AS `wechat_session_key`,
       `herizon`.`users`.`wechat_openid`      AS `openid`,
       `herizon`.`users`.`wechat_unionid`     AS `unionid`,
       `herizon`.`users`.`wechat_session_key` AS `session_key`,
       `herizon`.`users`.`nickname`           AS `nickname`,
       `herizon`.`users`.`avatar`             AS `avatar`,
       `herizon`.`users`.`register_source`    AS `register_source`,
       `herizon`.`users`.`created_at`         AS `created_at`,
       `herizon`.`users`.`updated_at`         AS `updated_at`,
       `herizon`.`users`.`deleted`            AS `deleted`
from `herizon`.`users`
where (`herizon`.`users`.`deleted` = 0);

-- comment on column users_wechat_view.id not supported: 用户ID，主键自增

-- comment on column users_wechat_view.username not supported: 用户名，唯一标识

-- comment on column users_wechat_view.email not supported: 邮箱地址

-- comment on column users_wechat_view.password_hash not supported: 密码哈希值

-- comment on column users_wechat_view.role not supported: 用户角色: 0=体验用户, 1=正式用户, 2=管理员

-- comment on column users_wechat_view.questionnaire_data not supported: 身份认证问卷数据(JSON格式)

-- comment on column users_wechat_view.wechat_openid not supported: 微信用户唯一标识(openid)

-- comment on column users_wechat_view.wechat_unionid not supported: 微信开放平台统一标识(unionid)

-- comment on column users_wechat_view.wechat_session_key not supported: 微信会话密钥(session_key)

-- comment on column users_wechat_view.openid not supported: 微信用户唯一标识(openid)

-- comment on column users_wechat_view.unionid not supported: 微信开放平台统一标识(unionid)

-- comment on column users_wechat_view.session_key not supported: 微信会话密钥(session_key)

-- comment on column users_wechat_view.nickname not supported: 用户昵称(优先使用微信昵称)

-- comment on column users_wechat_view.avatar not supported: 用户头像URL(优先使用微信头像)

-- comment on column users_wechat_view.register_source not supported: 用户注册来源: 1=普通注册, 2=微信小程序, 3=微信App, 4=其他第三方

-- comment on column users_wechat_view.created_at not supported: 创建时间

-- comment on column users_wechat_view.updated_at not supported: 更新时间

-- comment on column users_wechat_view.deleted not supported: 逻辑删除标记: 0=正常, 1=删除

