package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.client.managers.ManagerDeserialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static org.example.client.Client.*;

public class Info implements Command {
    public void executeCommand(String[] args) {
        if (checkArgs(args)) {
            CommandPacket commandPacket = new CommandPacket("info", args, null);

            try {
                byte[] serialize_data = ManagerSerialize.serialize(commandPacket);
                channel.write(ByteBuffer.wrap(serialize_data));

                buffer.clear();
                int sizeBytes = channel.read(buffer);

                if (sizeBytes > 0) {
                    buffer.flip();
                    byte[] responseByte = new byte[buffer.remaining()];
                    buffer.get(responseByte);

                    ResponsePacket response = ManagerDeserialize.deserialize(responseByte);

                    if (response.getStatusCode() == 200) {
                        Map<String, Object> info = (Map<String, Object>) response.getData();
                        managerInputOutput.writeLineIO("Количество элементов: " + info.get("size") + "\n");
                        managerInputOutput.writeLineIO("Время инициализации: " + info.get("initTime") + "\n");
                        managerInputOutput.writeLineIO("Тип данных: Route\n");
                    } else {
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                    }
                }
            } catch (IOException e) {
                managerInputOutput.writeLineIO("Ошибка отправки\n", Colors.RED);
            } catch (ClassNotFoundException e) {
                managerInputOutput.writeLineIO("Ошибка десериализации\n", Colors.RED);
            } catch (Exception e) {
                managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
            }
        } else {
            managerInputOutput.writeLineIO("Неправильное количество аргументов\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "info - выводит информацию о коллекции";
    }
}