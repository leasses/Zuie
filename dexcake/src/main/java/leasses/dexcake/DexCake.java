package leasses.dexcake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.writer.builder.DexBuilder;
import com.android.tools.smali.dexlib2.writer.io.FileDataStore;
import com.android.tools.smali.smali.smaliFlexLexer;
import com.android.tools.smali.smali.smaliParser;
import com.android.tools.smali.smali.smaliTreeWalker;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Modifier;

public class DexCake {
    private static final String M_PUBLIC = "public ";
    private static final String M_PRIVATE = "private ";
    private static final String M_PROTECTED = "protected ";
    private static final String M_STATIC = "static ";
    private static final String M_FINAL = "final ";
    private static final String M_ABSTRACT = "abstract ";

    private static final String I_MOVE_16 = "move/16 ";
    private static final String I_MOVE_OBJECT = "move-object ";
    private static final String I_MOVE_FROM_16 = "move/from16 ";
    private static final String I_MOVE_OBJECT_FROM_16 = "move-object/from16 ";
    private static final String I_CONST = "const ";
    private static final String I_CONST_4 = "const/4 ";
    private static final String I_CONST_16 = "const/16 ";
    private static final String I_CONST_STRING = "const-string ";
    private static final String I_CONST_CLASS = "const-class ";
    private static final String I_CHECK_CAST = "check-cast ";

    private static final String I_I_GET = "iget ";
    private static final String I_I_GET_BOOLEAN = "iget-boolean ";
    private static final String I_I_GET_BYTE = "iget-byte ";
    private static final String I_I_GET_CHAR = "iget-char ";
    private static final String I_I_GET_OBJECT = "iget-object ";
    private static final String I_I_GET_SHORT = "iget-short ";
    private static final String I_I_GET_WIDE = "iget-wide ";

    private static final String I_S_GET = "sget ";
    private static final String I_S_GET_BOOLEAN = "sget-boolean ";
    private static final String I_S_GET_BYTE = "sget-byte ";
    private static final String I_S_GET_CHAR = "sget-char ";
    private static final String I_S_GET_OBJECT = "sget-object ";
    private static final String I_S_GET_SHORT = "sget-short ";
    private static final String I_S_GET_WIDE = "sget-wide ";

    private static final String I_I_PUT = "iput ";
    private static final String I_I_PUT_BOOLEAN = "iput-boolean ";
    private static final String I_I_PUT_BYTE = "iput-byte ";
    private static final String I_I_PUT_CHAR = "iput-char ";
    private static final String I_I_PUT_OBJECT = "iput-object ";
    private static final String I_I_PUT_SHORT = "iput-short ";
    private static final String I_I_PUT_WIDE = "iput-wide ";

    private static final String I_S_PUT = "sput ";
    private static final String I_S_PUT_BOOLEAN = "sput-boolean ";
    private static final String I_S_PUT_BYTE = "sput-byte ";
    private static final String I_S_PUT_CHAR = "sput-char ";
    private static final String I_S_PUT_OBJECT = "sput-object ";
    private static final String I_S_PUT_SHORT = "sput-short ";
    private static final String I_S_PUT_WIDE = "sput-wide ";

    private static final String I_NEW_ARRAY = "new-array ";
    private static final String I_FILLED_NEW_ARRAY = "filled-new-array ";
    private static final String I_FILLED_NEW_ARRAY_RANGE = "filled-new-array/range ";

    private static final String I_A_PUT_OBJECT = "aput-object ";
    private static final String I_A_GET_OBJECT = "aget-object ";
    private static final String I_MOVE_RESULT = "move-result ";
    private static final String I_MOVE_RESULT_WIDE = "move-result-wide ";
    private static final String I_MOVE_RESULT_OBJECT = "move-result-object ";
    private static final String I_NEW_INSTANCE = "new-instance ";
    private static final String I_THROW = "throw ";
    private static final String I_INVOKE_VIRTUAL = "invoke-virtual ";
    private static final String I_INVOKE_VIRTUAL_RANGE = "invoke-virtual/range ";
    private static final String I_INVOKE_DIRECT = "invoke-direct ";
    private static final String I_INVOKE_DIRECT_RANGE = "invoke-direct/range ";
    private static final String I_INVOKE_STATIC = "invoke-static ";
    private static final String I_INVOKE_STATIC_RANGE = "invoke-static/range ";
    private static final String I_GOTO = "goto ";
    private static final String I_RETURN = "return ";
    private static final String I_RETURN_VOID = "return-void ";
    private static final String I_RETURN_OBJECT = "return-object ";
    private static final String I_PACKAGE_SWITCH = "packed-switch ";

