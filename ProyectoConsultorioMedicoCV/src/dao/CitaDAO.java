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

    // Método para cargar los especialistas en el ComboBox
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

    // Buscar la cédula asociada al usuario logueado en la tabla 'usuarios'
    public String obtenerCedulaPorUsuario(String nombreUsuario) {
        String cedula = null;
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        String sql = "SELECT cedula FROM usuarios WHERE usuario = ?";
        
        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, nombreUsuario);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                cedula = rs.getString("cedula");
            }
            rs.close();
            pst.close();
            cn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar usuario: " + e.getMessage());
        }
        return cedula;
    }

    // Buscar nombre y teléfono del cliente por cédula
    public String[] buscarClientePorCedula(String cedula) {
        String[] datos = null;
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        String sql = "SELECT nombre, telefono FROM clientes WHERE cedula = ?";
        
        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, cedula);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                datos = new String[2];
                datos[0] = rs.getString("nombre");
                datos[1] = rs.getString("telefono");
            }
            
            rs.close();
            pst.close();
            cn.close();
        } catch (SQLException e) {
            // Manejo silencioso
        }
        return datos;
    }

    // ⭐ Valida si el especialista ya tiene una cita ocupada en la fecha y hora exacta
    public boolean existeCita(int idEspecialista, String fechaHora) {
        boolean existe = false;
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        String sql = "SELECT COUNT(*) FROM citas WHERE id_especialista = ? AND fecha_cita = ?";
        
        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, idEspecialista);
            pst.setString(2, fechaHora);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    existe = true; // El horario ya está ocupado
                }
            }
            
            rs.close();
            pst.close();
            cn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar la disponibilidad: " + e.getMessage());
        }
        return existe;
    }

    // Método para registrar la cita y el cliente en la BD
    public boolean guardarCita(String cedula, String nombre, String telefono, int idEspecialista, String fechaHora, String codigoBarras) {
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        
        try {
            // Insertar o actualizar cliente
            String sqlCliente = "INSERT INTO clientes (cedula, nombre, telefono) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE nombre=?, telefono=?";
            PreparedStatement pstCliente = cn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
            pstCliente.setString(1, cedula);
            pstCliente.setString(2, nombre);
            pstCliente.setString(3, telefono);
            pstCliente.setString(4, nombre);
            pstCliente.setString(5, telefono);
            pstCliente.executeUpdate();
            
            // Obtener ID del cliente
            ResultSet rsKeys = pstCliente.getGeneratedKeys();
            int idCliente = 0;
            if (rsKeys.next()) {
                idCliente = rsKeys.getInt(1);
            } else {
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

            // Guardar la cita médica
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

    // Listar todo el historial de citas de un paciente por su cédula
    public List<Object[]> listarCitasPorCedula(String cedula) {
        List<Object[]> listaCitas = new ArrayList<>();
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        
        String sql = "SELECT c.fecha_cita, CONCAT(e.nombre_completo, ' - ', e.especialidad) AS medico, " +
                     "c.codigo_barras, c.estado FROM citas c " +
                     "JOIN clientes cl ON c.id_cliente = cl.id_cliente " +
                     "JOIN especialistas e ON c.id_especialista = e.id_especialista " +
                     "WHERE cl.cedula = ? ORDER BY c.fecha_cita DESC";
        
        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, cedula);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getString("fecha_cita");
                fila[1] = rs.getString("medico");
                fila[2] = rs.getString("codigo_barras");
                fila[3] = rs.getString("estado");
                listaCitas.add(fila);
            }
            
            rs.close();
            pst.close();
            cn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar citas: " + e.getMessage());
        }
        
        return listaCitas;
    }
}