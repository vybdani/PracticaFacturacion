package ni.edu.uam.sistemafacturacion.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ni.edu.uam.sistemafacturacion.App;

import java.io.IOException;

public class MainController {
    @FXML
    private Button btnCategorias;

    @FXML
    private Button btnSalir;


    @FXML
    public void initialize() {

        System.out.println(
                "MainController inicializado correctamente."
        );
    }


    /**
     * Abre la ventana de categorías.
     */
    @FXML
    private void abrirCategorias() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource(
                            "/ni/edu/uam/sistemafacturacion/view/categoria-view.fxml"
                    )
            );

            Scene scene = new Scene(
                    loader.load(),
                    850,
                    600
            );

            Stage stage = new Stage();

            stage.setTitle(
                    "Gestión de Categorías"
            );

            stage.setScene(scene);

            stage.setMinWidth(750);
            stage.setMinHeight(500);

            /*
             * Impide trabajar con la ventana principal
             * mientras la ventana de categorías esté abierta.
             */
            stage.initModality(
                    Modality.WINDOW_MODAL
            );

            /*
             * Hace que la nueva ventana dependa
             * de la ventana principal.
             */
            Stage ventanaPrincipal =
                    (Stage) btnCategorias
                            .getScene()
                            .getWindow();

            stage.initOwner(ventanaPrincipal);

            stage.showAndWait();

        } catch (IOException e) {

            mostrarError(
                    "Error al abrir categorías",
                    e.getMessage()
            );
        }
    }


    /**
     * Cierra la aplicación.
     */
    @FXML
    private void salir() {

        Stage stage =
                (Stage) btnSalir
                        .getScene()
                        .getWindow();

        stage.close();
    }


    /**
     * Muestra una ventana de error.
     */
    private void mostrarError(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(
                mensaje != null
                        ? mensaje
                        : "Se produjo un error inesperado."
        );
        System.out.println("Error: " + mensaje);

        alert.showAndWait();
    }
}
