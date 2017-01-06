package co.renegadeeagle.mcproxy.streams;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MinecraftOutputStream extends DataOutputStream {

    public MinecraftOutputStream(OutputStream out) {
        super(out);
    }

    public void writeVarInt(int value) throws IOException {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                writeByte(value);
                return;
            }

            writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }

    public void writeVarLong(long i) throws IOException {
        while ((i & 0xFFFFFFFFFFFFFF80L) != 0L) {
            write((int) ((i & 0x7F) | 0x80));
            i >>>= 7;
        }
        write((int) (i & 0x7F));
    }

    public void writeString(String s) throws IOException {
        byte[] b = s.getBytes();
        writeVarInt(b.length);
        write(b);
    }

    public void writePosition(int x, int y, int z) throws IOException {
        write(((x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF));
    }

    public void writeBlock(int blockId, byte blockData) throws IOException {
        writeVarInt(blockId << 4 | blockData);
    }
}