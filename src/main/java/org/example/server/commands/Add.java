package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerGenerateId;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;

public class Add implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            Route route = new Route(ManagerGenerateId.generateId(),
                    value.getName(),
                    value.getCoordinates(),
                    value.getFrom(),
                    value.getTo(),
                    value.getDistance(),
                    value.getPrice());

            managerCollections.addCollections(route);

            // Создаем ResponsePacket с данными
            ResponsePacket response = new ResponsePacket(
                    200,
                    "Объект добавлен в коллекцию",
                    route.getId()
            );

            // Сериализуем и отправляем
            byte[] data = ManagerSerialize.serialize(response);
            clientChannel.write(ByteBuffer.wrap(data));

            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка при добавлении: " + e.getMessage(),
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