package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

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
                writeModule.writeResponseForClient(clientChannel, response);
                return 200;
            } else {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент с id " + id + " не найден",
                        null
                );
                writeModule.writeResponseForClient(clientChannel, response);
                return 400;
            }

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка: " + e.getMessage(),
                        null
                );
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket remove_by_id");
            }
            return 500;
        }
    }
}