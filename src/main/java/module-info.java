module ni.edu.uam.sistemafacturacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens ni.edu.uam.sistemafacturacion.controller to javafx.fxml;
    exports ni.edu.uam.sistemafacturacion;
}
