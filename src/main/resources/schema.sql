CREATE TABLE IF NOT EXISTS profile
(
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(16)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    ip_salt     VARCHAR(32)  NOT NULL,
    last_ip     INT          NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    last_login  TIMESTAMP    NOT NULL,
    mfa_enabled BOOLEAN      DEFAULT FALSE,
    mfa_secret  VARCHAR(16),
    invited_by  INT
);

CREATE INDEX IF NOT EXISTS profile_id ON profile(id);

CREATE TABLE IF NOT EXISTS profile_ip
(
    profile_id INT          NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    FOREIGN KEY (profile_id) REFERENCES profile(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, ip_address)
);

CREATE INDEX IF NOT EXISTS profile_ips_composite ON profile_ip(profile_id, ip_address);

CREATE INDEX IF NOT EXISTS profile_username ON profile(username);
CREATE INDEX IF NOT EXISTS profile_email ON profile(email);

CREATE TABLE IF NOT EXISTS component
(
    component_id       SERIAL PRIMARY KEY,
    component_type     INT,
    component_meta     TEXT,
    component_text     VARCHAR(255),
    component_styles   VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS component_id ON component(component_id);

CREATE TABLE IF NOT EXISTS profile_component
(
    user_id         INT,
    component_id    INT,
    component_index INT,
    PRIMARY KEY (user_id, component_index),
    FOREIGN KEY (user_id) REFERENCES profile(id),
    FOREIGN KEY (component_id) REFERENCES component(component_id)
);

CREATE INDEX IF NOT EXISTS profile_components_id ON profile_component(user_id);

CREATE TABLE IF NOT EXISTS rank
(
    id                   SERIAL PRIMARY KEY,
    name                 VARCHAR(50) UNIQUE,
    style                VARCHAR(255),
    priority             INT UNIQUE,
    max_redirects        INT DEFAULT 3,
    purchasable          BOOLEAN,
    can_manage_lower     BOOLEAN,
    staff                BOOLEAN,
    can_ban              BOOLEAN,
    can_manage_users     BOOLEAN,
    can_give_ranks       BOOLEAN
);

CREATE TABLE IF NOT EXISTS user_group
(
    user_id  INT,
    group_id INT,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES profile(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES rank (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS user_group_from_id ON user_group(user_id);

CREATE TABLE IF NOT EXISTS user_note
(
    note_id  SERIAL PRIMARY KEY,
    user_id  INT  NOT NULL,
    staff_id INT  NOT NULL,
    note     TEXT NOT NULL,
    left_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS user_note_user_id ON user_note(user_id);

CREATE TABLE IF NOT EXISTS profile_redirect
(
    redirect_string VARCHAR(32) NOT NULL PRIMARY KEY UNIQUE,
    user_id         INT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES profile (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS profile_redirect_user_id ON profile_redirect(user_id);
CREATE INDEX IF NOT EXISTS profile_redirect_redirect_string ON profile_redirect(redirect_string);

CREATE TABLE IF NOT EXISTS pending_redirect_transfers
(
    redirect_string VARCHAR(32) NOT NULL PRIMARY KEY UNIQUE,
    transferring_to INT         NOT NULL,
    FOREIGN KEY (redirect_string) REFERENCES profile_redirect (redirect_string) ON DELETE CASCADE,
    FOREIGN KEY (transferring_to) REFERENCES profile (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS profile_redirect_transfer_redirect_string ON pending_redirect_transfers(redirect_string);

CREATE TABLE IF NOT EXISTS domain
(
    user_id INT NOT NULL,
    domain  VARCHAR(100) UNIQUE,
    FOREIGN KEY (user_id) REFERENCES profile(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, domain)
);

CREATE INDEX IF NOT EXISTS domain_user_id ON domain(user_id);
CREATE INDEX IF NOT EXISTS domain_name ON domain(domain);

CREATE TABLE IF NOT EXISTS invite
(
    user_id     INT,
    invite_code VARCHAR(16) PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES profile(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS invite_user_id ON invite(user_id);
CREATE INDEX IF NOT EXISTS invite_code ON invite(invite_code);

CREATE TABLE IF NOT EXISTS platform
(
    platform_id           SERIAL PRIMARY KEY,
    platform_display_name VARCHAR(50) UNIQUE
);

CREATE INDEX platform_platform_id ON platform(platform_id);

CREATE TABLE IF NOT EXISTS context
(
    context_id   VARCHAR(255) PRIMARY KEY,
    user_id      INT       NOT NULL,
    context_meta TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT Now(),
    FOREIGN KEY (user_id) REFERENCES profile (id)
);

CREATE INDEX IF NOT EXISTS context_context_id ON context(context_id);

CREATE TABLE IF NOT EXISTS verification_code
(
    user_id           INT          NOT NULL PRIMARY KEY,
    verification_code INT          NOT NULL UNIQUE,
    context_id        VARCHAR(255) NOT NULL,
    FOREIGN KEY (context_id) REFERENCES context (context_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS verification_code_user_id ON verification_code(user_id);
CREATE INDEX IF NOT EXISTS verification_codes_code ON verification_code(verification_code);

CREATE TABLE IF NOT EXISTS user_social
(
    user_id     SERIAL,
    platform_id INT,
    username    VARCHAR(100),
    PRIMARY KEY (user_id, platform_id, username),
    FOREIGN KEY (platform_id) REFERENCES platform (platform_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS user_social_user_id ON user_social(user_id);

CREATE TABLE IF NOT EXISTS ban_reason
(
    ban_id     SERIAL PRIMARY KEY,
    ban_reason TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ban
(
    punishment_id SERIAL PRIMARY KEY,
    user_id       INT       NOT NULL,
    ban_type_id   INT       NOT NULL,
    banned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    issued_by     INT       NOT NULL,
    ban_active    BOOLEAN   NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES profile(id) ON DELETE CASCADE,
    FOREIGN KEY (issued_by) REFERENCES profile(id) ON DELETE CASCADE,
    FOREIGN KEY (ban_type_id) REFERENCES ban_reason(ban_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS bans_punishment_id ON ban(punishment_id);
CREATE INDEX IF NOT EXISTS bans_user_id ON ban(user_id);

CREATE TABLE IF NOT EXISTS staff_logs
(
    log_id      SERIAL PRIMARY KEY,
    log_type_id INT NOT NULL,
    staff_id    INT NOT NULL,
    target_user INT,
    description VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS logs_staff_id ON staff_logs(staff_id);
CREATE INDEX IF NOT EXISTS logs_user_id ON staff_logs(target_user);
CREATE INDEX IF NOT EXISTS logs_type_id ON staff_logs(log_type_id);

CREATE TABLE IF NOT EXISTS payment_package
(
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    rank_id        INT,
    price          DECIMAL(8,2),
    available_from TIMESTAMP DEFAULT NOW(),
    available_to   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS payment_package_id ON payment_package(id);

CREATE TABLE IF NOT EXISTS discount
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    discount_amount DECIMAL(5, 2),
    promotion_code  VARCHAR(30),
    available_from  TIMESTAMP DEFAULT NOW(),
    available_to    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS discount_id ON discount(id);
CREATE INDEX IF NOT EXISTS discount_name ON discount(name);

CREATE TABLE IF NOT EXISTS payment
(
    payment_id     SERIAL PRIMARY KEY,
    user_id        INT,
    payment_type   INT,
    payment_amount DECIMAL(8, 2),
    discount_used  INT,
    data TEXT,
    transaction_id VARCHAR(100),
    FOREIGN KEY (payment_type) REFERENCES payment_package (id)
);

CREATE INDEX IF NOT EXISTS payment_payment_id ON payment(payment_id);
CREATE INDEX IF NOT EXISTS payment_user_id ON payment(user_id);

CREATE OR REPLACE FUNCTION expire_handler() RETURNS trigger
    LANGUAGE plpgsql
    AS
    '
    BEGIN
        DELETE FROM context WHERE created_at < NOW() - INTERVAL ''30 minutes'';
        RETURN NEW;
    END;
    ';

CREATE OR REPLACE TRIGGER expire_handler_delete_old_rows AFTER INSERT ON context EXECUTE PROCEDURE expire_handler();