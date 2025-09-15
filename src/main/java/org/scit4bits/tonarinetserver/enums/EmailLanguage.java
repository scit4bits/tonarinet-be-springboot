package org.scit4bits.tonarinetserver.enums;

/**
 * 이메일 언어를 정의하는 열거형
 */
public enum EmailLanguage {
    /** 한국어 */
    KOR("kor", "한국어"),
    /** 영어 */
    ENG("eng", "English"),
    /** 일본어 */
    JPN("jpn", "日本語");

    private final String code;
    private final String displayName;

    /**
     * EmailLanguage 생성자
     * @param code 언어 코드
     * @param displayName 표시 이름
     */
    EmailLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * 언어 코드를 반환합니다.
     * @return 언어 코드
     */
    public String getCode() {
        return code;
    }

    /**
     * 표시 이름을 반환합니다.
     * @return 표시 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 코드로부터 EmailLanguage 열거형을 반환합니다.
     * @param code 언어 코드
     * @return 해당 EmailLanguage, 없으면 ENG (기본값)
     */
    public static EmailLanguage fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ENG; // Default to English
        }

        for (EmailLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return ENG; // Fallback to English
    }
}