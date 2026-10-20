/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Conexion;

import javax.swing.JOptionPane;
import java.sql.*;

public class ConexionBD {

    public Connection conectar() {
        Connection conn = null;
        try {
            // Driver actualizado para versiones modernas de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Ajusta el nombre de tu base de datos (por ejemplo, 'consultorio_db' o 'cuartob')
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/consultorio_db?useSSL=false&serverTimezone=UTC", "root", "");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + ex.getMessage());
        }
        return conn;
    }
}
