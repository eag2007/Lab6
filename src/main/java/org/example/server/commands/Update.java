package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class Update implements Command {
    public int executeCommand(String[] args, RouteClient newRoute, SocketChannel clientChannel) {
        try {
            long id = Long.parseLong(args[0]);

            if (newRoute == null) {
                Route existingRoute = managerCollections.getCollectionsRoute().stream()
                        .filter(route -> route.getId() == id)
                        .findFirst()
                        .orElse(null);

                ResponsePacket response;
                if (existingRoute != null) {
                    response = new ResponsePacket(200, "Элемент с id " + id + " найден", existingRoute);
                    writeModule.writeResponseForClient(clientChannel, response);
                    return 200;
                } else {
                    response = new ResponsePacket(400, "Элемент с id " + id + " не найден", null);
                    writeModule.writeResponseForClient(clientChannel, response);
                    return 400;
                }
            }

            else {
                PriorityQueue<Route> updatedRoutes = managerCollections.getCollectionsRoute().stream()
                        .map(route -> {
                            if (route.getId() == id) {
                                return new Route(id,
                                        newRoute.getName(),
                                        newRoute.getCoordinates(),
                                        route.getCreationDate(),
                                        newRoute.getFrom(),
                                        newRoute.getTo(),
                                        newRoute.getDistance(),
                                        newRoute.getPrice());
                            }
                            return route;
                        })
                        .collect(Collectors.toCollection(PriorityQueue::new));
                managerCollections.clearCollections();
                updatedRoutes.forEach(managerCollections::addCollections);

                ResponsePacket response = new ResponsePacket(200, "Элемент с id " + id + " обновлен", null);
                writeModule.writeResponseForClient(clientChannel, response);
                return 200;
            }

        } catch (Exception e) {
            ServerLogger.error("Ошибка в update: {}", e.getMessage());
            try {
                ResponsePacket error = new ResponsePacket(500, "Ошибка: " + e.getMessage(), null);
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket update {}", ex.getMessage());
            }
            return 500;
        }
    }
}