    private static final String D_CLASS = ".class ";
    private static final String D_SUPER = ".super ";
    private static final String D_SOURCE = ".source ";
    private static final String D_METHOD = ".method ";
    private static final String D_REGISTERS = ".registers ";
    private static final String D_END_METHOD = ".end method";
    private static final String D_PACKAGE_SWITCH = ".packed-switch ";
    private static final String D_END_PACKAGE_SWITCH = ".end packed-switch";

    private static final char LABEL_PREFIX = ':';
    private static final String CTR_NAME = "constructor <init>";
    private static final String STATIC_CTR_NAME = "constructor <init>";
    private final StringBuilder c = new StringBuilder(6666);
    private int lateConstIndex = -1;
    private int registerIndex = -1;
    private int registerCount = -1;
    private int paramNum = -1;


    public DexCake(int modifier, @NonNull Type name, @NonNull Type sup) {
        c.append(D_CLASS);
        adMod(c, modifier);
        c.append(name);
        c.append(D_SUPER).append(sup);
        c.append(D_SOURCE);
        adStr2(c, name.toString(), ".generated");
        c.append('\n');
    }

    private static void adStr(@NonNull StringBuilder sb, @NonNull String s) {
        sb.append('"').append(s).append('"');
    }

    private static void adStr2(@NonNull StringBuilder sb, @Nullable String s1, @Nullable String s2) {
        sb.append('"').append(s1).append(s2).append('"');
    }

    private static void adMod(@NonNull StringBuilder sb, int mod) {
        if (Modifier.isPublic(mod)) sb.append(M_PUBLIC);
        if (Modifier.isProtected(mod)) sb.append(M_PROTECTED);
        if (Modifier.isPrivate(mod)) sb.append(M_PRIVATE);

        if (Modifier.isAbstract(mod)) sb.append(M_ABSTRACT);
        if (Modifier.isStatic(mod)) sb.append(M_STATIC);
        if (Modifier.isFinal(mod)) sb.append(M_FINAL);
    }

    private static void adLab(@NonNull StringBuilder sb, String lab, int suffix) {
        sb.append(LABEL_PREFIX).append(lab);
        if (suffix != -1) sb.append(suffix);
    }

    private void adReg(int reg) {
        c.append(reg >= 0 ? 'v' : 'p').append(reg >= 0 ? reg : -reg - 1);
        registerCount = Math.max(registerCount, reg + paramNum + 1);
    }

    public DexCake constructor(int mod, @NonNull Type... paramTypes) {
        return method(mod, CTR_NAME, Type.VOID, paramTypes);
    }

    public DexCake staticConstructor(int mod, @NonNull Type... paramTypes) {
        return method(mod, STATIC_CTR_NAME, Type.VOID, paramTypes);
    }

    public DexCake method(int mod, String name,
                          @NonNull Type returnType, @NonNull Type... paramTypes) {
        c.append(D_METHOD);
        adMod(c, mod);
        c.append(name)
                .append('(');
        for (Type t : paramTypes) c.append(t);
        c.append(")").append(returnType).append('\n');

        c.append(D_REGISTERS).append('\n');

        registerIndex = c.length() - 1;
        paramNum = (Modifier.isStatic(mod) ? 0 : 1) + paramTypes.length;
        registerCount = paramNum;
        return this;
    }

    public DexCake move16(int fromReg, int toReg) {
        c.append(I_MOVE_16);
        adReg(toReg);
        c.append(',');
        adReg(fromReg);
        c.append('\n');
        return this;
    }

