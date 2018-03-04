package site.hackery.wonseok.util.keymap;

import java.util.HashMap;
import java.util.Map;

public class Keymap {
    private final Map<Integer, Key> keymap = new HashMap<>();

    public void register(int keycode, char input, char shiftedInput) {
        keymap.put(keycode, new Key(keycode, input, shiftedInput));
    }

    public void register(int keycode, char input) {
        keymap.put(keycode, new Key(keycode, input));
    }

    public Map<Integer, Key> getKeymap() {
        return keymap;
    }

    public static class Key {
        private final int keycode;
        private final char input;
        private final char shiftedInput;

        public Key(int keycode, char input) {
            this(keycode, input, input);
        }

        public Key(int keycode, char input, char shiftedInput) {
            this.keycode = keycode;
            this.input = input;
            this.shiftedInput = shiftedInput;
        }

        public int getKeycode() {
            return keycode;
        }

        public char getInput() {
            return input;
        }

        public char getShiftedInput() {
            return shiftedInput;
        }
    }
}
