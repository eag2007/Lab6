package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerGenerateId;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

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

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Объект добавлен в коллекцию",
                    route.getId()
            );

            writeModule.writeResponseForClient(clientChannel, response);
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
                ServerLogger.error("Ошибка создания ResponsePacket add");
            }
            return 500;
        }
    }
}