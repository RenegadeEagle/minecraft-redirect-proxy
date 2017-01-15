package co.renegadeeagle.mcproxy;

import co.renegadeeagle.mcproxy.codec.MinecraftDecoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static Settings settings = null;
    public static void main(String args[]) {
        loadSettings();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MinecraftDecoder());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(22000).sync(); // (7)

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    public static void loadSettings()  {
        File file = new File(System.getProperty("user.dir") + "/config.json");
        if(file.exists()) {
            Gson gson = new Gson();
            try {
                settings = gson.fromJson(new FileReader(file), Settings.class);
                System.out.println(settings.getPort());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            //Create default configuration file.
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Settings defaultSettings = new Settings();
            defaultSettings.setVersionName("ProxyCup");
            defaultSettings.setMaxPlayers(1337);
            defaultSettings.setOnlinePlayers(133);
            defaultSettings.setMotd("Couldn't connect to requested backend server. If you believe this to be an issue, contact the administrator of this proxy.");

            defaultSettings.setPort((short) 22000);

            List<Node> nodes = new ArrayList<>();
            Node exampleNode = new Node("localhost", "mc.hypixel.net", 25565);
            Node exampleNode2 = new Node("127.0.0.1", "mc.arkhamnetwork.org", 25565);
            nodes.add(exampleNode);
            nodes.add(exampleNode2);
            defaultSettings.setNodes(nodes);

            try {
                FileWriter writer = new FileWriter(file.getAbsolutePath());
                gson.toJson(defaultSettings, writer);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            settings = defaultSettings;
        }
    }

    public static Settings getSettings() {
        return settings;
    }

    public static EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public static EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }
}
