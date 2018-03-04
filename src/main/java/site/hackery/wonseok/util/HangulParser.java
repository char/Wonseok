package site.hackery.wonseok.util;

import java.util.*;

public class HangulParser {
    /**
     * Atomically deconstructs a syllable into its constituent 자모.
     * This method is <b>atomic</b>, meaning that vowels like 'ㅘ' are split into 'ㅗ, ㅏ' and
     * consonant blocks like 'ㄼ' are split into 'ㄹ, ㅂ'.
     *
     * @param syllable A complete Hangul syllable.
     * @return An array of constituent 자모. (technically 자소)
     */
    public static char[] deconstruct(char syllable) {
        if (withinHangulSyllables(syllable)) {
            char[] deconstructed = deconstructNonAtomic(syllable);

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
     * Deconstructs a syllable into its constituent Unicode 자모.
     * This method is <b>non-atomic</b>, meaning that vowels like ㅘ will not be split into 'ㅗ, ㅏ',
     * and consonant blocks like ㄼ will not be split into 'ㄹ, ㅂ'.
     *
     * @param syllable A complete Hangul syllable
     * @return An array of constituent 자모.
     */
    public static char[] deconstructNonAtomic(char syllable) {
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
     * Atomically constructs a syllable from its constituent 자모
     *
     * @param jamo An array of basic 자모.
     * @return A complete Hangul syllable.
     */
    public static String construct(char[] jamo) {
        jamo = joinJamo(jamo);

        StringBuilder builder = new StringBuilder();

        int i = 0;
        while (i <= jamo.length) {
            int nextSize = calculateNextSyllableSize(jamo, i);

            if (nextSize == 0) {
                for (int j = i; j < jamo.length; j++) {
                    builder.append(jamo[j]);
                }

                break;
            }

            char syllable = constructInternal(jamo, i, i + nextSize);

            if (syllable != 0) {
                builder.append(syllable);
            } else {
                for (int j = 0; j < nextSize; j++)
                    builder.append(jamo[i + j]);
            }

            i += nextSize;
        }

        return builder.toString();
    }

    private static char constructInternal(char[] jamo, int startIndex, int endIndex) {
        int length = endIndex - startIndex;

        if (length != 2 && length != 3) {
            return 0x00;
        }

        int choseong = CHOSEONG.indexOf(jamo[startIndex]);
        int jungseong = JUNGSEONG.indexOf(jamo[startIndex + 1]);

        if (choseong == -1 || jungseong == -1)
            return 0x00;

        if (length == 3) {
            int jongseong = JONGSEONG.indexOf(jamo[startIndex + 2]) + 1;

            if (jongseong == -1)
                return 0x00;

            return (char) (0xAC00 + choseong * 21 * 28 + jungseong * 28 + jongseong);
        } else {
            return (char) (0xAC00 + choseong * 21 * 28 + jungseong * 28);
        }
    }

    private static int calculateNextSyllableSize(char[] jamo, int offset) {
        final int remainingJamo = jamo.length - offset;

        if (remainingJamo > 2) {
            if (JONGSEONG.contains(jamo[offset + 2])) {
                if (remainingJamo > 3 && JUNGSEONG.contains(jamo[offset + 3]))
                    return 2;

                return 3;
            } else {
                return 2;
            }
        }

        if (remainingJamo == 2)
            return remainingJamo;

        return 0;
    }

    private static char[] joinJamo(char[] jamo) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < jamo.length; i++) {
            char c = jamo[i];

            if (i >= jamo.length - 1) {
                builder.append(c);
            } else {
                char next = jamo[i + 1];

                String agglomerate = String.valueOf(c) + String.valueOf(next);

                boolean shouldJoin = false;

                if (JOIN_TABLE.containsKey(agglomerate)) {
                    shouldJoin = true;

                    if (i < jamo.length - 2) {
                        char after = jamo[i + 2];
                        if (JUNGSEONG.contains(after)) {
                            shouldJoin = false;
                        }
                    }
                }

                if (shouldJoin) {
                    builder.append(JOIN_TABLE.get(agglomerate));
                    i++;
                } else {
                    builder.append(c);
                }
            }
        }

        return builder.toString().toCharArray();
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

        return 0x3130 <= codepoint && codepoint <= 0x318F;
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
