package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.AsyncTaskManager;
import org.example.server.managers.AsyncTaskManager.TaskInfo;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.server.Server.writeModule;

public class TaskStatus implements Command {

    @Override
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel) {
        if (args.length < 1) {
            try {
                ResponsePacket error = new ResponsePacket(400, "Использование: task_status {taskId}", null);
                writeModule.writeResponseForClient(clientChannel, error);
            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            }
            return 400;
        }

        String taskId = args[0];

        if (!AsyncTaskManager.taskExists(taskId)) {
            try {
                ResponsePacket notFound = new ResponsePacket(404, "Задача с id '" + taskId + "' не найдена", null);
                writeModule.writeResponseForClient(clientChannel, notFound);
            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            }
            return 404;
        }

        TaskInfo task = AsyncTaskManager.getTask(taskId);

        Map<String, String> data = new HashMap<>();
        data.put("taskId",      task.getTaskId());
        data.put("command",     task.getCommandName());
        data.put("status",      task.getStatus().name());
        data.put("message",     task.getMessage());
        data.put("created",   String.valueOf(task.getCreated()));
        data.put("finished",  String.valueOf(task.getFinished()));

        try {
            ResponsePacket response = new ResponsePacket(200, "Статус задачи", data);
            writeModule.writeResponseForClient(clientChannel, response);
        } catch (Exception e) {
            ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            return 500;
        }

        return 200;
    }

    @Override
    public String toString() {
        return "task_status {taskId} - показывает статус асинхронной задачи по её id";
    }
}