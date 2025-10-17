create table posts
(
    id            bigint auto_increment comment '甯栧瓙ID锛屼富閿嚜澧?
        primary key,
    user_id       bigint                             not null comment '鍙戝笘鐢ㄦ埛ID锛屽叧鑱攗sers琛?,
    title         varchar(200)                       not null comment '甯栧瓙鏍囬',
    content       text                               not null comment '甯栧瓙鍐呭',
    post_type     tinyint  default 0                 not null comment '甯栧瓙绫诲瀷: 0=鏅€氬笘, 1=鎶曠エ甯? 2=杩濊鍏ず甯?,
    view_count    int      default 0                 not null comment '娴忚娆℃暟',
    like_count    int      default 0                 not null comment '鐐硅禐鏁伴噺',
    share_count   int      default 0                 not null comment '鍒嗕韩鏁伴噺',
    collect_count int      default 0                 not null comment '鏀惰棌鏁伴噺',
    comment_count int      default 0                 not null comment '璇勮鏁伴噺',
    image_urls    json                               null comment '鍥剧墖URL鍒楄〃锛圝SON鏁扮粍锛?,
    created_at    datetime default CURRENT_TIMESTAMP not null comment '鍒涘缓鏃堕棿',
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '鏇存柊鏃堕棿',
    status        tinyint  default 0                 not null comment '鐘舵€? 0=姝ｅ父, 1=鍒犻櫎, 2=瀹℃牳涓?,
    deleted       tinyint  default 0                 not null comment '閫昏緫鍒犻櫎鏍囪: 0=姝ｅ父, 1=鍒犻櫎',
    constraint posts_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '甯栧瓙琛細瀛樺偍鐢ㄦ埛鍙戝竷鐨勫悇绫诲唴瀹?;

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

INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (1, 1001, '2025骞磋亴鍦哄コ鎬ф湀钖揪8978鍏冿紒鍒嗕韩鎴戠殑AIGC鎶€鑳芥彁鍗囦箣璺?, '鏍规嵁鏈€鏂拌皟鐮旓紝2025骞磋亴鍦哄コ鎬у钩鍧囨湀钖揪8978鍏冿紝姣斿幓骞村闀?0.3%锛佹垜鍒氶€氳繃鍏ㄥ浗鐢熸垚寮忎汉宸ユ櫤鑳斤紙AIGC锛夋妧鏈簲鐢ㄨ亴涓氬煿璁€冭瘯锛岃繖涓瘉涔︾湡鐨勫緢鏈夊惈閲戦噺銆傜幇鍦?0%鐨勫伐浣滈兘浼氭秹鍙婃彁绀鸿瘝宸ョ▼锛屾帉鎻hatGPT銆丮idjourney绛夊伐鍏峰凡鎴愬繀澶囨妧鑳姐€傚垎浜涔犺鐐癸細1.鍙傚姞澶嶆棪AIGC鐮斾慨鐝?2.鑰冨彇瀹樻柟鑱屼笟璇佷功 3.瀹炴搷缁冧範鎻愮ず璇嶇紪鍐?4.瀛︿細AI瑙嗛鍒朵綔銆傚コ鎬у湪AI鎺ュ彈搴︿笂姣旂敺鎬ф洿楂橈紝鎴戜滑瑕佹姄浣忚繖涓紭鍔匡紒', 0, 2161, 289, 45, 1, 0, null, '2025-01-29 10:30:00', '2025-09-29 21:18:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (2, 1002, '鑱屽満濡堝鐨勬暟瀛楀寲杞瀷锛?5%鐨勬垜浠凡鍦ㄤ娇鐢ˋI宸ュ叿骞宠　宸ヤ綔涓庡搴?, '鏅鸿仈鎷涜仒鎶ュ憡鏄剧ず锛?5%鐨勮亴鍦哄濡堝凡寮€濮嬩娇鐢ㄦ暟瀛楀寲宸ュ叿锛屾瘮鍘诲勾鎻愬崌6.4%锛佷綔涓轰簩瀹濆+閮ㄩ棬涓荤锛屽垎浜垜鐨勬暟瀛楀寲骞宠　鏈細1.鐢ˋI鍔╂墜鍒跺畾姣忔棩璁″垝鍜屼紭鍏堢骇鎺掑簭 2.鍒╃敤鏅鸿兘瀹跺眳鍑忓皯瀹跺姟鏃堕棿 3.閫氳繃杩滅▼鍔炲叕宸ュ叿瀹炵幇鐏垫椿宸ヤ綔 4.AI鑲插効鍔╂墜甯姪鏁欒偛瑙勫垝銆?5.3%鑱屽満濡堝鍋氳繃鍏ㄨ亴濡堝锛?6.7%閲嶈繑鑱屽満锛屾垜浠瀛︿細鎷ユ姳鎶€鏈彉闈┿€傝浣忥細鎶€鑳借凯浠ｂ啋鏁堢巼璺冭縼鈫掕亴涓氶煣鎬э紒', 0, 2758, 357, 67, 1, 0, null, '2025-01-28 16:20:00', '2025-10-11 21:13:38', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (3, 1003, '濂虫€ф嫨涓氭剰鎰挎瘮鐢锋€ф洿寮猴紒50.5%鐨勬垜浠効鎰忛噸鏂版嫨涓氳繘鍏ユ柊琛屼笟', '鏈€鏂版暟鎹渿鎾硷細50.5%濂虫€ф効鎰忛噸鏂版嫨涓氳繘鍏ユ柊琛屼笟锛屾槑鏄鹃珮浜庣敺鎬х殑43.9%锛?2.7%濂虫€у€惧悜閫氳繃鑰冭瘉鎻愬崌绔炰簤鍔涳紝鑰岀敺鎬у彧鏈?3.9%銆傛垜灏辨槸娲荤敓鐢熺殑渚嬪瓙锛?0宀佷粠浼犵粺琛屼笟杞叆AIGC棰嗗煙銆傚垎浜浆鍨嬪績寰楋細1.閫夋嫨鏈夊墠鏅殑缁嗗垎璧涢亾锛堝AI+鏁欒偛锛?.绯荤粺瀛︿範鑰岄潪纰庣墖鍖栵紙鎶ュ悕涓撲笟鍩硅鐝級3.鎵惧埌闈犺氨瀵煎笀鎴栫ぞ缇?4.淇濇寔瀛︿範鑺傚涓嶆€ヨ簛 5.鍑嗗鍏呰冻璧勯噾鏀拺銆傜幇鍦ㄨ柂璧勭炕鍊嶏紝濂虫€х殑瀛︿範閫傚簲鍔涚湡鐨勫緢寮猴紒', 0, 1878, 234, 41, 0, 0, null, '2025-01-27 14:45:00', '2025-09-29 21:18:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (4, 1004, '銆愮鐞嗗憳鍒嗕韩銆戣亴鍦哄コ鎬у崌鑱屼俊蹇冩柇宕栧紡涓嬫粦锛氫粠21.5%璺岃嚦7.9%', '浣滀负骞冲彴绠＄悊鍛橈紝鍒嗕韩涓€涓€煎緱鍏虫敞鐨勬暟鎹細浠呮湁7.9%濂虫€у鍗囪亴鏈夊崄瓒虫妸鎻★紝鐩歌緝鍘诲勾21.5%鏂礀寮忎笅婊戯紒涓昏鍘熷洜锛?5.3%鍦ㄥ鑲查樁娈佃鍔ㄥけ鍘绘檵鍗囨満浼氾紝10.2%鍥犵収椤惧搴簿鍔涘垎鏁ｃ€備絾鎴戜滑涓嶈兘姘旈锛?6.6%濂虫€ц涓鸿嚜宸卞伐浣滆〃鐜版瘮鍚岃亴绾х敺鍚屼簨鏇翠匠锛岃鏄庢垜浠殑瀹炲姏鏄璁ゅ彲鐨勩€傚缓璁細1.涓诲姩浜夊彇楂樹环鍊奸」鐩睍绀鸿兘鍔?2.寤虹珛鑱屽満瀵煎笀鍏崇郴 3.瀛︿細鍚戜笂绠＄悊 4.閫傛椂琛ㄨ揪鏅嬪崌鎰忔効銆傝浣忥細涓撲笟瀹炲姏+涓诲姩浜夊彇=鑱屽満绐佺牬锛?, 0, 3286, 449, 89, 1, 0, '["/api/uploads/post/2025/10/10/fde0d1caed2f4e318e5ab97e01720d47.png"]', '2025-01-26 09:15:00', '2025-10-11 21:41:14', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (5, 1005, '鑱屽満濡堝鏃堕棿绠＄悊鏂扮瓥鐣ワ細鍒╃敤AI宸ュ叿瀹炵幇鏁堢巼缈诲€?, '鍒嗕韩鎴戜綔涓鸿亴鍦哄濡堢殑鏃堕棿绠＄悊杩涘寲鍙诧紒浼犵粺鏂规硶vs AI璧嬭兘鏂规硶瀵规瘮锛氳繃鍘绘墜鍐欒鍒掕〃鈫掔幇鍦ˋI鏅鸿兘瑙勫垝锛涜繃鍘诲嚟缁忛獙鎺掍紭鍏堢骇鈫掔幇鍦ㄧ畻娉曡緟鍔╁喅绛栵紱杩囧幓鎵嬪姩璺熻釜杩涘害鈫掔幇鍦ㄨ嚜鍔ㄥ寲鐩戞帶銆傚叿浣撳伐鍏锋帹鑽愶細1.Motion AI鍋氭棩绋嬩紭鍖?2.Notion AI鏁寸悊浼氳绾 3.ChatGPT鐢熸垚閭欢妯℃澘 4.灏忕埍鍚屽绠＄悊瀹跺涵浜嬪姟銆傜粨鏋滐細宸ヤ綔鏁堢巼鎻愬崌40%锛屼翰瀛愭椂闂村鍔?灏忔椂/澶┿€傚缓璁埗瀹欰BC浼樺厛绾э細A=蹇呴』鍋氫笖閲嶈锛孊=搴旇鍋氾紝C=鍙互鎺ㄨ繜銆傝浣忥細宸ュ叿鏄斁澶у櫒锛屾柟娉曟槸鏍规湰锛?, 0, 2095, 279, 52, 1, 0, null, '2025-01-25 11:30:00', '2025-10-11 21:13:24', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (6, 1008, 'Test Post for Comments', 'This is a test post to test commenting functionality.', 0, 1, 0, 0, 0, 0, '[]', '2025-10-03 00:54:39', '2025-10-03 00:54:39', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (7, 1007, '鑷姩鍖栨祴璇曞笘瀛?- 1759480928656', '杩欐槸涓€涓敱API娴嬭瘯鍒涘缓鐨勫笘瀛愬唴瀹广€傛祴璇曟椂闂? 2025/10/3 16:42:08', 0, 1, 1, 0, 0, 0, '[]', '2025-10-03 16:42:09', '2025-10-03 16:42:09', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (8, 1007, '瀹屾暣娴嬭瘯 - 鍥炬枃甯栧瓙 1759481727033', '杩欐槸瀹屾暣娴嬭瘯鍒涘缓鐨勫浘鏂囧笘瀛愬唴瀹?, 0, 1, 0, 1, 0, 0, '[]', '2025-10-03 16:55:27', '2025-10-03 16:55:27', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (9, 1007, '瀹屾暣娴嬭瘯 - 鎶曠エ甯栧瓙 1759481727356', '杩欐槸涓€涓姇绁ㄩ棶棰?, 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:55:27', '2025-10-03 16:55:27', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (10, 1007, '鎶曠エ娴嬭瘯 1759481728354', '鎶曠エ闂', 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:55:28', '2025-10-03 16:55:28', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (11, 1007, '瀹屾暣娴嬭瘯 - 鍥炬枃甯栧瓙 1759481820577', '杩欐槸瀹屾暣娴嬭瘯鍒涘缓鐨勫浘鏂囧笘瀛愬唴瀹?, 0, 3, 0, 1, 0, 0, '[]', '2025-10-03 16:57:01', '2025-10-03 16:57:01', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (12, 1007, '瀹屾暣娴嬭瘯 - 鎶曠エ甯栧瓙 1759481820912', '杩欐槸涓€涓姇绁ㄩ棶棰?, 1, 0, 0, 0, 0, 0, null, '2025-10-03 16:57:01', '2025-10-03 16:57:01', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (13, 1007, '鎶曠エ娴嬭瘯 1759481821809', '鎶曠エ闂', 1, 1, 0, 0, 0, 0, null, '2025-10-03 16:57:02', '2025-10-03 16:57:02', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (14, 1007, '銆愰泦鎴愭祴璇曘€戝墠鍚庣浜や簰娴嬭瘯甯栧瓙', '杩欐槸閫氳繃闆嗘垚娴嬭瘯鑴氭湰鍙戝竷鐨勫笘瀛愶紝鐢ㄤ簬楠岃瘉鍓嶇鈫掑悗绔殑鏁版嵁浼犺緭銆?, 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:02:56', '2025-10-03 17:02:56', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (15, 1007, '銆愰泦鎴愭祴璇曘€戝浘鏂囧笘_1759482610851', '杩欐槸涓€鏉￠€氳繃闆嗘垚娴嬭瘯鍒涘缓鐨勫浘鏂囧笘瀛愶紝鍖呭惈涓板瘜鐨勫唴瀹广€?, 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:10:11', '2025-10-03 17:10:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (16, 1007, '銆愰泦鎴愭祴璇曘€戞姇绁ㄥ笘_1759482611291', '璇锋姇绁ㄩ€夋嫨浣犵殑鍋忓ソ', 1, 1, 0, 0, 0, 0, null, '2025-10-03 17:10:11', '2025-10-03 17:10:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (17, 1007, '銆愰泦鎴愭祴璇曘€戝浘鏂囧笘_1759482678366', '杩欐槸涓€鏉￠€氳繃闆嗘垚娴嬭瘯鍒涘缓鐨勫浘鏂囧笘瀛愶紝鍖呭惈涓板瘜鐨勫唴瀹广€?, 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:11:18', '2025-10-03 17:11:18', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (18, 1007, '銆愰泦鎴愭祴璇曘€戞姇绁ㄥ笘_1759482679439', '璇锋姇绁ㄩ€夋嫨浣犵殑鍋忓ソ', 1, 1, 0, 0, 0, 0, null, '2025-10-03 17:11:19', '2025-10-03 17:11:19', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (19, 1007, '銆愯嚜鎰堟祴璇曘€戞祴璇曞笘_1759483908663', '杩欐槸鑷剤寮忔祴璇曟鏋跺垱寤虹殑甯栧瓙', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:31:49', '2025-10-03 17:31:49', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (20, 1007, '銆愯嚜鎰堟祴璇曘€戞祴璇曞笘_1759484186224', '杩欐槸鑷剤寮忔祴璇曟鏋跺垱寤虹殑甯栧瓙', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:36:26', '2025-10-03 17:36:26', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (21, 1007, '娴嬭瘯_1759484538087', '鍐呭', 0, 1, 0, 0, 0, 0, null, '2025-10-03 17:42:18', '2025-10-03 17:42:18', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (22, 1015, '娴嬭瘯甯栧瓙_1759484900431', '杩欐槸涓€鏉℃祴璇曞笘瀛愬唴瀹癸紝鐢ㄤ簬楠岃瘉鍙戝竷娴佺▼', 0, 0, 0, 0, 0, 0, null, '2025-10-03 17:48:20', '2025-10-03 17:48:20', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (23, 1016, '娴嬭瘯甯栧瓙_1759485025015', '杩欐槸涓€鏉℃祴璇曞笘瀛愬唴瀹癸紝鐢ㄤ簬楠岃瘉鍙戝竷娴佺▼', 0, 0, 0, 0, 0, 0, '[]', '2025-10-03 17:50:25', '2025-10-03 17:50:25', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (24, 1023, 'Test Post 1759598015355', 'This is a comprehensive test post created by automation', 0, 0, 0, 0, 0, 0, null, '2025-10-05 01:13:35', '2025-10-05 01:13:35', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (25, 1024, 'Test Post 1759598058501', 'This is a comprehensive test post created by automation', 0, 0, 0, 0, 0, 0, null, '2025-10-05 01:14:19', '2025-10-05 01:14:19', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (26, 1039, '1111111', '111111111111111', 0, 4, 0, 0, 0, 0, '[]', '2025-10-05 23:31:30', '2025-10-09 23:08:33', 1, 1);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (27, 1039, '111', '111111111', 1, 18, 1, 0, 0, 0, '[]', '2025-10-06 01:59:25', '2025-10-06 01:59:25', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (28, 1039, '鎶曠エ璐存祴璇?, '娴嬭瘯', 1, 3, 0, 0, 0, 0, '[]', '2025-10-06 02:02:17', '2025-10-06 02:02:17', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (29, 1039, 'Test Post for Tag Count', 'Testing tag post count increment', 0, 1, 0, 0, 0, 0, null, '2025-10-06 02:35:09', '2025-10-11 18:27:11', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (31, 1039, '鍥炬枃娴嬭瘯', '111111', 0, 12, 1, 0, 0, 0, '["/api/uploads/post/2025/10/10/fde0d1caed2f4e318e5ab97e01720d47.png"]', '2025-10-10 21:41:42', '2025-10-11 03:26:39', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (32, 1039, '娴嬭瘯', '1', 0, 0, 0, 0, 0, 0, '["/api/uploads/post/2025/10/10/be955f7510b846cf8931bde2126d0ae0.png"]', '2025-10-10 21:57:37', '2025-10-10 21:57:37', 0, 0);
INSERT INTO herizon.posts (id, user_id, title, content, post_type, view_count, like_count, share_count, collect_count, comment_count, image_urls, created_at, updated_at, status, deleted) VALUES (33, 1039, '1', '1', 0, 6, 0, 0, 0, 0, '[]', '2025-10-10 22:01:54', '2025-10-11 14:17:28', 0, 0);

