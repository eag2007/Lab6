package org.example.client.modules;

import org.example.client.managers.ManagerDeserialize;
import org.example.packet.ResponsePacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.GZIPInputStream;

public class ReadModule {
    private static final int BUFFER_SIZE = 8192;

    public ResponsePacket readResponseForClient(SocketChannel serverChannel) throws IOException, ClassNotFoundException {
        /**
         * Считываем размер сообщения размер не больше чем long
         **/
        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
        while (sizeBuffer.hasRemaining()) {
            int r = serverChannel.read(sizeBuffer);
            if (r == -1) return null;
        }

        sizeBuffer.flip();
        long compressedSize = sizeBuffer.getLong();
        System.out.println(compressedSize);

        if (compressedSize > 50_000_000) {
            throw new IOException("Слишком большой ответ: " + compressedSize + " байт");
        }

        /**
         * Считыванием сжатое сообщение частями
         */

        System.out.println(1);
        ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();
        System.out.println(2);
        ByteBuffer dataBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        long remaining = compressedSize;
        System.out.println("Загружаю данные");
        while (remaining > 0) {
            dataBuffer.clear();
            int bytesToRead = (int) Math.min(BUFFER_SIZE, remaining);
            dataBuffer.limit(bytesToRead);

            int bytesRead = serverChannel.read(dataBuffer);
            if (bytesRead == -1) {
                throw new IOException("Соединение прервано");
            }

            dataBuffer.flip();
            byte[] chunk = new byte[bytesRead];
            dataBuffer.get(chunk);
            compressedBaos.write(chunk);

            remaining -= bytesRead;
            System.out.println(remaining);
        }

        /**
         * Разжимаем данные
         */

        byte[] compressedData = compressedBaos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream decompressedBaos = new ByteArrayOutputStream();

        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                decompressedBaos.write(buffer, 0, len);
            }
        }

        byte[] decompressedData = decompressedBaos.toByteArray();

        // Если надо
        // System.out.println("Получено сжатых: " + compressedData.length +
        //        " байт, разжато: " + decompressedData.length);

        return ManagerDeserialize.deserialize(decompressedData);
    }
}