package org.example.packet;

import org.example.packet.collection.RouteClient;

import java.io.Serializable;

public class CommandPacket implements Serializable {
    private final String type;
    private final String[] args;
    private final RouteClient values;

    public CommandPacket(String type, String[] args, RouteClient values) {
        this.type = type;
        this.args = args;
        this.values = values;
    }

    public String getType() { return this.type; }
    public String[] getArgs() { return this.args; }
    public RouteClient getValues() { return this.values; }
}