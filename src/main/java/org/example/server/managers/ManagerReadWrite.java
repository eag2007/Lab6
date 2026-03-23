package org.example.server.managers;

import org.example.packet.collection.Route;
import org.example.server.logger.ServerLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class ManagerReadWrite {
    private static ManagerReadWrite managerReadWrite;

    private ManagerReadWrite() {
    }

    public static ManagerReadWrite getInstance() {
        if (managerReadWrite == null) {
            managerReadWrite = new ManagerReadWrite();
        }
        return managerReadWrite;
    }

    public static List<String[]> readCSV(String pathToFile) {
        if (pathToFile == null) {
            return new ArrayList<>();
        }

        List<String[]> data = new ArrayList<>();
        File file = new File(pathToFile);

        if (!file.exists()) {
            ServerLogger.error("Файл не найден: {}", pathToFile);
            return data;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] fields = line.split(";");
                data.add(fields);
            }

            ServerLogger.info("Загружено строк: {}", data.size());

        } catch (IOException e) {
            ServerLogger.error("Ошибка чтения: {}", e.getMessage());
        }

        return data;
    }

    public static boolean writeCSV(String pathToFile, PriorityQueue<Route> routes) {
        if (pathToFile == null || pathToFile.trim().isEmpty()) {
            ServerLogger.error("Ошибка: путь не указан");
            return false;
        }

        File file = new File(pathToFile);

        if (!pathToFile.toLowerCase().endsWith(".csv")) {
            ServerLogger.error("Ошибка: файл должен быть с расширением .csv");
            return false;
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            ServerLogger.error("Ошибка: директория " + parentDir + " не существует\n");
            return false;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {

            writer.write("id;name;coordinates_x;coordinates_y;creation_date;from_x;from_y;from_z;to_x;to_y;to_z;distance;price\n");

            for (Route route : routes) {
                String line = route.getId() + ";" +
                        route.getName() + ";" +
                        route.getCoordinates().getX() + ";" +
                        route.getCoordinates().getY() + ";" +
                        route.getCreationDate() + ";" +
                        route.getFrom().getX() + ";" +
                        route.getFrom().getY() + ";" +
                        route.getFrom().getZ() + ";" +
                        route.getTo().getX() + ";" +
                        route.getTo().getY() + ";" +
                        route.getTo().getZ() + ";" +
                        route.getDistance() + ";" +
                        route.getPrice() + "\n";
                writer.write(line);
            }

            writer.flush();
            ServerLogger.info("Сохранено {} маршрутов в {}", routes.size(), pathToFile);
            return true;

        } catch (FileNotFoundException e) {
            ServerLogger.error("Ошибка: невозможно создать файл");
            return false;
        } catch (IOException e) {
            ServerLogger.error("Ошибка записи: {}", e.getMessage());
            return false;
        }
    }
}