package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.client.managers.ManagerDeserialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.example.client.Client.*;

public class RemoveById implements Command {
    public void executeCommand(String[] args) {
        if (checkArgs(args)) {
            CommandPacket commandPacket = new CommandPacket("remove_by_id", args, null);

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
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                    } else if (response.getStatusCode() == 400) {
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
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
            managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length == 1) {
            try {
                Long.parseLong(args[0]);
                return true;
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: аргумент должен быть целым числом\n", Colors.RED);
                return false;
            }
        }
        managerInputOutput.writeLineIO("Неправильное количество аргументов\n", Colors.RED);
        return false;
    }

    @Override
    public String toString() {
        return "remove_by_id id - удаляет элемент по id";
    }
}