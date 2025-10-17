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
    comment '帖子表：存储用户发布的各类内容';

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

INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (1, 1001, '2025年职场女性月薪达8978元！分享我的AIGC技能提升之路', '根据最新调研，2025年职场女性平均月薪达8978元，比去年增长10.3%！我刚通过全国生成式人工智能（AIGC）技术应用职业培训考试，这个证书真的很有含金量。现在50%的工作都会涉及提示词工程，掌握ChatGPT、Midjourney等工具已成必备技能。分享学习要点：1.参加复旦AIGC研修班 2.考取官方职业证书 3.实操练习提示词编写 4.学会AI视频制作。女性在AI接受度上比男性更高，我们要抓住这个优势！', 0, 2161, 289, 45, 1, 0, null, '2025-01-29 10:30:00', '2025-09-29 21:18:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (2, 1002, '职场妈妈的数字化转型：35%的我们已在使用AI工具平衡工作与家庭', '智联招聘报告显示，35%的职场妈妈已开始使用数字化工具，比去年提升6.4%！作为二宝妈+部门主管，分享我的数字化平衡术：1.用AI助手制定每日计划和优先级排序 2.利用智能家居减少家务时间 3.通过远程办公工具实现灵活工作 4.AI育儿助手帮助教育规划。65.3%职场妈妈做过全职妈妈，56.7%重返职场，我们要学会拥抱技术变革。记住：技能迭代→效率跃迁→职业韧性！', 0, 2758, 357, 67, 1, 0, null, '2025-01-28 16:20:00', '2025-10-11 21:13:38', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (3, 1003, '女性择业意愿比男性更强！50.5%的我们愿意重新择业进入新行业', '最新数据震撼：50.5%女性愿意重新择业进入新行业，明显高于男性的43.9%！42.7%女性倾向通过考证提升竞争力，而男性只有33.9%。我就是活生生的例子：30岁从传统行业转入AIGC领域。分享转型心得：1.选择有前景的细分赛道（如AI+教育）2.系统学习而非碎片化（报名专业培训班）3.找到靠谱导师或社群 4.保持学习节奏不急躁 5.准备充足资金支撑。现在薪资翻倍，女性的学习适应力真的很强！', 0, 1878, 234, 41, 0, 0, null, '2025-01-27 14:45:00', '2025-09-29 21:18:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (4, 1004, '【管理员分享】职场女性升职信心断崖式下滑：从21.5%跌至7.9%', '作为平台管理员，分享一个值得关注的数据：仅有7.9%女性对升职有十足把握，相较去年21.5%断崖式下滑！主要原因：15.3%在婚育阶段被动失去晋升机会，10.2%因照顾家庭精力分散。但我们不能气馁！36.6%女性认为自己工作表现比同职级男同事更佳，说明我们的实力是被认可的。建议：1.主动争取高价值项目展示能力 2.建立职场导师关系 3.学会向上管理 4.适时表达晋升意愿。记住：专业实力+主动争取=职场突破！', 0, 3286, 449, 89, 1, 0, '["http://localhost:8080/api/uploads/post/2025/10/10/fde0d1caed2f4e318e5ab97e01720d47.png"]', '2025-01-26 09:15:00', '2025-10-11 21:41:14', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (5, 1005, '职场妈妈时间管理新策略：利用AI工具实现效率翻倍', '分享我作为职场妈妈的时间管理进化史！传统方法vs AI赋能方法对比：过去手写计划表→现在AI智能规划；过去凭经验排优先级→现在算法辅助决策；过去手动跟踪进度→现在自动化监控。具体工具推荐：1.Motion AI做日程优化 2.Notion AI整理会议纪要 3.ChatGPT生成邮件模板 4.小爱同学管理家庭事务。结果：工作效率提升40%，亲子时间增加1小时/天。建议制定ABC优先级：A=必须做且重要，B=应该做，C=可以推迟。记住：工具是放大器，方法是根本！', 0, 2095, 279, 52, 1, 0, null, '2025-01-25 11:30:00', '2025-10-11 21:13:24', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (6, 1008, 'Test Post for Comments', 'This is a test post to test commenting functionality.', 0, 1, 0, 0, 0, 0, '[]', '2025-10-03 00:54:39', '2025-10-03 00:54:39', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (7, 1007, '自动化测试帖子 - 1759480928656', '这是一个由API测试创建的帖子内容。测试时间: 2025/10/3 16:42:08', 0, 1, 1, 0, 0, 0, '[]', '2025-10-03 16:42:09', '2025-10-03 16:42:09', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (8, 1007, '完整测试 - 图文帖子 1759481727033', '这是完整测试创建的图文帖子内容', 0, 1, 0, 1, 0, 0, '[]', '2025-10-03 16:55:27', '2025-10-03 16:55:27', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (9, 1007, '完整测试 - 投票帖子 1759481727356', '这是一个投票问题', 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:55:27', '2025-10-03 16:55:27', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (10, 1007, '投票测试 1759481728354', '投票问题', 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:55:28', '2025-10-03 16:55:28', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (11, 1007, '完整测试 - 图文帖子 1759481820577', '这是完整测试创建的图文帖子内容', 0, 3, 0, 1, 0, 0, '[]', '2025-10-03 16:57:01', '2025-10-03 16:57:01', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (12, 1007, '完整测试 - 投票帖子 1759481820912', '这是一个投票问题', 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:57:01', '2025-10-03 16:57:01', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (13, 1007, '投票测试 1759481821809', '投票问题', 1, 1, 0, 0, 0, 0, null, '2025-10-03 16:57:02', '2025-10-03 16:57:02', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (14, 1007, '【集成测试】前后端交互测试帖子', '这是通过集成测试脚本发布的帖子，用于验证前端→后端的数据传输。', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:02:56', '2025-10-03 17:02:56', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (15, 1007, '【集成测试】图文帖_1759482610851', '这是一条通过集成测试创建的图文帖子，包含丰富的内容。', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:10:11', '2025-10-03 17:10:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (16, 1007, '【集成测试】投票帖_1759482611291', '请投票选择你的偏好', 1, 1, 0, 0, 0, 0, null, '2025-10-03 17:10:11', '2025-10-03 17:10:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (17, 1007, '【集成测试】图文帖_1759482678366', '这是一条通过集成测试创建的图文帖子，包含丰富的内容。', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:11:18', '2025-10-03 17:11:18', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (18, 1007, '【集成测试】投票帖_1759482679439', '请投票选择你的偏好', 1, 1, 0, 0, 0, 0, null, '2025-10-03 17:11:19', '2025-10-03 17:11:19', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (19, 1007, '【自愈测试】测试帖_1759483908663', '这是自愈式测试框架创建的帖子', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:31:49', '2025-10-03 17:31:49', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (20, 1007, '【自愈测试】测试帖_1759484186224', '这是自愈式测试框架创建的帖子', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:36:26', '2025-10-03 17:36:26', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (21, 1007, '测试_1759484538087', '内容', 0, 1, 0, 0, 0, 0, null, '2025-10-03 17:42:18', '2025-10-03 17:42:18', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (22, 1015, '测试帖子_1759484900431', '这是一条测试帖子内容，用于验证发布流程', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:48:20', '2025-10-03 17:48:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (23, 1016, '测试帖子_1759485025015', '这是一条测试帖子内容，用于验证发布流程', 0, 0, 0, 0, 0, 0, '[]', '2025-10-03 17:50:25', '2025-10-03 17:50:25', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (24, 1023, 'Test Post 1759598015355', 'This is a comprehensive test post created by automation', 0, 0, 0, 0, 0, 0, null, '2025-10-05 01:13:35', '2025-10-05 01:13:35', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (25, 1024, 'Test Post 1759598058501', 'This is a comprehensive test post created by automation', 0, 0, 0, 0, 0, 0, null, '2025-10-05 01:14:19', '2025-10-05 01:14:19', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (26, 1039, '1111111', '111111111111111', 0, 4, 0, 0, 0, 0, '[]', '2025-10-05 23:31:30', '2025-10-09 23:08:33', 1, 1);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (27, 1039, '111', '111111111', 1, 18, 1, 0, 0, 0, '[]', '2025-10-06 01:59:25', '2025-10-06 01:59:25', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (28, 1039, '投票贴测试', '测试', 1, 3, 0, 0, 0, 0, '[]', '2025-10-06 02:02:17', '2025-10-06 02:02:17', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (29, 1039, 'Test Post for Tag Count', 'Testing tag post count increment', 0, 1, 0, 0, 0, 0, null, '2025-10-06 02:35:09', '2025-10-11 18:27:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (31, 1039, '图文测试', '111111', 0, 12, 1, 0, 0, 0, '["http://localhost:8080/api/uploads/post/2025/10/10/fde0d1caed2f4e318e5ab97e01720d47.png"]', '2025-10-10 21:41:42', '2025-10-11 03:26:39', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (32, 1039, '测试', '1', 0, 0, 0, 0, 0, 0, '["http://localhost:8080/api/uploads/post/2025/10/10/be955f7510b846cf8931bde2126d0ae0.png"]', '2025-10-10 21:57:37', '2025-10-10 21:57:37', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (33, 1039, '1', '1', 0, 6, 0, 0, 0, 0, '[]', '2025-10-10 22:01:54', '2025-10-11 14:17:28', 0, 0);
