package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.example.server.Server.managerCollections;

public class FilterLessThanDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            int distance = Integer.parseInt(args[0]);
            List<Route> result = new ArrayList<>();

            for (Route route : managerCollections.getCollectionsRoute()) {
                if (route.getDistance() < distance) {
                    result.add(route);
                }
            }

            if (result.isEmpty()) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Нет элементов с distance меньше " + distance,
                        result
                );
                byte[] data = ManagerSerialize.serialize(response);
                clientChannel.write(ByteBuffer.wrap(data));
                return 400;
            }

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Найдено элементов: " + result.size(),
                    result
            );
            byte[] data = ManagerSerialize.serialize(response);
            clientChannel.write(ByteBuffer.wrap(data));

            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка: " + e.getMessage(),
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