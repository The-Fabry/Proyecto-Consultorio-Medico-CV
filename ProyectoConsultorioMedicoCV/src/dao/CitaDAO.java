/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Conexion.ConexionBD;
import model.Especialista;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CitaDAO {

    // Método para cargar los especialistas en el ComboBox de la vista Agendamiento
    public List<Especialista> listarEspecialistas() {
        List<Especialista> lista = new ArrayList<>();
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        String sql = "SELECT * FROM especialistas";
        
        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Especialista esp = new Especialista(
                    rs.getInt("id_especialista"),
                    rs.getString("nombre_completo"),
                    rs.getString("especialidad")
                );
                lista.add(esp);
            }
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar especialistas: " + e.getMessage());
        }
        return lista;
    }

    // Método para registrar la cita y el cliente en la BD
    public boolean guardarCita(String cedula, String nombre, String apellido, String telefono, int idEspecialista, String fechaHora, String codigoBarras) {
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        
        try {
            // 1. Primero insertamos o verificamos al cliente
            String sqlCliente = "INSERT INTO clientes (cedula, nombre, apellido, telefono) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE nombre=?, apellido=?, telefono=?";
            PreparedStatement pstCliente = cn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
            pstCliente.setString(1, cedula);
            pstCliente.setString(2, nombre);
            pstCliente.setString(3, apellido);
            pstCliente.setString(4, telefono);
            pstCliente.setString(5, nombre);
            pstCliente.setString(6, apellido);
            pstCliente.setString(7, telefono);
            pstCliente.executeUpdate();
            
            // Obtener el ID del cliente (nuevo o existente)
            ResultSet rsKeys = pstCliente.getGeneratedKeys();
            int idCliente = 0;
            if (rsKeys.next()) {
                idCliente = rsKeys.getInt(1);
            } else {
                // Si ya existía, consultamos su ID por la cédula
                PreparedStatement pstId = cn.prepareStatement("SELECT id_cliente FROM clientes WHERE cedula = ?");
                pstId.setString(1, cedula);
                ResultSet rsId = pstId.executeQuery();
                if (rsId.next()) {
                    idCliente = rsId.getInt("id_cliente");
                }
                rsId.close();
                pstId.close();
            }
            rsKeys.close();
            pstCliente.close();

            // 2. Ahora guardamos la cita médica con el código de barras
            String sqlCita = "INSERT INTO citas (id_cliente, id_especialista, fecha_cita, codigo_barras, estado) VALUES (?, ?, ?, ?, 'Pendiente')";
            PreparedStatement pstCita = cn.prepareStatement(sqlCita);
            pstCita.setInt(1, idCliente);
            pstCita.setInt(2, idEspecialista);
            pstCita.setString(3, fechaHora);
            pstCita.setString(4, codigoBarras);
            
            pstCita.executeUpdate();
            pstCita.close();
            cn.close();
            
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar la cita: " + e.getMessage());
            return false;
        }
    }
}
