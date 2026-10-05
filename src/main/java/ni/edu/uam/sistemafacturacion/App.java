package ni.edu.uam.sistemafacturacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    // Hoja de estilos usada por todas las ventanas
    public static final String TEMA_CSS = "/ni/edu/uam/sistemafacturacion/css/theme.css";

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource(
                        "/ni/edu/uam/sistemafacturacion/view/main-view.fxml"
                )
        );

        Scene scene = new Scene(
                fxmlLoader.load(),
                1000,
                650
        );

        scene.getStylesheets().add(
                App.class.getResource(TEMA_CSS).toExternalForm()
        );

        stage.setTitle("Sistema de Facturación");

        stage.setScene(scene);

        stage.setMinWidth(800);
        stage.setMinHeight(500);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
