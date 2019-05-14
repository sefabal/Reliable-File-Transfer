package UDPService;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import UDPModel.ReceivingFile;
import UDPModel.SendingFile;

import java.io.*;
import java.net.*;

public class UDPFileSender extends Service<Void> {
    private DatagramSocket socket;
    private InetAddress address;
    private int portNumber;

    private byte[] buf;
    private boolean isFileSending;

    private SendingFile sendingFileToSend;

    private int lastPacketReached;

    public UDPFileSender(SendingFile sendingFile,String ipAddress) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName(ipAddress);
        isFileSending = true;
        this.sendingFileToSend = sendingFile;
        this.portNumber = 4445;
        lastPacketReached = 0;
    }


    //TODO : COMPLETE INT TO BYTE ARRAY AND CONVERTING BYTE TO INT

    private boolean sendFileBytes(byte[] bytes) throws IOException {
        buf = bytes;
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, portNumber);
        socket.send(packet);

        //Get response from server
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        byte[] received = packet.getData();

        byte[] length = new byte[4];
        System.arraycopy(received, 0, length, 0, 4);
        int convertedLength = ReceivingFile.bytesToInt(length);

        //Print response
        String receivedString = parseReceivedBytes(received, convertedLength);

        return checkReceivedMessage(receivedString);
    }

    private boolean checkReceivedMessage(String receivedString) {

        //(packageNumber + " has not arrived.")
        //(packageNumber + " has arrived")

        String[] splittedString = receivedString.split(" ");
        //packet received succesfully update the value
        if (splittedString.length == 3) {
            if (splittedString[0].equals("File"))
                lastPacketReached = 0;
            else
                lastPacketReached = Integer.parseInt(splittedString[0]);
            return true;
        }

        return false;
    }

    private String parseReceivedBytes(byte[] received, int lenght) {
        StringBuilder builder = new StringBuilder();
        for (int i = 4; i < lenght + 4; i++) {
            builder.append((char) (received[i]));
        }
        String inf = builder.toString();
        System.out.println(inf);

        return inf;

    }


    public void close() {
        socket.close();
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (isFileSending) {
                    try {
                        if (!sendingFileToSend.getFileInfo()) {
                            break;
                        }

                        byte[][] sendingBytes = sendingFileToSend.getSendingBytes();

                        //packet numbers start from 1
                        //packet index start from 0
                        int i = 0;
                        while (i < sendingBytes.length ) {
                            //If packet reached succesfully we update the lastPacketReached value
                            //otherwise try to send same packet to destination
                            if (sendFileBytes(sendingBytes[i]))
                                i = ++lastPacketReached;
                            updateProgress(lastPacketReached, sendingBytes.length + 1);
                            if (lastPacketReached == sendingBytes.length) {
                                isFileSending = false;
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return null;
            }
        };
    }
}

/*
TODO: If the packet not received from receiver it will send request for specific packet. Sender should send that packet..
byte[] notReceivedVal = (packageNumber + " has not arrived.").getBytes();
*/
