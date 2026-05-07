package com.example.project_management.feature.comment;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility để parse @mention từ nội dung comment.
 * Regex: @username — chứa chữ, số, dấu gạch dưới, 1-50 ký tự.
 */
public class MentionParser {

    private static final Pattern MENTION_PATTERN =
            Pattern.compile("@([a-zA-Z0-9_]{1,50})");

    private MentionParser() {}

    /**
     * Trả về Set<String> username (lowercase) không trùng, theo thứ tự xuất hiện.
     * @param content nội dung comment
     * @return set username được mention
     */
    public static Set<String> extractUsernames(String content) {
        Set<String> usernames = new LinkedHashSet<>();
        if (content == null || content.isBlank()) return usernames;
        Matcher m = MENTION_PATTERN.matcher(content);
        while (m.find()) {
            usernames.add(m.group(1).toLowerCase());
        }
        return usernames;
    }
}
