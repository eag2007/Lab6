package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;

import java.io.IOException;

import static org.example.client.Client.channel;
import static org.example.client.Client.managerInputOutput;

public class Exit implements Command {

    public void executeCommand(String[] args) {
        try {
            if (checkArgs(args)) {
                managerInputOutput.closeIO();
                managerInputOutput.writeLineIO("Завершение работы\n", Colors.GREEN);
                channel.close();
                System.exit(0);
            } else {
                managerInputOutput.writeLineIO("Неверное количество аргументов\n", Colors.RED);
            }
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Произошла ошибка при разрыве соединения\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "Команда exit - завершает работу SppoManager";
    }
}