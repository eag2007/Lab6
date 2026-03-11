package org.example.client;

import org.example.client.commands.Exit;
import org.example.client.enums.Colors;
import org.example.client.managers.*;
import org.example.client.modules.ReadModule;
import org.example.client.modules.WriteModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;

public class Client {
    public static ManagerValidation managerValidation = new ManagerValidation();
    public static ManagerInputOutput managerInputOutput = ManagerInputOutput.getInstance();
    public static ManagerParserClient managerParserClient = new ManagerParserClient();
    public static ByteBuffer buffer = ByteBuffer.allocate(8192);
    public static SocketChannel server = null;
    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();

    public static void main(String[] args) {
        try {
            managerInputOutput.setCommands(managerParserClient.getCommandNames());

            boolean connected = false;
            while (!connected) {
                try {
                    managerInputOutput.writeLineIO("Подключение к серверу...\n", Colors.BLUE);
                    server = SocketChannel.open();
                    server.configureBlocking(true);
                    server.connect(new InetSocketAddress("localhost", 8080));
                    connected = true;
                    managerInputOutput.writeLineIO("Вы подключились к серверу\n", Colors.GREEN);
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Сервер не доступен. Нажмите Enter для повторной попытки...\n", Colors.YELLOW);
                    if (managerInputOutput.readLineIO().trim().replaceAll("\\s+", " ").equalsIgnoreCase("exit")) {
                        new Exit().executeCommand(new String[]{}, server);
                        return;
                    }
                }
            }

            while (true) {

                try {
                    server.socket().sendUrgentData(0);
                } catch (IOException e) {
                    managerInputOutput.writeLineIO(
                            "Пропало соединение с сервером\nПопробуйте перезайти\nДоступен только локальный режим\n",
                            Colors.YELLOW);
                }

                String input = managerInputOutput.readLineIO("\u001B[34mВведите команду : \u001B[0m");
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