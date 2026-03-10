package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.packet.collection.Route;
import org.example.server.managers.ManagerSerialize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;

public class RemoveFirst implements Command {
    public int executeCommand(String[] args, RouteClient values, SocketChannel clientChannel) {
        try {
            Route route = managerCollections.getCollectionsRoute().poll();

            ResponsePacket response;
            if (route == null) {
                response = new ResponsePacket(400, "Коллекция пуста", null);
            } else {
                response = new ResponsePacket(200,
                        "Объект удалён с id = " + route.getId(), route.getId());
            }

            byte[] serialize_data = ManagerSerialize.serialize(response);
            clientChannel.write(ByteBuffer.wrap(serialize_data));
            return route == null ? 400 : 200;
        } catch (IOException e) {
            System.out.println();
        }
        return 500;
    }
}