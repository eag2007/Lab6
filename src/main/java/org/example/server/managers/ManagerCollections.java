package org.example.server.managers;

import org.example.packet.collection.Route;
import org.example.server.enums.Colors;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static org.example.server.Server.managerInputOutput;


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

    public void removeByIdCollections(Long id) {
        boolean flag = this.collectionsRoute.removeIf(route -> ((Long) route.getId()).equals(id));

        if (flag) {
            managerInputOutput.writeLineIO("Объект удалён\n", Colors.GREEN);
        } else {
            managerInputOutput.writeLineIO("Объект не найден\n", Colors.RED);
        }
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
}