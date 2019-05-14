# RELIABLE FILE TRANSFER APP

- You can use this app with Java9.

- "mvn package" will create a fileTransferApp.jar file inside target directory.

You can open file 

- java -jar fileTransferApp.jar 

You can transfer files with 2 options.

- UDP
- TCP/IP

UDP Options include some extra methods for reliability.
First packet include file informations for handshaking. Each file's packets ordered starting from 1 to FileSize/1024.
Receiver has a chance to accept or decline the file.
Each packet's first 4 bytes is packetNumber.
App is handling the transferred packets number between receiver and server side.