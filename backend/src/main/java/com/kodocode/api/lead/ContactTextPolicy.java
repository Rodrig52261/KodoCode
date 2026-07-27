package com.kodocode.api.lead;

import java.util.List;
import java.util.regex.Pattern;

final class ContactTextPolicy {
    private static final List<Pattern> CODE_PATTERNS = List.of(
            Pattern.compile("<\\s*/?\\s*[a-z][^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:javascript|data)\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("```|<\\?|<%|\\$\\{|=>"),
            Pattern.compile("(?:^|\\s)(?:function|class|import|export|const|let|var)\\s+[a-z_$]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(?:select\\s+.+\\s+from|insert\\s+into|delete\\s+from|drop\\s+table|union\\s+select)\\b", Pattern.CASE_INSENSITIVE)
    );

    private ContactTextPolicy() {}

    static boolean containsCode(String value) {
        return CODE_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(value).find());
    }
}
