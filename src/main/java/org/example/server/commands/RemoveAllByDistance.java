package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;

import java.nio.channels.SocketChannel;
import java.util.PriorityQueue;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class RemoveAllByDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            int distance = Integer.parseInt(args[0]);
            PriorityQueue<Route> routes = managerCollections.getCollectionsRoute();
            PriorityQueue<Route> routesNew = new PriorityQueue<>();
            int removedCount = 0;

            for (Route route : routes) {
                if (!route.getDistance().equals(distance)) {
                    routesNew.add(route);
                } else {
                    removedCount++;
                }
            }

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
                System.out.println("Ошибка создания ResponsePacket");
            }
            return 500;
        }
    }
}