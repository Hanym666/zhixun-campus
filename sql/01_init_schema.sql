USE zhixun_campus;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    identity_type VARCHAR(20) NOT NULL,
    student_staff_no VARCHAR(50) NOT NULL,
    phone VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    avatar_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_student_staff_no (student_staff_no),
    UNIQUE KEY uk_sys_user_email (email),
    KEY idx_sys_user_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联';

CREATE TABLE IF NOT EXISTS item_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT UNSIGNED NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_category_code (code),
    KEY idx_item_category_parent (parent_id),
    CONSTRAINT fk_item_category_parent FOREIGN KEY (parent_id) REFERENCES item_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物品分类';

CREATE TABLE IF NOT EXISTS item_post (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    post_no VARCHAR(40) NOT NULL,
    publisher_id BIGINT UNSIGNED NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    title VARCHAR(120) NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    public_features TEXT NULL,
    private_features TEXT NULL,
    occurred_at DATETIME NULL,
    campus_area VARCHAR(100) NULL,
    location_name VARCHAR(200) NULL,
    color VARCHAR(50) NULL,
    brand VARCHAR(100) NULL,
    contact_hint VARCHAR(255) NULL,
    view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
    published_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_post_no (post_no),
    KEY idx_item_post_publisher (publisher_id, deleted),
    KEY idx_item_post_type_status (item_type, status, deleted),
    KEY idx_item_post_category (category_id, status),
    KEY idx_item_post_occurred_at (occurred_at),
    CONSTRAINT fk_item_post_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user (id),
    CONSTRAINT fk_item_post_category FOREIGN KEY (category_id) REFERENCES item_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='寻物与招领帖子';

CREATE TABLE IF NOT EXISTS item_image (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    cover_image TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_item_image_post (post_id, sort_order),
    CONSTRAINT fk_item_image_post FOREIGN KEY (post_id) REFERENCES item_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片';

CREATE TABLE IF NOT EXISTS claim_request (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    claim_no VARCHAR(40) NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    claimant_id BIGINT UNSIGNED NOT NULL,
    evidence TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT UNSIGNED NULL,
    review_comment VARCHAR(500) NULL,
    reviewed_at DATETIME NULL,
    handover_location VARCHAR(255) NULL,
    handover_at DATETIME NULL,
    completed_at DATETIME NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_claim_request_no (claim_no),
    KEY idx_claim_request_post (post_id, status),
    KEY idx_claim_request_claimant (claimant_id, status),
    CONSTRAINT fk_claim_request_post FOREIGN KEY (post_id) REFERENCES item_post (id),
    CONSTRAINT fk_claim_request_claimant FOREIGN KEY (claimant_id) REFERENCES sys_user (id),
    CONSTRAINT fk_claim_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认领申请';

CREATE TABLE IF NOT EXISTS claim_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    claim_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_claim_message_claim (claim_id, created_at),
    CONSTRAINT fk_claim_message_claim FOREIGN KEY (claim_id) REFERENCES claim_request (id),
    CONSTRAINT fk_claim_message_sender FOREIGN KEY (sender_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认领沟通记录';

CREATE TABLE IF NOT EXISTS match_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    source_post_id BIGINT UNSIGNED NOT NULL,
    target_post_id BIGINT UNSIGNED NOT NULL,
    keyword_score DECIMAL(6,5) NOT NULL DEFAULT 0,
    vector_score DECIMAL(6,5) NOT NULL DEFAULT 0,
    final_score DECIMAL(6,5) NOT NULL DEFAULT 0,
    notified TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_match_record_pair (source_post_id, target_post_id),
    KEY idx_match_record_score (final_score, created_at),
    CONSTRAINT fk_match_source_post FOREIGN KEY (source_post_id) REFERENCES item_post (id),
    CONSTRAINT fk_match_target_post FOREIGN KEY (target_post_id) REFERENCES item_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能撮合记录';

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    business_type VARCHAR(40) NULL,
    business_id BIGINT UNSIGNED NULL,
    read_flag TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notification_user_read (user_id, read_flag, created_at),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知';

CREATE TABLE IF NOT EXISTS ai_usage_daily (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    usage_date DATE NOT NULL,
    call_count INT NOT NULL DEFAULT 0,
    input_tokens BIGINT UNSIGNED NOT NULL DEFAULT 0,
    output_tokens BIGINT UNSIGNED NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_usage_user_date (user_id, usage_date),
    CONSTRAINT fk_ai_usage_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 每日用量';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    operator_id BIGINT UNSIGNED NULL,
    operation VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id BIGINT UNSIGNED NULL,
    request_method VARCHAR(10) NULL,
    request_path VARCHAR(500) NULL,
    client_ip VARCHAR(64) NULL,
    success TINYINT(1) NOT NULL DEFAULT 1,
    error_message VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operation_log_operator (operator_id, created_at),
    KEY idx_operation_log_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作审计日志';

INSERT INTO sys_role (code, name, description)
VALUES
    ('USER', '普通用户', '学生或教职工用户'),
    ('ADMIN', '管理员', '平台管理人员')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description);

INSERT INTO item_category (code, name, sort_order)
VALUES
    ('ELECTRONICS', '数码电子', 10),
    ('CARDS', '证件卡片', 20),
    ('KEYS', '钥匙门禁', 30),
    ('BOOKS', '书籍文具', 40),
    ('CLOTHING', '衣物配饰', 50),
    ('DAILY', '生活用品', 60),
    ('OTHER', '其他', 99)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order);
