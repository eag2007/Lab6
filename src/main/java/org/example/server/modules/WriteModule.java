package org.example.server.modules;

import org.example.packet.ResponsePacket;
import org.example.server.managers.ManagerSerialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.GZIPOutputStream;

public class WriteModule {

    public void writeResponseForClient(SocketChannel client, ResponsePacket response) throws IOException {
        /**
         * Сериализуем
         */
        byte[] data = ManagerSerialize.serialize(response);

        /**
         * Сжимаем данные
         */

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        byte[] compressedData = baos.toByteArray();

        // Если надо
        // System.out.println("Исходный размер: " + data.length + " байт");
        // System.out.println("Сжатый размер: " + compressedData.length + " байт");
        // System.out.println("Сжатие: " + (100 - (compressedData.length * 100 / data.length)) + "%");


        /**
         * Отправляем размер
         */

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
        sizeBuffer.putLong(compressedData.length);
        sizeBuffer.flip();
        client.write(sizeBuffer);

        /**
         * Отправляем сжатые данные
         */

        ByteBuffer buffer = ByteBuffer.wrap(compressedData);
        client.write(buffer);

        System.out.println("Отправлен код: " + response.getStatusCode() + " " + client.getRemoteAddress());
    }
}