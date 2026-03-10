package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.client.managers.ManagerDeserialize;
import org.example.packet.collection.Route;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.example.client.Client.*;

public class Add implements Command {
    public void executeCommand(String[] args) {
        if (checkArgs(args)) {
            if (!managerInputOutput.isScriptMode()) {
                // запросили данные
                RouteClient route = managerValidation.validateFromInput();

                // сделали пакет с командой
                CommandPacket commandPacket = new CommandPacket("add", null, route);

                try {
                    // отправляем команду
                    byte[] serialize_data = ManagerSerialize.serialize(commandPacket);
                    channel.write(ByteBuffer.wrap(serialize_data));

                    // очистим буфер куда будем грузить ответ
                    buffer.clear();

                    int sizeBytes = channel.read(buffer);

                    // если данные есть
                    if (sizeBytes > 0) {
                        buffer.flip();
                        byte[] responseByte = new byte[buffer.remaining()];
                        buffer.get(responseByte);

                        ResponsePacket response = ManagerDeserialize.deserialize(responseByte);

                        if (response.getStatusCode() == 200) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + " ID:"
                                    + response.getData() + "\n", Colors.GREEN);
                        }

                        if (response.getStatusCode() == 500) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                        }
                    }
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Ошибка формата\n", Colors.RED);
                } catch (ClassNotFoundException e) {
                    managerInputOutput.writeLineIO("Ошибка десириализации\n", Colors.RED);
                } catch (Exception e) {
                    managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
                }
            } else {
                RouteClient route = managerValidation.validateFromScript();

                if (route == null) {
                    managerInputOutput.writeLineIO("Объект не создан\n", Colors.RED);
                }

                CommandPacket commandPacket = new CommandPacket("add", null, route);

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
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + " ID:"
                                    + response.getData() + "\n", Colors.GREEN);
                        }

                        if (response.getStatusCode() == 500) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                        }
                    }
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Ошибка формата\n", Colors.RED);
                } catch (ClassNotFoundException e) {
                    managerInputOutput.writeLineIO("Ошибка десириализации\n", Colors.RED);
                } catch (Exception e) {
                    managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
                }
            }
        } else {
            managerInputOutput.writeLineIO("Неправильное количество элементов\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0 || (args.length == 1 && args[0].equals("Route"));
    }

    @Override
    public String toString() {
        return "add - добавляет новый элемент в коллекцию";
    }
}