    public DexCake moveObject(int fromReg, int toReg) {
        c.append(I_MOVE_OBJECT);
        adReg(toReg);
        c.append(',');
        adReg(fromReg);
        c.append('\n');
        return this;
    }

    public DexCake moveFrom16(int fromReg, int toReg) {
        c.append(I_MOVE_FROM_16);
        adReg(toReg);
        c.append(',');
        adReg(fromReg);
        c.append('\n');
        return this;
    }

    public DexCake moveObjectFrom16(int fromReg, int toReg) {
        c.append(I_MOVE_OBJECT_FROM_16);
        adReg(toReg);
        c.append(',');
        adReg(fromReg);
        c.append('\n');
        return this;
    }

    public DexCake const4(int reg, int n) {
        c.append(I_CONST_4);
        adReg(reg);
        c.append(',').append(n).append('\n');
        return this;
    }

    public DexCake const16(int reg, int n) {
        c.append(I_CONST_16);
        adReg(reg);
        c.append(',').append(n).append('\n');
        return this;
    }

    public DexCake lateConst16(int reg) {
        c.append(I_CONST_16);
        adReg(reg);
        c.append(',');
        lateConstIndex = c.length();
        c.append('\n');
        return this;
    }

    public DexCake const16late(int n) {
        c.insert(lateConstIndex, n);
        return this;
    }

    public DexCake endMethod() {
        c.append(D_END_METHOD).insert(registerIndex, registerCount).append('\n');
        return this;
    }

