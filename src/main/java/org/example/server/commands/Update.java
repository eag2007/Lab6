package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.PriorityQueue;

import static org.example.server.Server.managerCollections;

public class Update implements Command {
    public int executeCommand(String[] args, RouteClient newRoute, SocketChannel clientChannel) {
        try {
            long id = Long.parseLong(args[0]);

            if (newRoute == null) {
                boolean exists = managerCollections.getCollectionsRoute().stream()
                        .anyMatch(route -> route.getId() == id);

                ResponsePacket response;
                if (exists) {
                    response = new ResponsePacket(200, "Элемент с id " + id + " найден", null);
                } else {
                    response = new ResponsePacket(400, "Элемент с id " + id + " не найден", null);
                }

                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));

                return exists ? 200 : 400;
            }

            else {
                PriorityQueue<Route> routes = managerCollections.getCollectionsRoute();
                boolean found = false;

                PriorityQueue<Route> updatedRoutes = new PriorityQueue<>();
                for (Route route : routes) {
                    if (route.getId() == id) {
                        Route updatedRoute = new Route(id,
                                newRoute.getName(),
                                newRoute.getCoordinates(),
                                route.getCreationDate(),
                                newRoute.getFrom(),
                                newRoute.getTo(),
                                newRoute.getDistance(),
                                newRoute.getPrice());
                        updatedRoutes.add(updatedRoute);
                        found = true;
                    } else {
                        updatedRoutes.add(route);
                    }
                }

                if (found) {
                    managerCollections.clearCollections();
                    for (Route route : updatedRoutes) {
                        managerCollections.addCollections(route);
                    }

                    ResponsePacket response = new ResponsePacket(
                            200,
                            "Элемент с id " + id + " обновлен",
                            null
                    );
                    byte[] data = ManagerSerialize.serialize(response);
                    clientChannel.write(ByteBuffer.wrap(data));
                    return 200;
                } else {
                    ResponsePacket response = new ResponsePacket(
                            400,
                            "Элемент с id = " + id + " не найден",
                            null
                    );
                    byte[] data = ManagerSerialize.serialize(response);
                    clientChannel.write(ByteBuffer.wrap(data));
                    return 400;
                }
            }

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(500, "Ошибка: " + e.getMessage(), null);
                byte[] data = ManagerSerialize.serialize(error);
                clientChannel.write(ByteBuffer.wrap(data));
            } catch (Exception ex) {
                System.out.println("Ошибка создания ResponsePacket");
            }
            return 500;
        }
    }

    @Override
    public String toString() {
        return "update - обновляет элемент по id";
    }
}