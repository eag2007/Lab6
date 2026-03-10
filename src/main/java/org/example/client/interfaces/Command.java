package org.example.client.interfaces;

public interface Command {
    String toString();
    void executeCommand(String[] args);
}