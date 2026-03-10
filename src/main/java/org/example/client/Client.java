package org.example.client;

import org.example.client.commands.Exit;
import org.example.client.enums.Colors;
import org.example.client.managers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class Client {
    public static ManagerValidation managerValidation = new ManagerValidation();
    public static ManagerInputOutput managerInputOutput = ManagerInputOutput.getInstance();
    public static ManagerParserClient managerParserClient = new ManagerParserClient();
    public static ByteBuffer buffer = ByteBuffer.allocate(8192);
    public static SocketChannel channel = null;

    public static void main(String[] args) {
        try {
            managerInputOutput.setCommands(managerParserClient.getCommandNames());

            // Цикл подключения к серверу
            boolean connected = false;
            while (!connected) {
                try {
                    managerInputOutput.writeLineIO("Подключение к серверу...\n", Colors.BLUE);
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress("localhost", 8080));
                    connected = true;
                    managerInputOutput.writeLineIO("Вы подключились к серверу\n", Colors.GREEN);
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Сервер не доступен. Нажмите Enter для повторной попытки...\n", Colors.RED);
                    if (managerInputOutput.readLineIO().trim().replaceAll("\\s+", " ").equalsIgnoreCase("exit")) {
                        new Exit().executeCommand(new String[]{});
                        return;
                    }
                }
            }

            // Основной цикл команд
            while (true) {
                String input = managerInputOutput.readLineIO("Введите команду : ");
                managerParserClient.parserCommand(input);
            }

        } catch (NoSuchElementException e) {
            managerInputOutput.writeLineIO("Завершение работы\n", Colors.GREEN);
            managerInputOutput.closeIO();
        } catch (RuntimeException e) {
            managerInputOutput.writeLineIO("Ошибка во время работы программы\n", Colors.RED);
        }
    }
}