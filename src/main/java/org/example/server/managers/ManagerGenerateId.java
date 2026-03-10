package org.example.server.managers;

public class ManagerGenerateId {
    private static long id = 0;

    public static synchronized long generateId() {
        return ++id;
    }

    public static synchronized void setId(long idGet) {
        id = idGet++;
    }
}