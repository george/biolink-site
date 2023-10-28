package dev.george.biolink.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum LogType {

    UPDATE_USER_GROUP("update_user_group", 0),
    BAN_USER("ban_user", 1);

    private final String status;
    private final int type;

}
