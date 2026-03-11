package org.example.server.modules;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ConnectModule {
    private ServerSocketChannel server;
    private Selector selector;

    public void startServer(int port) throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.bind(new InetSocketAddress(port));

        this.selector = Selector.open();

        this.server.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Сервер запущен на порту " + port);
    }

    public Selector getSelector() {
        return this.selector;
    }

    public SocketChannel acceptConnection() throws IOException {
        SocketChannel client = server.accept();
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("Клиент подключился " + client.getRemoteAddress());
        }
        return client;
    }

    public void stopServer() throws IOException {
        if (server.isOpen())
            server.close();

        if (selector.isOpen())
            selector.close();
    }
}