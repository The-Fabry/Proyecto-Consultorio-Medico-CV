/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Conexion.ConexionBD;
import model.Cliente;
import java.sql.*;
import javax.swing.JOptionPane;

public class ClienteDAO {

    public Cliente buscarClientePorCedula(String cedula) {
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        Cliente cliente = null;
        
        try {
            String sql = "SELECT * FROM clientes WHERE cedula = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, cedula);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                cliente = new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono")
                );
            }
            rs.close();
            pst.close();
            cn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar cliente: " + e.getMessage());
        }
        return cliente;
    }
}

