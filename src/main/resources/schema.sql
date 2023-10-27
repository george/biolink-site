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

CREATE INDEX IF NOT EXISTS profile_id ON profile(id);
CREATE INDEX IF NOT EXISTS profile_username ON profile(username);
CREATE INDEX IF NOT EXISTS profile_email ON profile(email);

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

CREATE INDEX IF NOT EXISTS profile_component_user_id ON profile_component(user_id);

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

CREATE INDEX IF NOT EXISTS user_group_from_id ON user_group(user_id);

CREATE TABLE IF NOT EXISTS profile_redirect
(
    user_id         INT,
    redirect_string VARCHAR(32),
    PRIMARY KEY (user_id, redirect_string)
);

CREATE INDEX IF NOT EXISTS profile_redirect_user_id ON profile_redirect(user_id);

CREATE TABLE IF NOT EXISTS domain
(
    user_id INT,
    domain  VARCHAR(100) UNIQUE,
    PRIMARY KEY (user_id, domain)
);

CREATE INDEX IF NOT EXISTS domain_name ON domain(domain);

CREATE TABLE IF NOT EXISTS invite
(
    user_id     INT,
    invite_code VARCHAR(16),
    PRIMARY KEY (user_id, invite_code)
);

CREATE INDEX IF NOT EXISTS invite_code ON invite(invite_code);

CREATE TABLE IF NOT EXISTS platform
(
    platform_id           SERIAL,
    platform_key          VARCHAR(50) UNIQUE,
    platform_display_name VARCHAR(50) UNIQUE,
    PRIMARY KEY (platform_id)
);

CREATE TABLE IF NOT EXISTS verification_code
(
    user_id           INT,
    verification_code INT       NOT NULL,
    sent_at           TIMESTAMP NOT NULL DEFAULT Now(),
    PRIMARY KEY (user_id, verification_code)
);

CREATE INDEX IF NOT EXISTS verification_codes_code ON verification_code(verification_code);
CREATE INDEX IF NOT EXISTS verification_codes_sent_at ON verification_code(sent_at);

CREATE TABLE IF NOT EXISTS user_social
(
    user_id     SERIAL,
    platform_id INT,
    username    VARCHAR(100),
    PRIMARY KEY (user_id, platform_id, username),
    FOREIGN KEY (platform_id) REFERENCES platform (platform_id)
);

CREATE INDEX IF NOT EXISTS user_social_user_id ON user_social(user_id);

CREATE TABLE IF NOT EXISTS ban_reason
(
    ban_id     SERIAL PRIMARY KEY,
    ban_reason VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ban
(
    punishment_id SERIAL PRIMARY KEY,
    user_id       INT       NOT NULL,
    ban_type_id   INT       NOT NULL,
    banned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    issued_by     INT       NOT NULL,
    ban_active    BOOLEAN   NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS bans_punishment_id ON ban(punishment_id);
CREATE INDEX IF NOT EXISTS bans_user_id ON ban(user_id);

CREATE TABLE IF NOT EXISTS staff_logs
(
    log_id      SERIAL PRIMARY KEY,
    log_type_id INT NOT NULL,
    staff_id    INT NOT NULL,
    target_user INT,
    description VARCHAR(255),
    FOREIGN KEY (target_user) REFERENCES profile(id)
);

CREATE INDEX IF NOT EXISTS logs_staff_id ON staff_logs(staff_id);
CREATE INDEX IF NOT EXISTS logs_user_id ON staff_logs(target_user);
CREATE INDEX IF NOT EXISTS logs_type_id ON staff_logs(log_type_id);

CREATE FUNCTION expire_handler() RETURNS trigger
    LANGUAGE plpgsql
    AS
    '
    BEGIN
        DELETE FROM verification_code WHERE sent_at < NOW() - INTERVAL ''30 minutes'';
        RETURN NEW;
    END;
    ';

CREATE TRIGGER expire_handler_delete_old_rows AFTER INSERT ON verification_code EXECUTE PROCEDURE expire_handler();