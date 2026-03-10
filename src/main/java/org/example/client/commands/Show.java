package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.client.managers.ManagerDeserialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

import static org.example.client.Client.*;

public class Show implements Command {

    public void executeCommand(String[] args) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Неверное количество аргументов\n", Colors.RED);
        }

        try {
            // Отправляем команду show
            CommandPacket commandPacket = new CommandPacket("show", args, null);

            byte[] serializeData = ManagerSerialize.serialize(commandPacket);
            channel.write(ByteBuffer.wrap(serializeData));

            // Читаем ответ
            buffer.clear();
            int bytesRead = channel.read(buffer);

            if (bytesRead > 0) {
                buffer.flip();
                byte[] responseData = new byte[buffer.remaining()];
                buffer.get(responseData);

                // Десериализуем ответ
                ResponsePacket response = ManagerDeserialize.deserialize(responseData);

                if (response.getStatusCode() == 200) {
                    List<Route> routes = (List<Route>) response.getData();

                    if (routes.isEmpty()) {
                        managerInputOutput.writeLineIO("Коллекция пуста\n", Colors.YELLOW);
                        return;
                    }

                    // Заголовок таблицы
                    String header = String.format("%-3s | %-15s | %-3s | %-3s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-5s | %-10s | %-10s",
                            "ID", "Name", "X", "Y", "Date", "FromX", "FromY", "FromZ", "ToX", "ToY", "ToZ", "Distance", "Price");

                    managerInputOutput.writeLineIO(header + "\n");
                    managerInputOutput.writeLineIO("-".repeat(header.length()) + "\n");

                    // Выводим каждый маршрут
                    for (Route route : routes) {
                        String line = String.format("%-3s | %-15s | %-3s | %-3s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-5s | %-10s | %-10s",
                                route.getId(),
                                route.getName(),
                                route.getCoordinates().getX(),
                                route.getCoordinates().getY(),
                                route.getCreationDate(),
                                route.getFrom().getX(),
                                route.getFrom().getY(),
                                route.getFrom().getZ(),
                                route.getTo().getX(),
                                route.getTo().getY(),
                                route.getTo().getZ(),
                                route.getDistance(),
                                route.getPrice());

                        managerInputOutput.writeLineIO(line + "\n");
                    }
                } else {
                    managerInputOutput.writeLineIO("Ошибка: " + response.getMessage() + "\n", Colors.RED);
                }
            }
        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "show - выводит в стандартный поток вывода все элементы коллекции в строковом представлении";
    }
}