package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerGenerateId;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Comparator;

import static org.example.server.Server.managerCollections;

public class AddIfMax implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            if (value == null) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Не переданы данные элемента",
                        null
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
                return 400;
            }

            Route newRoute = new Route(ManagerGenerateId.generateId(),
                    value.getName(),
                    value.getCoordinates(),
                    value.getFrom(),
                    value.getTo(),
                    value.getDistance(),
                    value.getPrice());

            if (managerCollections.getCollectionsRoute().isEmpty()) {
                managerCollections.addCollections(newRoute);
                ResponsePacket response = new ResponsePacket(
                        200,
                        "Коллекция была пуста, элемент добавлен",
                        newRoute.getId()
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
                return 200;
            }

            Route maxRoute = managerCollections.getCollectionsRoute().stream()
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if (maxRoute != null && newRoute.compareTo(maxRoute) > 0) {
                managerCollections.addCollections(newRoute);
                ResponsePacket response = new ResponsePacket(
                        200,
                        "Элемент добавлен (превышает максимальный)",
                        newRoute.getId()
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
            } else {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент не добавлен (не превышает максимальный)",
                        null
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
            }

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