package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class AverageOfDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            if (managerCollections.getSizeCollections() == 0) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Коллекция пуста",
                        0.0
                );
                writeModule.writeResponseForClient(clientChannel, response);
                return 400;
            }

            double sum = 0;
            for (Route route : managerCollections.getCollectionsRoute()) {
                sum += route.getDistance();
            }
            double average = sum / managerCollections.getSizeCollections();

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Среднее значение distance",
                    average
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