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

public class EspecialistaDAO {

    public List<Especialista> obtenerEspecialistas() {
        List<Especialista> lista = new ArrayList<>();
        ConexionBD cc = new ConexionBD();
        Connection cn = cc.conectar();
        
        try {
            String sql = "SELECT * FROM especialistas";
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
            JOptionPane.showMessageDialog(null, "Error al obtener especialistas: " + e.getMessage());
        }
        return lista;
    }
}

