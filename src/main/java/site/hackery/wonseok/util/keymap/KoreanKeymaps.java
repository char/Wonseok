package site.hackery.wonseok.util.keymap;

import org.lwjgl.input.Keyboard;

public class KoreanKeymaps {
    public static final Keymap DUBEOLSIK = new Keymap() {{
        char[][] topRow = new char[][] {
                {'ㅂ', 'ㅃ'}, {'ㅈ', 'ㅉ'}, {'ㄷ', 'ㄸ'}, {'ㄱ', 'ㄲ'}, {'ㅅ', 'ㅆ'},
                {'ㅛ'}, {'ㅕ'}, {'ㅑ'}, {'ㅐ', 'ㅒ'}, {'ㅔ', 'ㅖ'}
        };

        for (int i = 0; i < topRow.length; i++) {
            if (topRow[i].length == 1) {
                register(Keyboard.KEY_Q + i, topRow[i][0]);
            } else if (topRow[i].length == 2) {
                register(Keyboard.KEY_Q + i, topRow[i][0], topRow[i][1]);
            }
        }

        String middleRow = "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ";
        for (int i = 0; i < middleRow.length(); i++) {
            register(Keyboard.KEY_A + i, middleRow.charAt(i));
        }

        String bottomRow = "ㅋㅌㅊㅍㅠㅜㅡ";
        for (int i = 0; i < bottomRow.length(); i++) {
            register(Keyboard.KEY_Z + i, bottomRow.charAt(i));
        }
    }};

    // TODO: Romaja -> Hangul (somehow)
    // i.e annyeong -> 안녕
}