    public DexCake moveResult(int reg) {
        c.append(I_MOVE_RESULT);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake moveResultWide(int reg) {
        c.append(I_MOVE_RESULT_WIDE);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake moveResultObject(int reg) {
        c.append(I_MOVE_RESULT_OBJECT);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake constString(int reg, @Nullable String s) {
        if (s == null) return const16(reg, 0);

        c.append(I_CONST_STRING);
        adReg(reg);
        c.append(',');
        adStr(c, s);
        c.append('\n');
        return this;
    }

    public DexCake constClass(int reg, @NonNull Type t) {
        c.append(I_CONST_CLASS);
        adReg(reg);
        c.append(',').append(t);
        c.append('\n');
        return this;
    }

    public DexCake checkCast(int reg, @NonNull Type t) {
        if (t.isPrimitive())
            throw new IllegalArgumentException("Cannot do check-cast for a primitive type: " + t);
        c.append(I_CHECK_CAST);
        adReg(reg);
        c.append(',').append(t);
        c.append('\n');
        return this;
    }

    public DexCake iGet(int objReg,
                        int resultReg,
                        @NonNull Type clz,
                        @NonNull String name,
                        @NonNull Type type) {
        c.append(type.fitInstruction(
                I_I_GET,
                I_I_GET_BOOLEAN,
                I_I_GET_BYTE,
                I_I_GET_CHAR,
                I_I_GET_OBJECT,
                I_I_GET_SHORT,
                I_I_GET_WIDE));
        adReg(resultReg);
        c.append(',');
        adReg(objReg);
        c.append(',').append(clz).append("->").append(name).append(':').append(type);
        c.append('\n');
        return this;
    }

    public DexCake iPut(int objReg,
                        int resultReg,
                        @NonNull Type clz,
                        @NonNull String name,
                        @NonNull Type type) {
        c.append(type.fitInstruction(
                I_I_PUT,
                I_I_PUT_BOOLEAN,
                I_I_PUT_BYTE,
                I_I_PUT_CHAR,
                I_I_PUT_OBJECT,
                I_I_PUT_SHORT,
                I_I_PUT_WIDE));
        adReg(resultReg);
        c.append(',');
        adReg(objReg);
        c.append(',').append(clz).append("->").append(name).append(':').append(type);
        c.append('\n');
        return this;
    }

    public DexCake sGet(int resultReg,
                        @NonNull Type clz,
                        @NonNull String name,
                        @NonNull Type type) {
        c.append(type.fitInstruction(
                I_S_GET,
                I_S_GET_BOOLEAN,
                I_S_GET_BYTE,
                I_S_GET_CHAR,
                I_S_GET_OBJECT,
                I_S_GET_SHORT,
                I_S_GET_WIDE));
        adReg(resultReg);
        c.append(',');
        c.append(clz).append("->").append(name).append(':').append(type);
        c.append('\n');
        return this;
    }

    public DexCake sPut(int resultReg,
                        @NonNull Type clz,
                        @NonNull String name,
                        @NonNull Type type) {
        c.append(type.fitInstruction(
                I_S_PUT,
                I_S_PUT_BOOLEAN,
                I_S_PUT_BYTE,
                I_S_PUT_CHAR,
                I_S_PUT_OBJECT,
                I_S_PUT_SHORT,
                I_S_PUT_WIDE));
        adReg(resultReg);
        c.append(',');
        c.append(clz).append("->").append(name).append(':').append(type);
        c.append('\n');
        return this;
    }

    public DexCake wrap(int reg, @NonNull Type type) {
        invokeStatic(Type.of("leasses.anlua.api.CLight"), "of", Type.of("leasses.anlua.api.CLight"),
                type.isWide() ? new int[]{reg, reg + 1} : new int[]{reg},
                type.isPrimitive() ? type : Type.OBJECT);
        return this;
    }

    public DexCake newInstance(int reg, @NonNull Type t) {
        c.append(I_NEW_INSTANCE);
        adReg(reg);
        c.append(',').append(t);
        c.append('\n');
        return this;
    }

    public DexCake throw0(int reg) {
        c.append(I_THROW);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake invokeDirect(Type clz,
                                String method,
                                Type ret,
                                @NonNull int[] regs,
                                @NonNull Type... params) {
        c.append(I_INVOKE_DIRECT).append("{ ");
        for (var r : regs) {
            adReg(r);
            c.append(',');
        }
        c.deleteCharAt(c.length() - 1).append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var p : params) c.append(p);
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake invokeDirectRange(Type clz,
                                     String method,
                                     Type ret,
                                     int fromReg,
                                     int toReg,
                                     @NonNull Type... params) {
        c.append(I_INVOKE_DIRECT_RANGE).append("{ ");
        adReg(fromReg);
        c.append("..");
        adReg(toReg);
        c.append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var param : params) {
            c.append(param);
        }
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake invokeVirtual(Type clz,
                                 String method,
                                 Type ret,
                                 @NonNull int[] regs,
                                 @NonNull Type... params) {
        c.append(I_INVOKE_VIRTUAL).append("{ ");
        for (var r : regs) {
            adReg(r);
            c.append(',');
        }
        c.deleteCharAt(c.length() - 1).append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var p : params) c.append(p);
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake invokeVirtualRange(Type clz,
                                      String method,
                                      Type ret,
                                      int fromReg,
                                      int toReg,
                                      @NonNull Type... params) {
        c.append(I_INVOKE_VIRTUAL_RANGE).append("{ ");
        adReg(fromReg);
        c.append("..");
        adReg(toReg);
        c.append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var param : params) {
            c.append(param);
        }
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake invokeStatic(Type clz,
                                String method,
                                Type ret,
                                @NonNull int[] regs,
                                @NonNull Type... params) {
        c.append(I_INVOKE_STATIC).append("{ ");
        for (var r : regs) {
            adReg(r);
            c.append(',');
        }
        c.deleteCharAt(c.length() - 1).append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var p : params) c.append(p);
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake invokeStaticRange(Type clz,
                                     String method,
                                     Type ret,
                                     int fromReg,
                                     int toReg,
                                     @NonNull Type... params) {
        c.append(I_INVOKE_STATIC_RANGE).append("{ ");
        adReg(fromReg);
        c.append("..");
        adReg(toReg);
        c.append("},")
                .append(clz)
                .append("->")
                .append(method)
                .append('(');
        for (var param : params) {
            c.append(param);
        }
        c.append(')').append(ret).append('\n');
        return this;
    }

    public DexCake newArray(Type t, int resultReg, int lengthReg) {
        c.append(I_NEW_ARRAY);
        adReg(resultReg);
        c.append(',');
        adReg(lengthReg);
        c.append(',').append(t).append('\n');
        return this;
    }

    public DexCake filledNewArrayRange(Type t, int fromReg, int toReg) {
        c.append(I_FILLED_NEW_ARRAY_RANGE).append("{ ");
        adReg(fromReg);
        c.append("..");
        adReg(toReg);
        c.append("},").append(t).append('\n');
        return this;
    }

    public DexCake filledNewArray(Type t, @NonNull int... regs) {
        c.append(I_FILLED_NEW_ARRAY).append("{ ");
        for (int r : regs) {
            adReg(r);
            c.append(',');
        }
        c.deleteCharAt(c.length() - 1).append("},").append(t).append('\n');
        return this;
    }

    public DexCake aGetObject(int resultReg, int arrReg, int idxReg) {
        c.append(I_A_GET_OBJECT);
        adReg(resultReg);
        c.append(',');
        adReg(arrReg);
        c.append(',');
        adReg(idxReg);
        c.append('\n');
        return this;
    }

    public DexCake aPutObject(int eleReg, int arrReg, int idxReg) {
        c.append(I_A_PUT_OBJECT);
        adReg(eleReg);
        c.append(',');
        adReg(arrReg);
        c.append(',');
        adReg(idxReg);
        c.append('\n');
        return this;
    }

    public DexCake returnVoid() {
        c.append(I_RETURN_VOID).append('\n');
        return this;
    }

    public DexCake goto0(String lab) {
        c.append(I_GOTO);
        adLab(c, lab, -1);
        c.append('\n');
        return this;
    }

    public DexCake return0(int reg) {
        c.append(I_RETURN);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake returnObject(int reg) {
        c.append(I_RETURN_OBJECT);
        adReg(reg);
        c.append('\n');
        return this;
    }

    public DexCake label(String lab) {
        adLab(c, lab, -1);
        c.append('\n');
        return this;
    }

    public DexCake label(String lab, int suffix) {
        adLab(c, lab, suffix);
        c.append('\n');
        return this;
    }

    public DexCake packedSwitch(int reg, String payload) {
        c.append(I_PACKAGE_SWITCH);
        adReg(reg);
        c.append(',');
        adLab(c, payload, -1);
        c.append('\n');
        return this;
    }

    public DexCake packedSwitchData(int offset) {
        c.append(D_PACKAGE_SWITCH)
                .append(offset)
                .append('\n');
        return this;
    }

    public DexCake endPackedSwitchData() {
        c.append(D_END_PACKAGE_SWITCH)
                .append('\n');
        return this;
    }

    public String build() {
        return c.toString();
    }

    public void strawberry(File output) throws Exception {
        final var apiLevel = 21;
        var dexBuilder = new DexBuilder(Opcodes.forApi(apiLevel));

        var reader = new StringBuilderReader(c);

        var tokens = new CommonTokenStream(new smaliFlexLexer(reader, apiLevel));
        var parser = new smaliParser(tokens);
        parser.setApiLevel(apiLevel);

        var treeStream = new CommonTreeNodeStream(parser.smali_file().getTree());
        treeStream.setTokenStream(tokens);

        var dexGen = new smaliTreeWalker(treeStream);
        dexGen.setApiLevel(apiLevel);
        dexGen.setDexBuilder(dexBuilder);
        dexGen.smali_file();

        dexBuilder.writeTo(new FileDataStore(output));
    }

    private static class StringBuilderReader extends Reader {
        private final int length;
        private StringBuilder str;
        private int next = 0;

        StringBuilderReader(@NonNull StringBuilder s) {
            this.str = s;
            this.length = s.length();
        }

        private void ensureOpen() throws IOException {
            if (str == null)
                throw new IOException("Stream closed");
        }

        @Override
        public int read(char[] buff, int off, int len) throws IOException {
            synchronized (this) {
                ensureOpen();
                if ((off < 0) || (off > buff.length) || (len < 0) ||
                        ((off + len) > buff.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return 0;
                }
                if (next >= length)
                    return -1;
                int n = Math.min(length - next, len);
                str.getChars(next, next + n, buff, off);
                next += n;
                return n;
            }
        }

        @Override
        public void close() {
            str = null;
        }
    }

}
