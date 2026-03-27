package org.example.server;

import org.example.packet.CommandPacket;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerCollections;
import org.example.server.managers.ManagerParserServer;
import org.example.server.managers.ManagerReadWrite;
import org.example.server.modules.ConnectModule;
import org.example.server.modules.ReadModule;
import org.example.server.modules.WriteModule;

import java.io.IOException;
import java.net.BindException;
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

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        try {
            ServerLogger.info("Коды ошибок\n200 - все хорошо\n400 - не критическая ошибка\n500 - крититческая ошибка");

            ServerLogger.info("Запуск сервера");
            ServerLogger.info("Загрузка в managerCollections из файла: {}", pathToCollection);

            managerCollections.addAllCollection(ManagerReadWrite.readCSV(pathToCollection));
            ServerLogger.info("Загружено элементов {}", managerCollections.getSizeCollections());

            int port = parsePortFromArgs(args);
            connectModule.startServer(port);
            ServerLogger.info("Сервер запущен на порту {}", port);

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
                            String remoteAddress = "unknown";
                            try {
                                remoteAddress = client.getRemoteAddress().toString();
                            } catch (IOException ignored) {
                            }

                            key.cancel();
                            client.close();
                            ServerLogger.info("Клиент отключился {}", remoteAddress);
                        } else {
                            int code = managerParserServer.parserCommand(packet, client);
                            ServerLogger.info("Код выполнения команды {} от {}", code, client.getRemoteAddress());
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = "Неизвестная ошибка ввода-вывода";
            }
            ServerLogger.error("Ошибка на сервере: {}", errorMsg);
        } finally {
            try {
                connectModule.stopServer();
            } catch (IOException e) {
                ServerLogger.error("Сервер не хочется отключаться произошла ошибка: {}", e.getMessage());
                e.printStackTrace();
            }
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
                        ServerLogger.info("Экстренное сохранение");
                        try {
                            boolean flag = ManagerReadWrite.writeCSV(pathToCollection, managerCollections.getCollectionsRoute());
                            ServerLogger.info(
                                    flag ? "Коллекция сохранена в файле {}" : " Коллекция не сохранена в файле {}", pathToCollection);
                            System.out.println(flag ? "Коллекция сохранена" : " Коллекция не сохранена");
                        } catch (Exception e) {
                            ServerLogger.error("Ошибка при сохранении {}", e.getMessage());
                        }
                    } else if (input.equalsIgnoreCase("exit")) {
                        ServerLogger.info("Отключение сервера");
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
                            ServerLogger.info("Завершение работы сервера, Экстренное сохранение");
                            try {
                                boolean flag = ManagerReadWrite.writeCSV(pathToCollection, managerCollections.getCollectionsRoute());
                                ServerLogger.info(
                                        flag ? "Коллекция сохранена в файле {}" : " Коллекция не сохранена в файле {}",
                                        pathToCollection);
                            } catch (Exception e) {
                                ServerLogger.error("Ошибка при сохранении {}", e.getMessage());
                            }
                        }
                )
        );
    }

    public static int parsePortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    ServerLogger.error("Порт {} вне допустимого диапазона (1-65535). Используется порт по умолчанию: {}",
                            port, DEFAULT_PORT);
                    return DEFAULT_PORT;
                }
                return port;
            } catch (NumberFormatException e) {
                ServerLogger.error("Некорректный формат порта: {}. Используется порт по умолчанию: {}",
                        args[0], DEFAULT_PORT);
                return DEFAULT_PORT;
            }
        }
        return DEFAULT_PORT;
    }
}