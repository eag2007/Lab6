package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class Update implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        try {
            if (checkArgs(args)) {
                CommandPacket commandPacket = new CommandPacket("update", args, null);

                writeModule.writePacketForServer(serverChannel, commandPacket);

                ResponsePacket response = readModule.readResponseForClient(serverChannel);

                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        managerInputOutput.writeLineIO("Элемент найден, введите значения полей\n", Colors.GREEN);

                        RouteClient route = managerValidation.validateFromInput();

                        CommandPacket updatePacket = new CommandPacket("update", args, route);

                        writeModule.writePacketForServer(serverChannel, updatePacket);

                        response = readModule.readResponseForClient(serverChannel);

                        if (response != null) {
                            if (response.getStatusCode() == 200) {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                            } else if (response.getStatusCode() == 400) {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
                            } else {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                            }
                        } else {
                            managerInputOutput.writeLineIO("Сервер не ответил\n", Colors.YELLOW);
                        }
                    } else {
                        managerInputOutput.writeLineIO("Элемент не найден\n", Colors.YELLOW);
                    }
                } else {
                    managerInputOutput.writeLineIO("Сервер не ответил\n", Colors.YELLOW);
                }

            } else {
                managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
            }
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка сериализации или десериализации\n", Colors.RED);
        } catch (ClassNotFoundException e) {
            managerInputOutput.writeLineIO("Ошибка в преобразовании в ResponsePacket\n", Colors.RED);
        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length == 1) {
            try {
                Long.parseLong(args[0]);
                return true;
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Аргумент должен быть числом\n", Colors.RED);
                return false;
            }
        }
        return false;
    }
}
