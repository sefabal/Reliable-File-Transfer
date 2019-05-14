package UDPModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SendingFile {
    private byte[] fileBytes;
    private byte[][] sendingBytes;
    private String fileStringPath;
    private byte[] fileInfo;
    private int lastPacketSize;
    private int totalPacketSize;

    private File sendFile;

    public SendingFile(String fileStringPath) {
        this.fileStringPath = fileStringPath;
        this.sendFile = new java.io.File(fileStringPath);
    }

    public boolean getFileInfo() throws IOException {
        Path filePath = Paths.get(fileStringPath);
        String fileName;
        long fileSize;

        if (sendFile.exists()) {
            fileName = sendFile.getName();
            fileSize = Files.size(filePath);
            this.fileBytes = this.fileToBytes();
            this.totalPacketSize = (fileBytes.length) / 1024 + 2;
            this.lastPacketSize = this.fileBytes.length % 1024;
            this.fileInfo = createFileInfoByteArray(fileName, fileSize);
            this.sendingBytes = createByteArrayForPackage(this.fileBytes);
            System.out.println("File name :" + fileName + " \n" +
                    "File Size : " + fileSize + "\n" +
                    "Total packet size : " + totalPacketSize + " \n" +
                    "Sending bytes : " + sendingBytes.length + "\n" +
                    "Last packet size : " + this.lastPacketSize);
            return true;
        }

        return false;

    }

    private byte[] createFileInfoByteArray(String fileName, long fileSize) {
        String info = fileName + "//" + fileSize + "//" + lastPacketSize;

        byte[] infoArray = info.getBytes();
        int sendingInfoLength = infoArray.length;
        byte[] sendingInfoByte = new byte[infoArray.length + 9];
        byte[] packetNumber;
        byte[] totalPacketNumber;

        packetNumber = intToBytes(1);
        totalPacketNumber = intToBytes(totalPacketSize);

        System.arraycopy(infoArray, 0, sendingInfoByte, 9, sendingInfoLength);

        //Add packet number to first 4 byte
        System.arraycopy(packetNumber, 0, sendingInfoByte, 0, 4);

        //Add totalPacketSize to first packet
        System.arraycopy(totalPacketNumber, 0, sendingInfoByte, 4, 4);
        sendingInfoByte[8] = (byte) sendingInfoLength;




        return sendingInfoByte;
    }

    public byte[][] createByteArrayForPackage(byte[] bytes) {

        int packetLenght = (bytes.length / 1024) + 2;

        int lenght = bytes.length;

        byte[][] packets = new byte[packetLenght][1028];

        int count = 0;
        int packetNumber = 1;
        for (int i = 0; i < packetLenght; i++) {

            byte[] packetNumberBytes = intToBytes(packetNumber++);
            System.arraycopy(packetNumberBytes, 0, packets[i], 0, packetNumberBytes.length);
            //Add file info to the first packet
            if (i == 0) {
                System.arraycopy(this.fileInfo, 0, packets[i], 0, this.fileInfo.length);
                continue;
            }

            for (int j = 4; j < 1028; j++) {
                if (count < lenght)
                    packets[i][j] = bytes[count++];
                else {
                    if (i == packetLenght - 1)
                        this.lastPacketSize = j;
                    break;
                }
            }

        }

        return packets;
    }

    public byte[] fileToBytes() throws IOException {
        InputStream fis = new FileInputStream(sendFile);
        if (sendFile.length() > Integer.MAX_VALUE)
            throw new IOException("SendingFile size is too big.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) sendFile.length());
        byte[] buffer = new byte[8 * 1024];

        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        fis.close();

        return baos.toByteArray();
    }

    public static byte[] intToBytes(int number) {
        return new byte[]{
                (byte) ((number >>> 24) & 0xff),
                (byte) ((number >>> 16) & 0xff),
                (byte) ((number >>> 8) & 0xff),
                (byte) (number & 0xff)
        };
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public byte[][] getSendingBytes() {
        return sendingBytes;
    }

    public String getFileStringPath() {
        return fileStringPath;
    }

    public int getLastPacketSize() {
        return lastPacketSize;
    }

    public java.io.File getSendFile() {
        return sendFile;
    }

    public int getTotalPacketSize() {
        return this.totalPacketSize;
    }
}
