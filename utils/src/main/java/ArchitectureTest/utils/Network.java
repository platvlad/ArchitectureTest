package ArchitectureTest.utils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import architectureTest.protobuf.RequestOuterClass.Request;
import com.google.protobuf.Message;

public class Network {
    public static void sendMessage(Message msg, OutputStream output) throws IOException {
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.writeInt(msg.getSerializedSize());
        objectOutput.flush();
        msg.writeTo(output);
        output.flush();
    }

    public static void sendArray(Socket socket, List<Long> elems) throws IOException {
        OutputStream output = socket.getOutputStream();
        Request.Builder responseBuilder = Request.newBuilder();
        responseBuilder.setCode(2);
        responseBuilder.addAllElems(elems);
        Request response = responseBuilder.build();
        sendMessage(response, output);
    }

    public static Request parseRequest(Socket socket) throws IOException {
        Request request;
        InputStream input = socket.getInputStream();
        ObjectInputStream objectInput = new ObjectInputStream(input);
        int messageSize = objectInput.readInt();
        byte[] message = new byte[messageSize];
        int offset = 0;
        while (offset < messageSize) {
            int readBytes = input.read(message, offset, messageSize - offset);
            offset += readBytes;
        }
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
