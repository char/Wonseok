package site.hackery.wonseok.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiTextFieldPatcher {
    private static final String GUITEXTFIELD_NOTCH = "bjc";
    private static final String GUITEXTFIELD_WRITETEXT_NOTCH = "b"; // (Ljava/lang/String;)V

    private static final String GUITEXTFIELD_MCP = "net/minecraft/client/gui/GuiTextField";
    private static final String GUITEXTFIELD_TEXTBOXKEYTYPED_MCP = "textboxKeyTyped";
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
            if (methodNode.desc.equals("(CI)Z") && methodNode.name.equals(GUITEXTFIELD_TEXTBOXKEYTYPED_MCP)) {
                for (int i = methodNode.instructions.size() - 1; i >= 0; i--) {
                    // Loop backwards because we want to hook only the *last* instance of writeText.

                    AbstractInsnNode insn = methodNode.instructions.get(i);

                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            if (methodInsn.name.equals(GUITEXTFIELD_WRITETEXT_MCP) && methodInsn.desc.equals("(Ljava/lang/String;)V")) {
                                InsnList instructions = new InsnList();

                                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "site/hackery/wonseok/Wonseok", "writeTextHook", String.format("(Ljava/lang/String;L%s;)Ljava/lang/String;", GUITEXTFIELD_MCP), false));

                                methodNode.instructions.insertBefore(insn, instructions);
                                break;
                            }
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
