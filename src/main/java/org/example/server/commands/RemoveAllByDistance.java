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

public class RemoveAllByDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            int distance = Integer.parseInt(args[0]);

            long removedCount = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() == distance)
                    .count();

            PriorityQueue<Route> routesNew = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() != distance)
                    .collect(Collectors.toCollection(PriorityQueue::new));

            managerCollections.removeAllByDistanceCollections(routesNew);

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Удалено элементов: " + removedCount,
                    null
            );
            writeModule.writeResponseForClient(clientChannel, response);
            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка: " + e.getMessage(),
                        null
                );
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket remove_all_by_distance");
            }
            return 500;
        }
    }
}