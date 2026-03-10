package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.managers.ManagerSerialize;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.server.Server.managerCollections;

public class Show implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            // Получаем отсортированную коллекцию
            List<Route> routes = managerCollections.getSortedCollections();

            // Создаем ответ
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

            // Сериализуем и отправляем
            byte[] data = ManagerSerialize.serialize(response);
            clientChannel.write(ByteBuffer.wrap(data));

            return 200;

        } catch (Exception e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        500,
                        "Ошибка при получении коллекции: " + e.getMessage(),
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