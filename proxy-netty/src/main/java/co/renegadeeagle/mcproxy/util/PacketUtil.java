package co.renegadeeagle.mcproxy.util;

import co.renegadeeagle.mcproxy.Main;
import co.renegadeeagle.mcproxy.Settings;
import co.renegadeeagle.mcproxy.codec.MinecraftDecoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.StringWriter;

public class PacketUtil {
    /**
     * @param protocolVersion protocol verison of connecting client, because we always want to show we are the right version.
     * @return
     */
    public static ByteBuf createStatusPacket(int protocolVersion) {
        Settings settings = Main.getSettings();
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try {
            writer.beginObject();
            writer.name("version").beginObject();
            writer.name("name").value(settings.getVersionName());
            writer.name("protocol").value(protocolVersion);
            writer.endObject();

            writer.name("players").beginObject();
            writer.name("max").value(settings.getMaxPlayers());
            writer.name("online").value(settings.getOnlinePlayers());
            writer.endObject();

            writer.name("description").beginObject();
            writer.name("text").value(settings.getMotd());
            writer.endObject();

            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuf buf = Unpooled.buffer();
        MinecraftDecoder.writeVarInt(0, buf);

        MinecraftDecoder.writeString(sw.toString(), buf);
        return buf;
    }
}
