package TCPService;

import UDPModel.ReceivingFile;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TCPFileReceiver extends Service<Void> {

    private ServerSocket serverSocket;

    private Socket clientSocket;

    private BufferedOutputStream out;

    private BufferedInputStream in;

    private ReceivingFile receivingFile;

    public TCPFileReceiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void startConnection() {
        try {
            clientSocket = serverSocket.accept();
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            in = new BufferedInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Task<Void> createTask() {
        this.startConnection();
        return new Task<>() {
            @Override
            protected Void call() {
                byte[] b = new byte[1028];
                int offset = 0;
                int length = 1028;

                byte[] packetNumber = new byte[4];

                try {
                    while (true) {
                        if (in.read(b, offset, length) > 0) {
                            System.arraycopy(b, 0, packetNumber, 0, 4);
                            int packetNo = ReceivingFile.bytesToInt(packetNumber);
                            System.out.println(packetNo + " has received.");
                            if (packetNo == 1) {
                                if (!isFileAccepted(b)) {
                                    break;
                                }
                            } else {
                                writeValue(packetNo, b);
                                updateProgress(packetNo, receivingFile.getTotalPacketNumber());
                                if (packetNo == receivingFile.getTotalPacketNumber()) {
                                    receivingFile.dissambleAllPackets();
                                    receivingFile.createReceivedFile();
                                    System.out.println("File transfer completed");
                                    break;
                                }
//                                out.write((packetNo + " has received.\n").getBytes());
                            }
                        }
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                return null;
            }

        };


    }

    private boolean isFileAccepted(byte[] arrivedData) throws IOException {
        this.receivingFile = new ReceivingFile(arrivedData);

        String parsedString = receivingFile.parseFistPacket();

        System.out.println(parsedString);
//        System.out.println("Do you accept file transfer. YES or NO");
//        Scanner scanner = new Scanner(System.in);
//        final String acceptInfo = scanner.nextLine();
//
//        if (acceptInfo.equals("YES")) {
//            String startingMessage = "File transfer is starting.\n";
////            out.write(startingMessage.getBytes());
//            System.out.println(startingMessage);

        return true;

    }

    private void writeValue(int packageNumber, byte[] arrivedData) {
        writeValue(packageNumber, arrivedData, receivingFile);

    }

    public static void writeValue(int packageNumber, byte[] arrivedData, ReceivingFile receivingFile) {
        if (packageNumber != receivingFile.receivingBytes.length+1)
            System.arraycopy(arrivedData, 4, receivingFile.receivingBytes[packageNumber - 2], 0, 1024);
        else
            System.arraycopy(arrivedData, 4, receivingFile.receivingBytes[packageNumber - 2], 0, receivingFile.getLastPacketSize());
    }
}
