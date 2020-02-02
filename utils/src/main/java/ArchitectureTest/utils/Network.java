package ArchitectureTest.utils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import architectureTest.protobuf.RequestOuterClass.Request;
import com.google.protobuf.Message;

public class Network {
    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));
    }

    public static void sendMessage(Message msg, OutputStream output) throws IOException {
        byte[] sizeBytes = intToByteArray(msg.getSerializedSize());
        output.write(sizeBytes);
        msg.writeTo(output);
        output.flush();
    }

    public static Request packArray(List<Long> elems) {
        Request.Builder responseBuilder = Request.newBuilder();
        responseBuilder.setCode(2);
        responseBuilder.addAllElems(elems);
        return responseBuilder.build();
    }

    public static void sendArray(Socket socket, List<Long> elems) throws IOException {
        OutputStream output = socket.getOutputStream();
        Request response = packArray(elems);
        sendMessage(response, output);
    }

    private static byte[] readToByteArray(InputStream input, int size) throws IOException {
        byte[] result = new byte[size];
        int offset = 0;
        while (offset < size) {
            int readBytes = input.read(result, offset, size - offset);
            offset += readBytes;
        }
        return result;
    }

    public static Request parseRequest(Socket socket) throws IOException {
        Request request;
        InputStream input = socket.getInputStream();
        PushbackInputStream pushBackInput = new PushbackInputStream(input);
        int bytes = pushBackInput.read();
        if (bytes == -1) {
            return null;
        }
        pushBackInput.unread(bytes);
        byte[] messageSizeBytes = readToByteArray(pushBackInput, 4);
        int messageSize = byteArrayToInt(messageSizeBytes);

        byte[] message = readToByteArray(pushBackInput, messageSize);
        request = Request.parseFrom(message);
        return request;
    }

    public static void sendFinishSign(Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setCode(0);
        requestBuilder.addAllElems(new ArrayList<>());
        Request request = requestBuilder.build();
        sendMessage(request, output);
    }
}
