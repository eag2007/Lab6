package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class Clear implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            managerCollections.clearCollections();

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Коллекция очищена",
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
                ServerLogger.error("Ошибка создания ResponsePacket clear");
            }
            return 500;
        }
    }
}