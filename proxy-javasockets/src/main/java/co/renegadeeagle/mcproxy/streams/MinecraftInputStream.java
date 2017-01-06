package co.renegadeeagle.mcproxy.streams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MinecraftInputStream extends DataInputStream {

    public MinecraftInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads the next VarInt from this input stream.
     *
     * @return the next VarInt of this input stream.
     * @throws IOException the stream has been closed and the contained
     *                     input stream does not support reading after close, or
     *                     another I/O error occurs.
     */
    public int readVarInt() throws IOException {
        int target = 0;
        int length = 0;
        byte data;

        do {
            // Read in the next byte.
            data = readByte();

            // Append data to the target, ignoring the most significant bit.
            target |= (data & 0x7F) << (length * 7);

            // Throw exception if VarInt is too big.
            if (++length > 5) {
                throw new IOException("VarInt is too big");
            }

        } while ((data & 0x80) == 0x80); // Stop when most significant bit is set.

        return target;
    }

    /**
     * Reads the next {@link String} from this input stream.
     *
     * @return the next string of this input stream.
     * @throws IOException the stream has been closed and the contained
     *                     input stream does not support reading after close, or
     *                     another I/O error occurs.
     */
    public String readString() throws IOException {
        // Get the string length.
        int length = readVarInt();

        // Read in the string.
        byte[] target = new byte[length];
        readFully(target);

        // Return target byte array as a usable string.
        return new String(target);
    }
}
