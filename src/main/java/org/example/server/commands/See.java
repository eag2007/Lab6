package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class See implements Command {

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        try {
            List<Route> allRoutes = managerCollections.getSortedCollections();
            int total = allRoutes.size();

            if (total == 0) {
                ResponsePacket response = new ResponsePacket(200, "Коллекция пуста", null);
                writeModule.writeResponseForClient(clientChannel, response);
                return 200;
            }

            int page = 1;
            if (args.length >= 1) {
                try {
                    page = Integer.parseInt(args[0]);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    ResponsePacket error = new ResponsePacket(400, "Неверный номер страницы", null);
                    writeModule.writeResponseForClient(clientChannel, error);
                    return 400;
                }
            }

            int pageSize = DEFAULT_PAGE_SIZE;
            if (args.length >= 2) {
                try {
                    pageSize = Integer.parseInt(args[1]);
                    if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                } catch (NumberFormatException e) {
                    ResponsePacket error = new ResponsePacket(400, "Неверный размер страницы", null);
                    writeModule.writeResponseForClient(clientChannel, error);
                    return 400;
                }
            }

            int totalPages = (int) Math.ceil((double) total / pageSize);
            if (page > totalPages) page = totalPages;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            List<Route> pageRoutes = new ArrayList<>();
            for (int i = start; i < end; i++) {
                pageRoutes.add(allRoutes.get(i));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("routes", pageRoutes);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", totalPages);
            data.put("total", total);

            String message = String.format("Страница %d из %d (элементов на странице: %d)",
                    page, totalPages, pageSize);

            ResponsePacket response = new ResponsePacket(200, message, data);
            writeModule.writeResponseForClient(clientChannel, response);

            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка в see: {}", e.getMessage());
            try {
                ResponsePacket error = new ResponsePacket(500, "Ошибка: " + e.getMessage(), null);
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Не удалось отправить ошибку: {}", ex.getMessage());
            }
            return 500;
        }
    }

    @Override
    public String toString() {
        return "see [page] [pageSize] - постраничный вывод коллекции";
    }
}