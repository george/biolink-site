package dev.george.biolink.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter @AllArgsConstructor
public enum LogType {

    UPDATE_USER_GROUP("update_user_group", 0),
    BAN_USER("ban_user", 1),
    UNBAN_USER("unban_user", 2),
    VIEW_PROFILE("view_profile", 3),
    GENERAL_USER_SEARCH("general_user_search", 4),
    ASSIGN_PROFILE_REDIRECT("assign_profile_redirect", 5),
    DISABLE_MFA("disable_mfa", 6),
    USER_CHANGE_EMAIL("user_change_email", 7),
    USER_INVALID_INPUT("user_invalid_input", 8);

    private final String status;
    private final int type;

    public static LogType findById(int typeId) {
        return Arrays.stream(values())
                .filter(logType -> logType.getType() == typeId)
                .findFirst()
                .orElse(null);
    }
}
