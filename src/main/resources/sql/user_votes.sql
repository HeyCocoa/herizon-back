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
    comment '用户投票记录表';

create index idx_post_option
    on user_votes (post_id, option_id)
    comment '用于快速统计各选项票数';

INSERT INTO herizon.user_votes (id, user_id, post_id, option_id, created_at, deleted) VALUES (1, 1007, 13, 9, '2025-10-03 16:57:04', 0);
INSERT INTO herizon.user_votes (id, user_id, post_id, option_id, created_at, deleted) VALUES (2, 1007, 16, 11, '2025-10-03 17:10:13', 0);
INSERT INTO herizon.user_votes (id, user_id, post_id, option_id, created_at, deleted) VALUES (3, 1007, 18, 14, '2025-10-03 17:11:23', 0);
INSERT INTO herizon.user_votes (id, user_id, post_id, option_id, created_at, deleted) VALUES (4, 1039, 27, 18, '2025-10-09 06:04:13', 0);
INSERT INTO herizon.user_votes (id, user_id, post_id, option_id, created_at, deleted) VALUES (5, 1039, 28, 19, '2025-10-09 23:38:56', 0);
