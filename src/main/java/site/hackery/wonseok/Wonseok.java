package site.hackery.wonseok;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import site.hackery.wonseok.util.HangulParser;
import site.hackery.wonseok.util.keymap.Keymap;
import site.hackery.wonseok.util.keymap.KoreanKeymaps;

public class Wonseok {
    private static final Wonseok INSTANCE = new Wonseok();

    private final Keymap keymap = KoreanKeymaps.DUBEOLSIK;
    private boolean imeIsEnabled = false;

    private String handleInput(GuiTextField textField, String textToWrite) {
        if (StringUtils.isAlpha(textToWrite)) {
            int selectionStart = Math.min(textField.getSelectionEnd(), textField.getCursorPosition());
            int selectionEnd = Math.max(textField.getSelectionEnd(), textField.getCursorPosition());

            String currentText = textField.getText().substring(0, selectionStart);
            String afterSelectionText = textField.getText().substring(selectionEnd);

            char typedChar = textToWrite.charAt(0);
            int keyIndex = Keyboard.getKeyIndex(String.valueOf(Character.toUpperCase(typedChar)));

            Keymap.Key hangulKey = keymap.getKeymap().get(keyIndex);
            char hangulInput = GuiScreen.isShiftKeyDown() ? hangulKey.getShiftedInput() : hangulKey.getInput();

            if (currentText.toCharArray().length > 0) {
                char lastChar = currentText.toCharArray()[currentText.toCharArray().length - 1];
                if (HangulParser.withinHangulSyllables(lastChar) || HangulParser.withinHangulJamo(lastChar)) {
                    int cursorPos = textField.getCursorPosition(); // setText overwrites the cursor position, so save it here.
                    int selectionPos = textField.getSelectionEnd(); // Setting the cursor position sets the value of selectionPos, so save it here.

                    // Trim the character before the selection, since we're modifying it instead of adding to the text.
                    textField.setText(currentText.substring(0, currentText.length() - 1) + afterSelectionText);

                    textField.setCursorPosition(cursorPos - 1);
                    textField.setSelectionPos(selectionPos - 1);

                    return addToSyllable(lastChar, hangulInput);
                }
            }

            return String.valueOf(hangulInput);
        }

        return textToWrite;
    }

    private String addToSyllable(char syllable, char typedJamo) {
        StringBuilder jamo = new StringBuilder();

        if (HangulParser.withinHangulSyllables(syllable)) {
            for (char c : HangulParser.deconstruct(syllable)) {
                jamo.append(c);
            }
        } else {
            jamo.append(syllable);
        }

        jamo.append(typedJamo);

        return HangulParser.construct(jamo.toString().toCharArray());
    }

    // Parameters are ordered this way so that we can ALOAD 0 straight before invoking the method
    public static String writeTextHook(String textToWrite, GuiTextField textField) {
        if (GuiScreen.isShiftKeyDown() && textToWrite.equals(" ")) {
            INSTANCE.imeIsEnabled = !INSTANCE.imeIsEnabled;
            return "";
        }

        if (INSTANCE.imeIsEnabled) {
            return INSTANCE.handleInput(textField, textToWrite);
        } else {
            return textToWrite;
        }
    }
}
