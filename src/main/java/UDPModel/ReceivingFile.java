package UDPModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceivingFile {

    private byte[] dissambledPacket;
    public byte[][] receivingBytes;

    private byte[] infoBytes;
    private String fileName;


    private int fileSize;
    private int totalPacketNumber;
    private int lastPacketSize;


    //First Packets
    // [0 - 3] packet no
    // [3 - 7] total packet no
    // [8] info length
    // [9 - ] infoo

    //Others
    // [0 - 3] packet no
    // [4-1028] data

    public ReceivingFile(byte[] infoBytes) {
        this.infoBytes = infoBytes;
    }

    public String parseFistPacket() {
        StringBuilder packetInfo = new StringBuilder();
        byte size = infoBytes[8]; //info lenghth

        byte[] totalPacketNumberBytes = new byte[4];
        System.arraycopy(infoBytes, 4, totalPacketNumberBytes, 0, 4); // 4-5-6-7 totalPacketLenght
        this.totalPacketNumber = bytesToInt(totalPacketNumberBytes); //convert total packet bytes to// int
        System.out.println("Total packet number : " + totalPacketNumber);


        for (int i = 9; i < size + 9; i++) {
            packetInfo.append((char) (infoBytes[i]));
        }

        String packetInfoString = packetInfo.toString();

        String[] splitted = packetInfoString.split("//");

        this.fileName = splitted[0];
        this.fileSize = Integer.valueOf(splitted[1]);

        this.lastPacketSize = Integer.valueOf(splitted[2]);

        this.receivingBytes = new byte[totalPacketNumber - 1][1024];
        this.receivingBytes[totalPacketNumber-2] = new byte[lastPacketSize];

        return "The file will be retrieved in " + totalPacketNumber + " packet. \nFile Name : " + this.fileName + " \nSize : " + this.fileSize + " KB\n" +"Last packet size : "+this.lastPacketSize;
    }

    public void dissambleAllPackets() {

        int length = 0;

        for (byte[] receivingByte1 : receivingBytes) {
            length += receivingByte1.length;
        }
        byte[] value = new byte[length];
        int counter = 0;

        for (byte[] receivingByte : receivingBytes) {

            for (byte b : receivingByte) {
                value[counter] = b;
                counter++;
            }
        }
        this.dissambledPacket = value;

    }

    public void createReceivedFile() {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(this.fileName)));
            bos.write(dissambledPacket);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getLastPacketSize() { return this.lastPacketSize;}

    public int getTotalPacketNumber() {
        return totalPacketNumber;
    }

    public static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xff) << 24) |
                ((bytes[1] & 0xff) << 16) |
                ((bytes[2] & 0xff) << 8) |
                (bytes[3] & 0xff);
    }



}
