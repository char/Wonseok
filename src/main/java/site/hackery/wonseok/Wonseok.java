package site.hackery.wonseok;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import site.hackery.wonseok.util.HangulParser;
import site.hackery.wonseok.util.keymap.Keymap;
import site.hackery.wonseok.util.keymap.KoreanKeymaps;

import java.util.LinkedList;
import java.util.List;

public class Wonseok {
    private static Wonseok INSTANCE = new Wonseok();

    private Keymap keymap = KoreanKeymaps.DUBEOLSIK;
    private boolean imeIsEnabled = false;

    private String handleInput(GuiTextField textField, String textToWrite) {
        if (StringUtils.isAlpha(textToWrite)) {
            int selectionStart = Math.min(textField.getSelectionEnd(), textField.getCursorPosition());
            int selectionEnd = Math.max(textField.getSelectionEnd(), textField.getCursorPosition());

            String currentText = textField.getText().substring(selectionStart);
            String afterSelectionText = textField.getText().substring(selectionEnd);


            char typedChar = textToWrite.charAt(0);
            char lastChar = currentText.toCharArray()[currentText.toCharArray().length - 1];

            int keyIndex = Keyboard.getKeyIndex(String.valueOf(Character.toUpperCase(typedChar)));

            Keymap.Key hangulKey = keymap.getKeymap().get(keyIndex);
            char hangulInput = GuiScreen.isShiftKeyDown() ? hangulKey.getShiftedInput() : hangulKey.getInput();

            if (HangulParser.withinHangulSyllables(lastChar) || HangulParser.withinHangulJamo(lastChar)) {
                int cursorPos = textField.getCursorPosition(); // setText overwrites the cursor position, so save it here.
                int selectionPos = textField.getSelectionEnd(); // Setting the cursor position sets the value of selectionPos, so save it here.

                // Trim the character before the selection, since we're modifying it instead of adding to the text.
                textField.setText(currentText.substring(0, currentText.length() - 1) + afterSelectionText);

                textField.setCursorPosition(cursorPos - 1);
                textField.setSelectionPos(selectionPos - 1);

                return addToSyllable(lastChar, hangulInput);
            } else {
                return String.valueOf(hangulKey.getInput());
            }
        }

        return textToWrite;
    }

    private String addToSyllable(char syllable, char typedJamo) {
        StringBuilder jamo = new StringBuilder();

        if (HangulParser.withinHangulSyllables(syllable)) {
            for (char c : HangulParser.deconstructAtomic(syllable)) {
                jamo.append(c);
            }
        } else {
            jamo.append(syllable);
        }

        jamo.append(typedJamo);

        char newSyllable = HangulParser.constructAtomic(jamo.toString().toCharArray());

        if (newSyllable != 0x00) {
            return String.valueOf(newSyllable);
        } else {
            return String.valueOf(syllable) + String.valueOf(typedJamo);
        }
    }

    public static void textboxKeyTyped(int keyCode) {
        if (keyCode == Keyboard.KEY_RMENU)
            INSTANCE.imeIsEnabled ^= true; // Flip boolean
    }

    public static String writeTextHook(GuiTextField textField, String textToWrite) {
        if (INSTANCE.imeIsEnabled) {
            return INSTANCE.handleInput(textField, textToWrite);
        }

        return textToWrite;
    }
}
