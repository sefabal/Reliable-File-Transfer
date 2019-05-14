package TCPService;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import UDPModel.SendingFile;

import java.io.*;
import java.net.Socket;

public class TCPFileSender extends Service<Void> {

    private Socket clientSocket;
    private BufferedOutputStream out;
    private BufferedInputStream in;
    private int sleepTimeMS;
    private int sleepTimeNS;


    private SendingFile sendingFile;

    public TCPFileSender(String ip, int port, String filePath,int sleepTimeMS,int sleepTimeNS) throws IOException {
        this.clientSocket = new Socket(ip, port);
        this.sendingFile = new SendingFile(filePath);
        sendingFile.getFileInfo();
        this.sleepTimeMS = sleepTimeMS;
        this.sleepTimeNS = sleepTimeNS;
    }

    public void startConnection() throws IOException {
        out = new BufferedOutputStream(clientSocket.getOutputStream(), 1028);
        in = new BufferedInputStream(clientSocket.getInputStream());

    }

    public void sendPacket(byte[] packet, int offset, int length) throws IOException {
        out.write(packet, offset, length);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    startConnection();

                    int totalPacketNo = sendingFile.getSendingBytes().length;
                    for (int i = 0; i < totalPacketNo; i++) {
                        sendPacket(sendingFile.getSendingBytes()[i], 0, 1028);

                        Thread.sleep(sleepTimeMS,sleepTimeNS);

                        updateProgress(i, totalPacketNo);
                        System.out.println(i + " has sent.");
                        byte[] responseBytes = new byte[1024];
//                if (in.read(responseBytes) > 0) {
//                    String response = Arrays.toString(responseBytes);
//                    System.out.println(response);
                    }
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
                return null;
            }
        };
    }


}
