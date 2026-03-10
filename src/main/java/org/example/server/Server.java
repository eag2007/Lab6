package org.example.server;

import org.example.packet.CommandPacket;
import org.example.server.managers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class Server {
    public static ManagerInputOutput managerInputOutput = ManagerInputOutput.getInstance();
    public static ManagerCollections managerCollections = new ManagerCollections();
    public static ManagerParserServer managerParserServer = new ManagerParserServer();

    private static volatile boolean running = true;

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Сервер запущен на порту 8080");
        System.out.println("Команды сервера: save - сохранить, exit - выйти");

        // ЭТО ДОБАВИТЬ - сохранение при выключении
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Автосохранение...");
        }));

        // ЭТО ИЗМЕНИТЬ - добавить running и проверку консоли
        while (running) {
            // Проверка команд с консоли
            try {
                if (System.in.available() > 0) {
                    Scanner scanner = new Scanner(System.in);
                    String cmd = scanner.nextLine();
                    if (cmd.equals("save")) {
                        System.out.println("Сохранение...");
                        System.out.println("Готово");
                    } else if (cmd.equals("exit")) {
                        System.out.println("Выход...");
                        running = false;
                        selector.wakeup();
                    }
                }
            } catch (IOException e) {}

            // ЭТО ИЗМЕНИТЬ - добавить таймаут
            selector.select(100);
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Клиент подключился: " + client.getRemoteAddress());
                }

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(8192);

                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            System.out.println("Клиент отключился: " + client.getRemoteAddress());
                            client.close();
                            continue;
                        }

                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);

                        CommandPacket packet = ManagerDeserialize.deserialize(data);
                        System.out.println("Получена команда: " + packet.getType() + " от " + client.getRemoteAddress());
                        System.out.println("Получены аргументы: " + Arrays.toString(packet.getArgs()));
                        System.out.println("Получены значения: " + packet.getValues());

                        // ЭТО ДОБАВИТЬ - запрет save для клиента
                        if (packet.getType().equals("save")) {
                            client.write(ByteBuffer.wrap("Команда save только для сервера".getBytes(StandardCharsets.UTF_8)));
                            continue;
                        }

                        // Передаем clientChannel в парсер
                        int code = managerParserServer.parserCommand(packet, client);

                        System.out.println("Код выполнения: " + code);

                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                        try {
                            client.write(ByteBuffer.wrap(("Ошибка: " + e.getMessage()).getBytes(StandardCharsets.UTF_8)));
                        } catch (IOException ex) {}
                        client.close();
                    }
                }
            }
        }

        // ЭТО ДОБАВИТЬ - закрытие ресурсов
        serverChannel.close();
        selector.close();
        System.out.println("Сервер остановлен");
    }
}