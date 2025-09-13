package org.scit4bits.tonarinetserver.enums;

public enum EmailLanguage {
    KO("ko", "한국어"),
    EN("en", "English"),
    JA("ja", "日本語");

    private final String code;
    private final String displayName;

    EmailLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EmailLanguage fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return KO; // Default to Korean
        }

        for (EmailLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return KO; // Fallback to Korean
    }
}