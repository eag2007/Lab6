package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.server.Server.managerCollections;

public class Info implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("size", managerCollections.getSizeCollections());
            info.put("initTime", managerCollections.getTimeInit().toString());

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Информация о коллекции",
                    info
            );
            byte[] data = ManagerSerialize.serialize(response);
            clientChannel.write(ByteBuffer.wrap(data));

            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка: " + e.getMessage(),
                        null
                );
                byte[] data = ManagerSerialize.serialize(error);
                clientChannel.write(ByteBuffer.wrap(data));
            } catch (Exception ex) {
                System.out.println("Ошибка создания ResponsePacket");
            }
            return 500;
        }
    }
}