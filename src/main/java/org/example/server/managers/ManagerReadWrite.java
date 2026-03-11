package org.example.server.managers;

import org.example.packet.collection.Route;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static org.example.server.Server.managerCollections;


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
            System.out.println("Файл не найден: " + pathToFile + "\n");
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

            System.out.println("Загружено строк: " + data.size() + "\n");

        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage() + "\n");
        }

        return data;
    }

    public boolean writeCSV(String pathToFile) {
        if (pathToFile == null || pathToFile.trim().isEmpty()) {
            System.out.println("Ошибка: путь не указан\n");
            return false;
        }

        File file = new File(pathToFile);

        if (!pathToFile.toLowerCase().endsWith(".csv")) {
            System.out.println("Ошибка: файл должен быть с расширением .csv\n");
            return false;
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("Ошибка: директория " + parentDir + " не существует\n");
            return false;
        }

        PriorityQueue<Route> routes = managerCollections.getCollectionsRoute();

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
            System.out.println("Сохранено " + routes.size() + " маршрутов в " + pathToFile + "\n");
            return true;

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка: невозможно создать файл\n");
            return false;
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage() + "\n");
            return false;
        }
    }
}