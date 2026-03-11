package org.example.server.modules;

import org.example.packet.CommandPacket;
import org.example.server.managers.ManagerDeserialize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ReadModule {
    private static final int BUFFER_SIZE = 8192;

    public CommandPacket readPacketForServer(SocketChannel clientChannel) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.clear();

        int r = clientChannel.read(buffer);
        if (r == -1) {
            System.out.println("Клиент отключился " + clientChannel.getRemoteAddress());
            return null;
        }
        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        CommandPacket packet = ManagerDeserialize.deserialize(data);
        System.out.println("Получена команда: " + packet.getType() + " от " + clientChannel.getRemoteAddress());
        System.out.println("Получены аргументы: " + Arrays.toString(packet.getArgs()) + " от " + clientChannel.getRemoteAddress());
        System.out.println("Получены значения: " + packet.getValues() + " от " + clientChannel.getRemoteAddress());

        return packet;
    }
}