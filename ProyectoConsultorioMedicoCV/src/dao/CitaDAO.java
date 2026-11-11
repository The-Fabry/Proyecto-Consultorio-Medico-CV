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
import javax.swing.table.DefaultTableModel;

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

    // Valida si el especialista ya tiene una cita ocupada en la fecha y hora exacta
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
            String sqlCliente = "INSERT INTO clientes (cedula, nombre, telefono) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE nombre=?, telefono=?";
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

        String sql = "SELECT c.fecha_cita, CONCAT(e.nombre_completo, ' - ', e.especialidad) AS medico, "
                + "c.codigo_barras, c.estado FROM citas c "
                + "JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "JOIN especialistas e ON c.id_especialista = e.id_especialista "
                + "WHERE cl.cedula = ? ORDER BY c.fecha_cita DESC";

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

    public DefaultTableModel obtenerCitasPorMedico(int idEspecialista) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Cita #");
        modelo.addColumn("Fecha/Hora");
        modelo.addColumn("Paciente");
        modelo.addColumn("Cédula");
        modelo.addColumn("Estado");

        String sql = "SELECT c.id_cita, c.fecha_cita, cl.nombre, cl.cedula, c.estado "
                + "FROM citas c "
                + "JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "WHERE c.id_especialista = ?";

        try (Connection cn = new ConexionBD().conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialista);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[5];
                    fila[0] = "CITA-" + rs.getInt("id_cita");
                    fila[1] = rs.getString("fecha_cita");
                    fila[2] = rs.getString("nombre");
                    fila[3] = rs.getString("cedula");
                    fila[4] = rs.getString("estado");
                    modelo.addRow(fila);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar citas del médico: " + e.getMessage());
        }

        return modelo;
    }

    // =============================================================
    // MODULO DE NANDO - Historial clinico del paciente
    // Mantiene la conexion existente de ConexionBD.
    // =============================================================
    private Connection conectarHistorial() throws SQLException {
        Connection conexion = new ConexionBD().conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo conectar con consultorio_db. Verifica MySQL y JDBC.");
        }
        return conexion;
    }

    // Busca un paciente y su ultima consulta clinica REGISTRADA.
    public String[] obtenerPacienteParaHistorial(String cedula) throws SQLException {
        String sql = "SELECT cl.cedula, cl.nombre, MAX(h.fecha_registro) AS ultima_consulta "
                + "FROM clientes cl "
                + "LEFT JOIN citas c ON c.id_cliente = cl.id_cliente "
                + "LEFT JOIN historial_clinico h ON h.id_cita = c.id_cita "
                + "WHERE cl.cedula = ? "
                + "GROUP BY cl.id_cliente, cl.cedula, cl.nombre "
                + "ORDER BY cl.id_cliente LIMIT 1";

        try (Connection cn = conectarHistorial(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Timestamp ultima = rs.getTimestamp("ultima_consulta");
                return new String[]{
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    ultima == null ? "Sin consultas registradas" : ultima.toString()
                };
            }
        }
    }

    // Las citas disponibles para asociar un diagnostico al paciente.
    public List<Object[]> listarCitasParaHistorial(String cedula) throws SQLException {
        List<Object[]> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.fecha_cita, e.nombre_completo, c.estado "
                + "FROM citas c "
                + "INNER JOIN clientes cl ON cl.id_cliente = c.id_cliente "
                + "INNER JOIN especialistas e ON e.id_especialista = c.id_especialista "
                + "WHERE cl.cedula = ? ORDER BY c.fecha_cita DESC";

        try (Connection cn = conectarHistorial(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    citas.add(new Object[]{
                        rs.getInt("id_cita"),
                        rs.getTimestamp("fecha_cita"),
                        rs.getString("nombre_completo"),
                        rs.getString("estado")
                    });
                }
            }
        }
        return citas;
    }

    // Devuelve solo consultas CON diagnostico/notas registrados.
    public List<Object[]> listarHistorialClinicoPorCedula(String cedula) throws SQLException {
        List<Object[]> historial = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.fecha_cita, e.nombre_completo, "
                + "h.diagnostico, h.notas "
                + "FROM historial_clinico h "
                + "INNER JOIN citas c ON c.id_cita = h.id_cita "
                + "INNER JOIN clientes cl ON cl.id_cliente = c.id_cliente "
                + "INNER JOIN especialistas e ON e.id_especialista = c.id_especialista "
                + "WHERE cl.cedula = ? ORDER BY c.fecha_cita DESC";

        try (Connection cn = conectarHistorial(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(new Object[]{
                        rs.getInt("id_cita"),
                        rs.getTimestamp("fecha_cita"),
                        rs.getString("nombre_completo"),
                        rs.getString("diagnostico"),
                        rs.getString("notas")
                    });
                }
            }
        }
        return historial;
    }

    // Guarda por cita. Si ya hay un registro para esa cita, lo actualiza.
    public boolean guardarDiagnosticoYNotas(int idCita, String cedula,
            String diagnostico, String notas) throws SQLException {
        if (idCita <= 0 || cedula == null || cedula.trim().isEmpty()
                || diagnostico == null || diagnostico.trim().isEmpty()) {
            return false;
        }
        String sql = "INSERT INTO historial_clinico (id_cita, diagnostico, notas) "
                + "SELECT c.id_cita, ?, ? FROM citas c "
                + "INNER JOIN clientes cl ON cl.id_cliente = c.id_cliente "
                + "WHERE c.id_cita = ? AND cl.cedula = ? "
                + "ON DUPLICATE KEY UPDATE diagnostico = ?, notas = ?, "
                + "fecha_registro = CURRENT_TIMESTAMP";
        try (Connection cn = conectarHistorial(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, diagnostico.trim());
            ps.setString(2, notas == null ? "" : notas.trim());
            ps.setInt(3, idCita);
            ps.setString(4, cedula.trim());
            ps.setString(5, diagnostico.trim());
            ps.setString(6, notas == null ? "" : notas.trim());
            boolean exito = ps.executeUpdate() > 0;
            if (exito) {
                // Al registrar diagnóstico y notas, la cita pasa automáticamente a estado 'Finalizada'
                actualizarEstadoCita(idCita, "Finalizada");
            }
            return exito;
        }
    }

    // Obtener el ID del especialista según el nombre de usuario logueado en sesión
    public int obtenerIdEspecialistaPorUsuario(String usuario) {
        int idEspecialista = 1; // Fallback seguro
        if (usuario == null || usuario.trim().isEmpty()) {
            return idEspecialista;
        }

        try (Connection cn = new ConexionBD().conectar()) {
            // 1. Buscar si en especialistas coincide con el nombre del usuario
            String sqlNombre = "SELECT id_especialista FROM especialistas WHERE LOWER(nombre_completo) LIKE LOWER(?) LIMIT 1";
            try (PreparedStatement ps = cn.prepareStatement(sqlNombre)) {
                ps.setString(1, "%" + usuario.trim() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id_especialista");
                    }
                }
            }

            // 2. Buscar si la cédula del usuario coincide en especialistas
            String sqlUsuario = "SELECT cedula FROM usuarios WHERE usuario = ?";
            try (PreparedStatement ps = cn.prepareStatement(sqlUsuario)) {
                ps.setString(1, usuario.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String cedula = rs.getString("cedula");
                        if (cedula != null && !cedula.trim().isEmpty()) {
                            try (PreparedStatement psEsp = cn.prepareStatement("SELECT id_especialista FROM especialistas WHERE cedula = ? LIMIT 1")) {
                                psEsp.setString(1, cedula.trim());
                                try (ResultSet rsEsp = psEsp.executeQuery()) {
                                    if (rsEsp.next()) {
                                        return rsEsp.getInt("id_especialista");
                                    }
                                }
                            } catch (SQLException ignored) {}
                        }
                    }
                }
            }
        } catch (SQLException ignored) {}

        return idEspecialista;
    }

    // Obtener el nombre y especialidad por ID
    public String obtenerNombreEspecialista(int idEspecialista) {
        String nombre = "Médico Especialista";
        String sql = "SELECT nombre_completo, especialidad FROM especialistas WHERE id_especialista = ?";
        try (Connection cn = new ConexionBD().conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEspecialista);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre_completo") + " (" + rs.getString("especialidad") + ")";
                }
            }
        } catch (SQLException ignored) {}
        return nombre;
    }

    // =============================================================
    // MODULO DE ANAHI - Busqueda de cita por codigo de barras
    // =============================================================
    // Devuelve: [0]=cedula, [1]=paciente, [2]=id_cita, [3]=fecha_cita, [4]=especialista, [5]=estado
    // Devuelve null si el codigo no existe.
    public String[] buscarCitaPorCodigoBarras(String codigoBarras) {
        String[] datos = null;
        String sql = "SELECT cl.cedula, cl.nombre, c.id_cita, c.fecha_cita, "
                + "e.nombre_completo, c.estado "
                + "FROM citas c "
                + "JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "JOIN especialistas e ON c.id_especialista = e.id_especialista "
                + "WHERE c.codigo_barras = ?";

        try (Connection cn = new ConexionBD().conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    datos = new String[6];
                    datos[0] = rs.getString("cedula");
                    datos[1] = rs.getString("nombre");
                    datos[2] = String.valueOf(rs.getInt("id_cita"));
                    datos[3] = rs.getString("fecha_cita");
                    datos[4] = rs.getString("nombre_completo");
                    datos[5] = rs.getString("estado");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar el ticket: " + e.getMessage());
        }
        return datos;
    }

    // =============================================================
    // MODULO DE CELESTE - Cambio de estado de la cita
    // =============================================================
    public boolean actualizarEstadoCita(int idCita, String nuevoEstado) {
        String sql = "UPDATE citas SET estado = ? WHERE id_cita = ?";

        try (Connection cn = new ConexionBD().conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el estado de la cita: " + e.getMessage());
            return false;
        }
    }

    public String obtenerEstadoCita(int idCita) {
        String estado = null;
        String sql = "SELECT estado FROM citas WHERE id_cita = ?";

        try (Connection cn = new ConexionBD().conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estado = rs.getString("estado");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar el estado: " + e.getMessage());
        }
        return estado;
    }
}
