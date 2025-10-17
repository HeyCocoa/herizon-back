create table user_follow
(
    id          bigint auto_increment comment '粉丝关系ID'
        primary key,
    follower_id bigint                             not null comment '关注人ID，引用users.id',
    followee_id bigint                             not null comment '被关注人ID，引用users.id',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '关注时间',
    constraint uk_follower_followee
        unique (follower_id, followee_id),
    constraint fk_user_follow_followee
        foreign key (followee_id) references users (id)
            on delete cascade,
    constraint fk_user_follow_follower
        foreign key (follower_id) references users (id)
            on delete cascade
)
    comment '用户关注关系表';

create index idx_followee_id
    on user_follow (followee_id);

create index idx_follower_id
    on user_follow (follower_id);

INSERT INTO herizon.user_follow (id, follower_id, followee_id, created_at) VALUES (3, 1039, 1002, '2025-10-11 14:15:45');
INSERT INTO herizon.user_follow (id, follower_id, followee_id, created_at) VALUES (4, 1001, 1039, '2025-10-11 14:19:38');
INSERT INTO herizon.user_follow (id, follower_id, followee_id, created_at) VALUES (5, 1039, 1001, '2025-10-11 15:04:49');
INSERT INTO herizon.user_follow (id, follower_id, followee_id, created_at) VALUES (7, 1039, 1004, '2025-10-11 18:22:59');
