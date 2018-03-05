package site.hackery.wonseok.launch;

import net.minecraft.launchwrapper.IClassTransformer;
import site.hackery.wonseok.patch.GuiTextFieldPatcher;

public class WonseokTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (GuiTextFieldPatcher.classMatches(name.replace('.', '/'))) {
            System.out.println("[Wonseok] Patching GuiTextField!");
            return new GuiTextFieldPatcher().patchClass(name, basicClass);
        }

        return basicClass;
    }
}
