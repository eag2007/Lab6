package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class FilterLessThanDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            int distance = Integer.parseInt(args[0]);

            List<Route> result = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() < distance)
                    .collect(Collectors.toCollection(ArrayList<Route>::new));

            if (result.isEmpty()) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Нет элементов с distance меньше " + distance,
                        result
                );
                writeModule.writeResponseForClient(clientChannel, response);
                return 400;
            }

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Найдено элементов: " + result.size(),
                    result
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
                ServerLogger.error("Ошибка создания ResponsePacket filter_less_than_distance");
            }
            return 500;
        }
    }
}