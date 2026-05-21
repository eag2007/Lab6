package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.Coordinates;
import org.example.packet.collection.Location;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.AsyncTaskManager;
import org.example.server.managers.AsyncTaskManager.TaskInfo;

import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class GenerateData implements Command {

    private static final String[] NAMES = {
            "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta",
            "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi",
            "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };

    @Override
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        if (args.length < 1) {
            try {
                ResponsePacket error = new ResponsePacket(
                        400,
                        "Использование: generate_data {count}",
                        null
                );

                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа generate_data: {}", e.getMessage());
            }
            return 400;
        }

        int count;
        try {
            count = Integer.parseInt(args[0]);
            if (count <= 0) throw new NumberFormatException("Число должно быть положительным");
        } catch (NumberFormatException e) {
            try {
                ResponsePacket error = new ResponsePacket(
                        400,
                        "Аргумент count должен быть положительным целым числом",
                        null
                );

                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception ex) {
                ServerLogger.error("Ошибка отправки ответа generate_data: {}", ex.getMessage());
            }
            return 400;
        }

        String taskId = AsyncTaskManager.createTask("generate_data");
        TaskInfo task = AsyncTaskManager.getTask(taskId);

        try {
            Map<String, String> responseData = new HashMap<>();
            responseData.put("taskId", taskId);
            responseData.put("count", String.valueOf(count));

            ResponsePacket accepted = new ResponsePacket(
                    202,
                    "Задача принята. Используйте 'task_status " + taskId + "' для проверки статуса.",
                    responseData
            );
            writeModule.writeResponseForClient(clientChannel, accepted);
        } catch (Exception e) {
            ServerLogger.error("Ошибка отправки подтверждения generate_data: {}", e.getMessage());
            return 500;
        }

        final int finalCount = count;
        Thread worker = new Thread(() -> {
            task.setStatus(AsyncTaskManager.TaskStatus.IN_PROGRESS);
            task.setMessage("Генерация данных...");
            ServerLogger.info("Задача {}: начата генерация {} элементов", taskId, finalCount);

            try {
                Random rnd = new Random();
                for (int i = 0; i < finalCount; i++) {
                    String name = NAMES[rnd.nextInt(NAMES.length)] + "-" + (i + 1);

                    Coordinates coords = new Coordinates(
                            rnd.nextLong() % 1000,
                            rnd.nextLong() % 1000
                    );
                    Location from = new Location(
                            rnd.nextFloat() * 200 - 100,
                            rnd.nextDouble() * 200 - 100,
                            rnd.nextInt(1000)
                    );
                    Location to = new Location(
                            rnd.nextFloat() * 200 - 100,
                            rnd.nextDouble() * 200 - 100,
                            rnd.nextInt(1000)
                    );
                    int distance = rnd.nextInt(9900) + 100;
                    BigDecimal price = BigDecimal.valueOf(rnd.nextInt(100000) + 1, 2);

                    Route route = new Route(name, coords, from, to, distance, price);
                    managerCollections.addCollections(route);

                    if (finalCount > 100) {
                        Thread.sleep(2);
                    }
                }

                task.finish("Успешно сгенерировано " + finalCount + " элементов. Всего в коллекции: " + managerCollections.getSizeCollections());
                ServerLogger.info("Задача {}: завершена, сгенерировано {} элементов", taskId, finalCount);

            } catch (InterruptedException e) {
                task.error("Задача прервана: " + e.getMessage());
                ServerLogger.error("Задача {}: прервана", taskId);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                task.error("Ошибка при генерации: " + e.getMessage());
                ServerLogger.error("Задача {}: ошибка - {}", taskId, e.getMessage());
            }
        });

        worker.setDaemon(true);
        worker.setName("generate-data-" + taskId);
        worker.start();

        return 202;
    }

    @Override
    public String toString() {
        return "generate_data {count} - асинхронно генерирует count элементов в коллекции";
    }
}