package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class Show implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            List<Route> routes = managerCollections.getSortedCollections();

            ResponsePacket response;
            if (routes.isEmpty()) {
                response = new ResponsePacket(
                        200,
                        "Коллекция пуста",
                        routes
                );
            } else {
                response = new ResponsePacket(
                        200,
                        "Найдено элементов: " + routes.size(),
                        routes
                );
            }
            writeModule.writeResponseForClient(clientChannel, response);
            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка при получении коллекции: " + e.getMessage(),
                        null
                );
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket show");
            }
            return 500;
        }
    }
}