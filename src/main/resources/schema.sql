CREATE TABLE IF NOT EXISTS profile
(
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(16) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    last_ip    INT,
    created_at TIMESTAMP,
    last_login TIMESTAMP,
    invited_by INT
);

CREATE TABLE IF NOT EXISTS profile_component
(
    user_id               INT,
    component_type        INT,
    component_priority    INT,
    component_title       VARCHAR(25),
    component_description VARCHAR(255),
    component_styles      VARCHAR(255),
    PRIMARY KEY (user_id, component_type, component_title)
);

CREATE TABLE IF NOT EXISTS rank
(
    rank_id                   SERIAL PRIMARY KEY,
    rank_name                 VARCHAR(50) UNIQUE,
    rank_style                VARCHAR(255),
    rank_priority             INT UNIQUE,
    rank_purchasable          BOOLEAN,
    rank_can_manage_lower     BOOLEAN,
    rank_staff                BOOLEAN,
    rank_can_ban              BOOLEAN,
    rank_can_manage_users     BOOLEAN,
    rank_can_give_ranks       BOOLEAN,
    rank_can_give_staff_ranks BOOLEAN
);

CREATE TABLE IF NOT EXISTS user_group
(
    user_id  INT,
    group_id INT,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (group_id) REFERENCES rank (rank_id)
);

CREATE TABLE IF NOT EXISTS profile_redirect
(
    user_id         INT,
    redirect_string VARCHAR(32),
    PRIMARY KEY (user_id, redirect_string)
);

CREATE TABLE IF NOT EXISTS domain
(
    user_id INT,
    domain  VARCHAR(100) UNIQUE,
    PRIMARY KEY (user_id, domain)
);

CREATE TABLE IF NOT EXISTS invite
(
    user_id     INT,
    invite_code VARCHAR(16),
    PRIMARY KEY (user_id, invite_code)
);

CREATE TABLE IF NOT EXISTS platform
(
    platform_id           SERIAL,
    platform_key          VARCHAR(50) UNIQUE,
    platform_display_name VARCHAR(50) UNIQUE,
    PRIMARY KEY (platform_id)
);

CREATE TABLE IF NOT EXISTS user_social
(
    user_id     SERIAL,
    platform_id INT,
    username    VARCHAR(100),
    PRIMARY KEY (user_id, platform_id, username),
    FOREIGN KEY (platform_id) REFERENCES platform (platform_id)
);