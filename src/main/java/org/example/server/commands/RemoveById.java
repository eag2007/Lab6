package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;

public class RemoveById implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            Long id = Long.parseLong(args[0]);

            boolean removed = managerCollections.getCollectionsRoute()
                    .removeIf(route -> route.getId() == id);

            if (removed) {
                ResponsePacket response = new ResponsePacket(
                        200,
                        "Элемент с id " + id + " удален",
                        null
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
                return 200;
            } else {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент с id " + id + " не найден",
                        null
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
                return 400;
            }

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