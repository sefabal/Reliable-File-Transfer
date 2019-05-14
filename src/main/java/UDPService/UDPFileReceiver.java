package UDPService;

import TCPService.TCPFileReceiver;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import UDPModel.ReceivingFile;
import UDPModel.SendingFile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPFileReceiver extends Service<Void> {

    //Throughout to send packets,a byte array to wrap our messages
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1028];

    private InetAddress address;
    private int portNumber;

    private ReceivingFile receivingFile;

    private double expectedPacketNumber;

    public boolean isTransactionStarted;

    public UDPFileReceiver() throws SocketException {
        portNumber = 4445;
        socket = new DatagramSocket(portNumber);
        this.expectedPacketNumber = 1;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                running = true;

                while (running) {
                    DatagramPacket packet
                            = new DatagramPacket(buf, buf.length);

                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] arrivedData = packet.getData();

                    //Take first 4 bytes for package number from received packet
                    byte[] packageNumberBytes = new byte[4];
                    System.arraycopy(arrivedData, 0, packageNumberBytes, 0, 4);
                    int packageNumber = ReceivingFile.bytesToInt(packageNumberBytes);
                    System.out.println("Package number received : " + packageNumber);

                    if (expectedPacketNumber == packageNumber) {
                        //First packet is for handshaking between client and server
                        //It contains file information and packet size
                        if (packageNumber == 1) {
                            address = packet.getAddress();
                            portNumber = packet.getPort();

                            //If server does not accept file.
                            if (!isFileAccepted(arrivedData))
                                break;

                        } else {
                            writeValue(packageNumber, arrivedData);
                            updateProgress(packageNumber, receivingFile.getTotalPacketNumber());
                        }
                        if (packageNumber == receivingFile.getTotalPacketNumber()) {
                            receivingFile.dissambleAllPackets();
                            receivingFile.createReceivedFile();
                            System.out.println("File transfer completed");
                            break;
                        }

                        byte[] receivedVal = (packageNumber + " has arrived").getBytes();

                        sendResponse(receivedVal);

                        //increase the expected packet number for receiving next packet
                        expectedPacketNumber++;

                    }// If expected packet number not received from client,send a request for packet number
                    else {

                        byte[] notReceivedVal = (packageNumber + " has not arrived.").getBytes();
                        sendResponse(notReceivedVal);

                    }
                }
                socket.close();
                return null;

            }

        };

    }

    private void sendResponse(byte[] receivedVal) {
        byte[] sendingBytes = addLengthToMessageArray(receivedVal);

        DatagramPacket receivedInfoPacket = new DatagramPacket(sendingBytes, sendingBytes.length, address, portNumber);

        try {
            socket.send(receivedInfoPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] addLengthToMessageArray(byte[] receivedVal) {
        byte[] sendingBytes = new byte[receivedVal.length + 4];
        byte[] sendingBytesLength = SendingFile.intToBytes(receivedVal.length);
        System.arraycopy(sendingBytesLength, 0, sendingBytes, 0, 4);
        System.arraycopy(receivedVal, 0, sendingBytes, 4, receivedVal.length);
        return sendingBytes;
    }


    public boolean isFileAccepted(byte[] arrivedData) {
        this.receivingFile = new ReceivingFile(arrivedData);

        String parsedString = receivingFile.parseFistPacket();

        System.out.println(parsedString);
        System.out.println("Do you accept file transfer. YES or NO");
        Scanner scanner = new Scanner(System.in);
        final String acceptInfo = scanner.nextLine();

        if (acceptInfo.equals("YES")) {
            try {
                byte[] startingMessageBytes = "File transfer isstarting".getBytes();
                byte[] messageWithLength = addLengthToMessageArray(startingMessageBytes);
                socket.send(new DatagramPacket(messageWithLength, messageWithLength.length, address, portNumber));
                isTransactionStarted = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void writeValue(int packageNumber, byte[] arrivedData) {
        // receivingBytes.length+1 = packageNumber --> last package

        TCPFileReceiver.writeValue(packageNumber, arrivedData, receivingFile);


    }
}
