package org.unibl.etf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Parent loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("simulation.fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML(), 1920, 1200);
        stage.setScene(scene);
        stage.setMaximized(true);
//      stage.setOnCloseRequest(e -> System.exit(0));
//      stage.setFullScreen(true);
        stage.show();
    }
}