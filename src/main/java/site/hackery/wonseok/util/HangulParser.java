package site.hackery.wonseok.util;

import java.util.*;

public class HangulParser {
    /**
     * Deconstructs a syllable into its constituent Unicode 자모.
     * This method is <b>non-atomic</b>, meaning that vowels like ㅘ will not be split into 'ㅗ, ㅏ',
     * and consonant blocks like ㄼ will not be split into 'ㄹ, ㅂ'.
     *
     * @param syllable A complete Hangul syllable
     * @return An array of constituent 자모.
     */
    public static char[] deconstruct(char syllable) {
        if (withinHangulSyllables(syllable)) {
            int base = syllable - 0xAC00;

            int jongseong = base % 28;
            base /= 28;
            int jungseong = base % 21;
            base /= 21;
            int choseong = base % 19;

            if (jongseong == 0) {
                return new char[] { CHOSEONG.get(choseong), JUNGSEONG.get(jungseong) };
            } else {
                return new char[] { CHOSEONG.get(choseong), JUNGSEONG.get(jungseong), JONGSEONG.get(jongseong - 1) };
            }
        } else {
            return new char[0];
        }
    }

    /**
     * Atomically deconstructs a syllable into its constituent 자모.
     * This method is <b>atomic</b>, meaning that vowels like 'ㅘ' are split into 'ㅗ, ㅏ' and
     * consonant blocks like 'ㄼ' are split into 'ㄹ, ㅂ'.
     *
     * @param syllable A complete Hangul syllable.
     * @return An array of constituent 자모. (technically 자소)
     */
    public static char[] deconstructAtomic(char syllable) {
        if (withinHangulSyllables(syllable)) {
            char[] deconstructed = deconstruct(syllable);

            StringBuilder builder = new StringBuilder();

            for (char c : deconstructed) {
                builder.append(SPLIT_TABLE.getOrDefault(c, String.valueOf(c)));
            }

            return builder.toString().toCharArray();
        } else {
            return new char[0];
        }
    }

    /**
     * Non-atomically constructs a syllable from its constituent 자모.
     *
     * @param jamo An array of two or three Unicode 자모.
     * @return A complete Hangul syllable.
     */
    public static char construct(char[] jamo) {
        if (jamo.length != 2 && jamo.length != 3) {
            return 0x00;
        }

        if (jamo.length == 3) {
            int choseong = CHOSEONG.indexOf(jamo[0]);
            int jungseong = JUNGSEONG.indexOf(jamo[1]);
            int jongseong = JONGSEONG.indexOf(jamo[2]) + 1;

            return (char) (0xAC00 + choseong * 21 * 28 + jungseong * 28 + jongseong);
        } else {
            int choseong = CHOSEONG.indexOf(jamo[0]);
            int jungseong = JUNGSEONG.indexOf(jamo[1]);

            return (char) (0xAC00 + choseong * 21 * 28 + jungseong * 28);
        }
    }

    /**
     * Atomically constructs a syllable from its constituent 자모
     *
     * @param jamo An array of basic 자모.
     * @return A complete Hangul syllable.
     */
    public static char constructAtomic(char[] jamo) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < jamo.length; i++) {
            char c = jamo[i];

            if (i >= jamo.length - 1) {
                builder.append(c);
            } else {
                char next = jamo[i + 1];

                String agglomerate = String.valueOf(c) + String.valueOf(next);

                if (JOIN_TABLE.containsKey(agglomerate)) {
                    builder.append(JOIN_TABLE.get(agglomerate));
                    i++;
                } else {
                    builder.append(c);
                }
            }
        }

        return construct(builder.toString().toCharArray());
    }

    /**
     * Checks if a character lies in the Unicode Hangul Syllables block
     *
     * @param codepoint A unicode codepoint
     * @return Whether the character could represent a Hangul syllable
     */
    public static boolean withinHangulSyllables(int codepoint) {
        // The first hangul character in Unicode is '가' - at 0xAC00
        // The last hangul character in Unicode is '힟' - at 0xD79F
        // (Source: http://www.unicode.org/charts/PDF/UAC00.pdf)

        return 0xAC00 <= codepoint && codepoint <= 0xD7A3;
    }

    /**
     * Checks if a character lies in the Unicode Hangul Jamo block
     *
     * @param codepoint A unicode codepoint
     * @return Whether the character could represent a Hangul 자모.
     */
    public static boolean withinHangulJamo(int codepoint) {
        // The first character in the Hangul Jamo block is 'ㄱ' at 0x1100
        // The last character in the Hangul Jamo block is the 옛한글 character 'ᇿ' at 0x11FF

        return 0x1100 <= codepoint && codepoint <= 0x11FF;
    }

    private static final List<Character> CHOSEONG = Arrays.asList(
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
            'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    );

    private static final List<Character> JUNGSEONG = Arrays.asList(
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    );

    private static final List<Character> JONGSEONG = Arrays.asList(
            'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ',
            'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ',
            'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    );

    private static final Map<Character, String> SPLIT_TABLE = new HashMap<>();
    private static final Map<String, Character> JOIN_TABLE = new HashMap<>();

    static {
        SPLIT_TABLE.put('ㅘ', "ㅗㅏ");
        SPLIT_TABLE.put('ㅙ', "ㅗㅐ");
        SPLIT_TABLE.put('ㅚ', "ㅗㅣ");
        SPLIT_TABLE.put('ㅝ', "ㅜㅓ");
        SPLIT_TABLE.put('ㅞ', "ㅜㅔ");
        SPLIT_TABLE.put('ㅟ', "ㅜㅣ");
        SPLIT_TABLE.put('ㅢ', "ㅡㅣ");
        SPLIT_TABLE.put('ㄵ', "ㄴㅈ");
        SPLIT_TABLE.put('ㄺ', "ㄹㄱ");
        SPLIT_TABLE.put('ㄻ', "ㄹㅁ");
        SPLIT_TABLE.put('ㄼ', "ㄹㅂ");
        SPLIT_TABLE.put('ㄽ', "ㄹㅅ");
        SPLIT_TABLE.put('ㄾ', "ㄹㅌ");
        SPLIT_TABLE.put('ㄿ', "ㄹㅍ");
        SPLIT_TABLE.put('ㅀ', "ㄹㅎ");
        SPLIT_TABLE.put('ㅄ', "ㅂㅅ");

        for (Map.Entry<Character, String> entry : SPLIT_TABLE.entrySet()) {
            JOIN_TABLE.put(entry.getValue(), entry.getKey());
        }
    }
}
