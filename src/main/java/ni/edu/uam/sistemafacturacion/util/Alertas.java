package ni.edu.uam.sistemafacturacion.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.sql.SQLException;

// Ventanas de mensaje compartidas por todos los controladores
public final class Alertas {

    private Alertas() {
    }

    public static void informacion(String titulo, String mensaje) {
        crear(Alert.AlertType.INFORMATION, titulo, mensaje).showAndWait();
    }

    public static void advertencia(String titulo, String mensaje) {
        crear(Alert.AlertType.WARNING, titulo, mensaje).showAndWait();
    }

    public static void error(String titulo, String mensaje) {
        crear(Alert.AlertType.ERROR, titulo,
                mensaje != null ? mensaje : "Se produjo un error inesperado.").showAndWait();
    }

    // Devuelve true solo si el usuario presionó Aceptar
    public static boolean confirmar(String titulo, String mensaje) {
        return crear(Alert.AlertType.CONFIRMATION, titulo, mensaje)
                .showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .isPresent();
    }

    /*
     * Muestra un error de base de datos con un mensaje comprensible.
     * El detalle técnico de la SQLException solo se escribe en la consola.
     */
    public static void errorBaseDatos(String mensaje, SQLException e) {
        System.err.println("[SQLState " + e.getSQLState() + "] " + e.getMessage());

        error("Error de base de datos", mensaje + "\n\n" + explicar(e));
    }

    // Traduce el código SQLState de PostgreSQL a una explicación para el usuario
    private static String explicar(SQLException e) {
        String estado = e.getSQLState() != null ? e.getSQLState() : "";

        if (estado.startsWith("08")) {
            return "No hay conexión con el servidor de base de datos. "
                    + "Verifique que PostgreSQL esté en ejecución e intente de nuevo.";
        }
        if (estado.equals("28P01") || estado.equals("28000")) {
            return "El usuario o la contraseña de la base de datos no son válidos.";
        }
        if (estado.equals("3D000")) {
            return "La base de datos facturacion_db no existe.";
        }
        if (estado.equals("23505")) {
            return "Ya existe un registro con esos datos.";
        }
        if (estado.equals("23503")) {
            return "El registro está relacionado con otros datos.";
        }
        if (estado.equals("23514") || estado.equals("23502")) {
            return "Uno de los valores no es permitido por la base de datos.";
        }
        return "No fue posible completar la operación.";
    }

    private static Alert crear(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);

        // El mensaje aparece encima de la ventana en la que se está trabajando (nunca detrás)
        Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .ifPresent(alert::initOwner);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert;
    }
}
