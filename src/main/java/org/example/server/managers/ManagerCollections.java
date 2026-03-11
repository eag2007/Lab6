package org.example.server.managers;

import org.example.packet.collection.Coordinates;
import org.example.packet.collection.Location;
import org.example.packet.collection.Route;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ManagerCollections {
    private PriorityQueue<Route> collectionsRoute;
    private ZonedDateTime timeInit;

    public ManagerCollections() {
        this.collectionsRoute = new PriorityQueue<>();
        this.timeInit = ZonedDateTime.now();
    }

    public void addCollections(Route element) {
        this.collectionsRoute.add(element);
    }

    public void clearCollections() {
        this.collectionsRoute.clear();
    }

    public void removeAllByDistanceCollections(PriorityQueue<Route> routes) {
        this.collectionsRoute = routes;
    }

    public List<Route> getSortedCollections() {
        List<Route> sorted = new ArrayList<>(collectionsRoute);
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }

    public PriorityQueue<Route> getCollectionsRoute() {
        return this.collectionsRoute;
    }

    public int getSizeCollections() {
        return this.collectionsRoute.size();
    }

    public ZonedDateTime getTimeInit() {
        return this.timeInit;
    }

    public void addAllCollection(List<String[]> collectionImportCSV) {
        long maxId = 0;
        for (String[] row : collectionImportCSV) {
            try {
                if (row[0].equals("id")) continue;

                long id = Long.parseLong(row[0]);
                String name = row[1];

                Coordinates coordinates = new Coordinates(
                        Long.parseLong(row[2]),
                        Long.parseLong(row[3])
                );

                ZonedDateTime creationDate = ZonedDateTime.parse(row[4]);

                Location from = new Location(
                        Float.parseFloat(row[5].replace(',', '.')),
                        Double.parseDouble(row[6].replace(',', '.')),
                        Integer.parseInt(row[7])
                );

                Location to = new Location(
                        Float.parseFloat(row[8].replace(',', '.')),
                        Double.parseDouble(row[9].replace(',', '.')),
                        Integer.parseInt(row[10])
                );

                Integer distance = Integer.parseInt(row[11]);

                BigDecimal price = new BigDecimal(row[12].trim().replace(',', '.'));

                Route route = new Route(id, name, coordinates, creationDate, from, to, distance, price);
                collectionsRoute.add(route);

                if (id > maxId) {
                    maxId = id;
                }

                ManagerGenerateId.setId(maxId);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: неправильный формат данных при загрузке\n");
                System.out.println("Данные не загружены в коллекцию");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage() + "\n");
            }
        }
    }
}