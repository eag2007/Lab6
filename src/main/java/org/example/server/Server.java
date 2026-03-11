package org.example.server;

import org.example.packet.CommandPacket;
import org.example.server.managers.ManagerCollections;
import org.example.server.managers.ManagerParserServer;
import org.example.server.managers.ManagerReadWrite;
import org.example.server.modules.ConnectModule;
import org.example.server.modules.ReadModule;
import org.example.server.modules.WriteModule;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Server {
    public static ManagerCollections managerCollections = new ManagerCollections();
    public static ManagerParserServer managerParserServer = new ManagerParserServer();
    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();
    public static ConnectModule connectModule = new ConnectModule();

    public static String pathToCollection = System.getenv("PATHTOCOLLECTION");


    public static void main(String[] args) {
        try {
            managerCollections.addAllCollection(ManagerReadWrite.readCSV(pathToCollection));

            connectModule.startServer(8080);

            /**
             * Обработка экстренного отключения сервера
             * Обработка отключения сервера через ввод команд
             */
            ifCloseServer();
            inputOutputServer();

            while (true) {
                int countChannels = connectModule.getSelector().select();
                if (countChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = connectModule.getSelector().selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        connectModule.acceptConnection();
                    }

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        CommandPacket packet = readModule.readPacketForServer(client);

                        if (packet == null) {
                            key.cancel();
                            client.close();
                        } else {
                            int code = managerParserServer.parserCommand(packet, client);
                            System.out.println("Код выполнения: " + code);
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка на сервере " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Ошибка десериализации " + e.getMessage());
        }
    }

    public static void inputOutputServer() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    System.out.print("\u001B[34mКоманда для сервера$: \u001B[0m\n");

                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("save")) {
                        System.out.println("Экстренное сохранение");
                        try {
                            ManagerReadWrite.writeCSV(pathToCollection, managerCollections.getCollectionsRoute());
                            System.out.println(
                                    ManagerReadWrite.writeCSV(pathToCollection, managerCollections.getCollectionsRoute()
                                    ) ? "Коллекция сохранена" : " Коллекция не сохранена");
                        } catch (Exception e) {
                            System.out.println("Ошибка при сохранении " + e.getMessage());
                        }
                    } else if (input.equalsIgnoreCase("exit")) {
                        System.exit(0);
                    } else if (input.equalsIgnoreCase("help")) {
                        System.out.println("help - справка");
                        System.out.println("exit - завершить работу сервера");
                        System.out.println("save - сохранить коллекцию");
                    }
                }
            } catch (NoSuchElementException e) {
                System.exit(0);
            }
        }).start();
    }

    public static void ifCloseServer() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        () -> {
                            System.out.println("Завершение работы сервера, Экстренное сохранение");
                            try {
                                System.out.println(
                                        ManagerReadWrite.writeCSV(pathToCollection, managerCollections.getCollectionsRoute()
                                        ) ? "Коллекция сохранена" : " Коллекция не сохранена");
                            } catch (Exception e) {
                                System.out.println("Ошибка при сохранении " + e.getMessage());
                            }
                        }
                )
        );
    }
}