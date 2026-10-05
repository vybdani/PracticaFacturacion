package ni.edu.uam.sistemafacturacion.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // PostgreSQL 18 escucha en el puerto 5433 (el 5432 es de PostgreSQL 17).
    // connectTimeout: si el servidor no responde, se informa el error a los 5 segundos.
    private static final String URL =
            "jdbc:postgresql://localhost:5433/facturacion_db?connectTimeout=5";

    private static final String USER = "postgres";

    private static final String PASSWORD = "9deoctubre";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
