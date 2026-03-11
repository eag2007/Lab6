package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerGenerateId;

import java.nio.channels.SocketChannel;
import java.util.Comparator;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class AddIfMax implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            if (value == null) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Не переданы данные элемента",
                        null
                );
                writeModule.writeResponseForClient(clientChannel, response);
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
                writeModule.writeResponseForClient(clientChannel, response);
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
                writeModule.writeResponseForClient(clientChannel, response);
            } else {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент не добавлен (не превышает максимальный)",
                        null
                );
                writeModule.writeResponseForClient(clientChannel, response);
            }
            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка при добавлении: " + e.getMessage(),
                        null
                );
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                System.out.println("Ошибка создания ResponsePacket");
            }
            return 500;
        }
    }
}