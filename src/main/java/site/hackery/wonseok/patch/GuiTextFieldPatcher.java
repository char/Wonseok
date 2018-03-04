package site.hackery.wonseok.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiTextFieldPatcher {
    private static final String GUITEXTFIELD_NOTCH = "bjc";
    private static final String GUITEXTFIELD_WRITETEXT_NOTCH = "b"; // (Ljava/lang/String;)V

    private static final String GUITEXTFIELD_MCP = "net/minecraft/client/gui/GuiTextField";
    private static final String GUITEXTFIELD_WRITETEXT_MCP = "writeText";

    public byte[] patchClass(String name, byte[] classBuffer) {
        ClassReader reader = new ClassReader(classBuffer);
        ClassNode classNode = new ClassNode();

        try {
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        } catch (Exception ignored) {
            try {
                reader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.equals("(Ljava/lang/String;)V") && methodNode.name.equals(GUITEXTFIELD_WRITETEXT_MCP)) {
                for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            // TODO: hook writeText
                        }
                    }
                }
            }
        }


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    public static boolean classMatches(String className) {
        return className.equals(GUITEXTFIELD_NOTCH) || className.equals(GUITEXTFIELD_MCP);
    }
}
