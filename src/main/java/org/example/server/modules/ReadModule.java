package org.example.server.modules;

import org.example.packet.CommandPacket;
import org.example.server.logger.ServerLogger;
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
            ServerLogger.info("Клиент отключился", clientChannel.getRemoteAddress());
            return null;
        }
        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        CommandPacket packet = ManagerDeserialize.deserialize(data);
        ServerLogger.debug("Получена команда: {} от {}", packet.getType(), clientChannel.getRemoteAddress());
        ServerLogger.debug("Получены аргументы: {} от {}", Arrays.toString(packet.getArgs()), clientChannel.getRemoteAddress());
        ServerLogger.debug("Получены значения: {} от {}", packet.getValues(), clientChannel.getRemoteAddress());

        return packet;
    }
}