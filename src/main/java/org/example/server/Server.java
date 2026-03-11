package org.example.server;

import org.example.packet.CommandPacket;
import org.example.server.managers.ManagerCollections;
import org.example.server.managers.ManagerParserServer;
import org.example.server.managers.ManagerReadWrite;
import org.example.server.modules.ReadModule;
import org.example.server.modules.WriteModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static ManagerCollections managerCollections = new ManagerCollections();
    public static ManagerParserServer managerParserServer = new ManagerParserServer();
    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();

    public static String pathToCollection = System.getenv("PATHTOCOLLECTION");


    public static void main(String[] args) {
        try {
            managerCollections.addAllCollection(ManagerReadWrite.readCSV(pathToCollection));

            ServerSocketChannel server = ServerSocketChannel.open();

            Selector selector = Selector.open();

            server.bind(new InetSocketAddress(8080));
            server.configureBlocking(false);

            System.out.println("Сервер запущен на порту 8080");

            server.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int countChannels = selector.select();
                if (countChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = server.accept();
                        if (client == null) {
                            continue;
                        }
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("Клиент подключился " + client.getRemoteAddress());
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
}