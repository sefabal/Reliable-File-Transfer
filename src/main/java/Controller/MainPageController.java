package Controller;

import TCPService.TCPFileReceiver;
import TCPService.TCPFileSender;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import UDPModel.SendingFile;
import UDPService.UDPFileReceiver;
import UDPService.UDPFileSender;

import javax.swing.*;
import java.io.File;
import java.io.IOException;


public class MainPageController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private ComboBox<String> transferProtocolChooser;

    @FXML
    private Button sendFileButton;

    @FXML
    private Button receiveFileButton;

    @FXML
    private Text transferProtocolText;

    @FXML
    private Text currentModeText;

    @FXML
    private TextField ipAddressVal;

    @FXML
    private TextField sleepTime;

    @FXML
    private TextField sleepTimeNs;


    private String selectedProtocol;

    @FXML
    void initialize() {
        transferProtocolChooser.getItems().addAll("TCP/IP", "UDP");
    }

    @FXML
    void receiveFileClicked(ActionEvent event) throws IOException {
        selectedProtocol = transferProtocolChooser.getSelectionModel().getSelectedItem();
        receiveFileButton.disableProperty().setValue(true);
        currentModeText.setText("Receiving");
        Service fileReceiver;
        if (selectedProtocol.equals("UDP")) {
            fileReceiver = new UDPFileReceiver();
        } else {
            fileReceiver = new TCPFileReceiver(6666);
        }

        new Thread(fileReceiver::start).start();

        progressBar.progressProperty().bind(fileReceiver.progressProperty());


        fileReceiver.setOnSucceeded(e -> {
            showMessageDialog("File received succesfully!");
            receiveFileButton.disableProperty().setValue(false);
        });

    }


    @FXML
    void sendFileClicked(ActionEvent event) throws IOException {
        selectedProtocol = transferProtocolChooser.getSelectionModel().getSelectedItem();
        receiveFileButton.disableProperty().setValue(true);

        String ipAddress = ipAddressVal.getText();

        String sleepTimeMS = sleepTime.getText();
        String sleepTimeNS = sleepTimeNs.getText();
        int sleepTimeMSVal;
        int sleepTimeNSVal = 200000;
        if (!sleepTimeNS.isEmpty())
            sleepTimeNSVal = Integer.valueOf(sleepTimeNS);
        if (sleepTimeMS.isEmpty())
            sleepTimeMSVal = 1;
        else
            sleepTimeMSVal = Integer.valueOf(sleepTimeMS);

        currentModeText.setText("Sending");
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(new Stage());
        Service fileSender;
        if (file.exists()) {
            sendFileButton.disableProperty().setValue(true);
            SendingFile sendingFile = new SendingFile(file.getPath());

            if (selectedProtocol.equals("UDP")) {
                fileSender = new UDPFileSender(sendingFile, ipAddress);
            } else {
                fileSender = new TCPFileSender(ipAddress, 6666, file.getPath(), sleepTimeMSVal, sleepTimeNSVal);
            }

            new Thread(fileSender::start).start();

            progressBar.progressProperty().bind(fileSender.progressProperty());

            fileSender.setOnSucceeded(e -> {
                showMessageDialog("File sended succesfully.");
                receiveFileButton.disableProperty().setValue(false);
            });
        } else {
            JOptionPane.showMessageDialog(null, "You must select valid file!");
        }
    }


    public static void showMessageDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();

    }

    @FXML
    void protocolChanged(ActionEvent event) {
        this.selectedProtocol = transferProtocolChooser.getSelectionModel().getSelectedItem();
        this.transferProtocolText.setText(selectedProtocol);
    }
}
