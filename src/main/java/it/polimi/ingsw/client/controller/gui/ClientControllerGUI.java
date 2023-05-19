package it.polimi.ingsw.client.controller.gui;
import it.polimi.ingsw.client.connection.Server;
import it.polimi.ingsw.client.controller.ClientController;
import it.polimi.ingsw.client.model.ClientModelGUI;
import it.polimi.ingsw.server.clientonserver.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class ClientControllerGUI extends Application implements ClientController {
    private ClientModelGUI model;
    private Server server;
    private Client client;
    private Stage stage;

    public ClientModelGUI getModel(){
        return model;
    }
    public void setModel(ClientModelGUI model) {
        this.model = model;
    }
    public Server getServer() {
        return server;
    }
    public void setServer(Server server) {
        this.server = server;
    }
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
    public boolean gameIsStarted() {
        return model.gameIsStarted();
    }
    public void setChatUpdate() {

    }

    public void loadScene(SceneEnum scene) {
        final FXMLLoader loader = new FXMLLoader(scene.getResource());
        final FxmlController sceneController = scene.getNewController(this);
        loader.setController(sceneController);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Error while loading scene " + scene);
        }
        stage.setScene(new Scene(parent, 800, 800));
    }

    public static Image loadImage(String fileName) {
        try {
            final URL url = ClientControllerGUI
                    .class
                    .getClassLoader()
                    .getResource("gui/gameGraphics/" + fileName);
            if(url == null) {
                throw new IOException("File not found");
            }
            return new Image(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Error while loading image " + fileName + " :" + e.getMessage());
        }
    }

    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("My Shelfie");
        loadScene(SceneEnum.login);
        stage.show();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.show();
    }
    public void errorMessage(String message) {
        ClientControllerGUI.showError(message);
    }

    public void startClient() {launch();}

}
