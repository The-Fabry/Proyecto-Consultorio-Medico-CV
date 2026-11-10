
package views;

import dao.CitaDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

// Modulo de Nando: se integra dentro de VistaMedico.
public class PanelHistorialMedico extends JPanel {

    private final CitaDAO dao = new CitaDAO();

    private final JTextField txtCedula = new JTextField(13);

    private final JLabel lblCedula = new JLabel("-");
    private final JLabel lblPaciente = new JLabel("-");
    private final JLabel lblUltimaConsulta = new JLabel("-");

    private final JComboBox<CitaItem> cmbCitas =
            new JComboBox<>();

    private final JTextArea txtDiagnostico =
            new JTextArea(3, 20);

    private final JTextArea txtNotas =
            new JTextArea(3, 20);

    private final JButton btnGuardar =
            new JButton("Guardar diagnostico y notas");

    private final DefaultTableModel modelo =
        new DefaultTableModel(
            new Object[]{
                "Cita", "Fecha", "Medico",
                "Diagnostico", "Notas"
            }, 0) {

            @Override
            public boolean isCellEditable(
                    int fila, int columna) {
                return false;
            }
        };

    private final JTable tblHistorial = new JTable(modelo);

    private String cedulaActual = null;
    private boolean cargando = false;

    // Clase auxiliar para las citas
    private static class CitaItem {

        final int id;
        final String descripcion;

        CitaItem(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    // Constructor
    public PanelHistorialMedico() {
        construirInterfaz();
        btnGuardar.setEnabled(false);
    }

    // ========================================
    // CONSTRUCCION DE LA INTERFAZ
    // ========================================
    private void construirInterfaz() {

        setLayout(new BorderLayout(8, 8));

        setBorder(BorderFactory.createTitledBorder(
            "Historial clinico del paciente"
        ));

        setPreferredSize(new Dimension(580, 600));

        // PANEL SUPERIOR: BUSQUEDA
        JPanel superior =
            new JPanel(new BorderLayout(5, 5));

        JPanel busqueda =
            new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnBuscar =
            new JButton("Buscar cedula");

        busqueda.add(new JLabel("Cedula:"));
        busqueda.add(txtCedula);
        busqueda.add(btnBuscar);

        superior.add(busqueda, BorderLayout.NORTH);

        // INFORMACION DEL PACIENTE
        JPanel datos =
            new JPanel(new GridLayout(3, 2, 4, 4));

        datos.setBorder(
            BorderFactory.createTitledBorder(
                "Datos del paciente"
            )
        );

        datos.add(new JLabel("Cedula:"));
        datos.add(lblCedula);

        datos.add(new JLabel("Paciente:"));
        datos.add(lblPaciente);

        datos.add(new JLabel("Ultima consulta registrada:"));
        datos.add(lblUltimaConsulta);

        superior.add(datos, BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);

        // ========================================
        // TABLA DEL HISTORIAL CLINICO
        // ========================================
        JPanel central = new JPanel(new BorderLayout());

        central.setBorder(
            BorderFactory.createTitledBorder(
                "Consultas anteriores"
            )
        );

        tblHistorial.setAutoCreateRowSorter(true);
        UIClinico.estilizarTabla(tblHistorial);
        UIClinico.estilizarBotonPrimario(btnBuscar);
        UIClinico.estilizarBotonExito(btnGuardar);
        UIClinico.estilizarCampoTexto(txtCedula);
        UIClinico.estilizarCombo(cmbCitas);
        btnGuardar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        central.add(
            new JScrollPane(tblHistorial),
            BorderLayout.CENTER
        );

        add(central, BorderLayout.CENTER);

        // ========================================
        // FORMULARIO DE DIAGNOSTICO Y NOTAS
        // ========================================
        JPanel inferior =
            new JPanel(new BorderLayout(5, 5));

        inferior.setBorder(
            BorderFactory.createTitledBorder(
                "Diagnostico y notas medicas"
            )
        );

        JPanel seleccionar =
            new JPanel(new BorderLayout(5, 5));

        seleccionar.add(
            new JLabel("Cita:"),
            BorderLayout.WEST
        );

        seleccionar.add(
            cmbCitas,
            BorderLayout.CENTER
        );

        inferior.add(
            seleccionar,
            BorderLayout.NORTH
        );

        // CAMPOS DE TEXTO
        JPanel campos =
            new JPanel(new GridLayout(2, 1, 4, 4));

        txtDiagnostico.setLineWrap(true);
        txtDiagnostico.setWrapStyleWord(true);

        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);

        JPanel bloqueDiagnostico =
            new JPanel(new BorderLayout());

        bloqueDiagnostico.add(
            new JLabel("Diagnostico:"),
            BorderLayout.NORTH
        );

        bloqueDiagnostico.add(
            new JScrollPane(txtDiagnostico),
            BorderLayout.CENTER
        );

        JPanel bloqueNotas =
            new JPanel(new BorderLayout());

        bloqueNotas.add(
            new JLabel("Observaciones:"),
            BorderLayout.NORTH
        );

        bloqueNotas.add(
            new JScrollPane(txtNotas),
            BorderLayout.CENTER
        );

        campos.add(bloqueDiagnostico);
        campos.add(bloqueNotas);

        inferior.add(campos, BorderLayout.CENTER);

        // BOTON GUARDAR
        JPanel acciones =
            new JPanel(new FlowLayout(FlowLayout.RIGHT));

        acciones.add(btnGuardar);

        inferior.add(
            acciones,
            BorderLayout.SOUTH
        );

        inferior.setPreferredSize(
            new Dimension(560, 280)
        );

        add(inferior, BorderLayout.SOUTH);

        // ========================================
        // EVENTOS DE LOS BOTONES
        // ========================================

        btnBuscar.addActionListener(e ->
            consultarHistorialPaciente()
        );

        txtCedula.addActionListener(e ->
            consultarHistorialPaciente()
        );

        cmbCitas.addActionListener(e ->
            mostrarDiagnosticoDeCita()
        );

        btnGuardar.addActionListener(e ->
            guardarDiagnostico()
        );

        // Seleccionar una cita desde la tabla
        tblHistorial.getSelectionModel()
            .addListSelectionListener(e -> {

                if (!e.getValueIsAdjusting()
                        && tblHistorial.getSelectedRow() >= 0) {

                    int fila =
                        tblHistorial.convertRowIndexToModel(
                            tblHistorial.getSelectedRow()
                        );

                    int idCita = ((Number)
                        modelo.getValueAt(fila, 0)
                    ).intValue();

                    for (int i = 0;
                         i < cmbCitas.getItemCount(); i++) {

                        if (cmbCitas.getItemAt(i).id == idCita) {

                            cmbCitas.setSelectedIndex(i);

                            mostrarDiagnosticoDeCita();

                            break;
                        }
                    }
                }
            });
    }

    // ========================================
    // BUSCAR PACIENTE POR CEDULA
    // ========================================
    private void consultarHistorialPaciente() {

        String cedula = txtCedula.getText().trim();

        limpiarResultados();

        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Ingrese una cedula."
            );
            return;
        }

        try {

            String[] paciente =
                dao.obtenerPacienteParaHistorial(cedula);

            if (paciente == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Paciente no encontrado."
                );
                return;
            }

            cedulaActual = paciente[0];

            lblCedula.setText(paciente[0]);
            lblPaciente.setText(paciente[1]);
            lblUltimaConsulta.setText(paciente[2]);

            // CARGAR CITAS DEL PACIENTE
            cargando = true;

            try {

                List<Object[]> citas =
                    dao.listarCitasParaHistorial(
                        cedulaActual
                    );

                for (Object[] cita : citas) {

                    int id =
                        ((Number) cita[0]).intValue();

                    String descripcion =
                        "#" + id +
                        " | " + cita[1] +
                        " | " + cita[2] +
                        " | " + cita[3];

                    cmbCitas.addItem(
                        new CitaItem(id, descripcion)
                    );
                }

                actualizarTabla();

            } finally {
                cargando = false;
            }

            btnGuardar.setEnabled(
                cmbCitas.getItemCount() > 0
            );

            mostrarDiagnosticoDeCita();

        } catch (SQLException ex) {
            limpiarResultados();
            mostrarError(ex);
        }
    }

    // ========================================
    // CARGAR EL HISTORIAL EN LA TABLA
    // ========================================
    private void actualizarTabla()
            throws SQLException {

        boolean anterior = cargando;
        cargando = true;

        try {

            modelo.setRowCount(0);

            List<Object[]> historial =
                dao.listarHistorialClinicoPorCedula(
                    cedulaActual
                );

            for (Object[] fila : historial) {
                modelo.addRow(fila);
            }

        } finally {
            cargando = anterior;
        }
    }

    // ========================================
    // MOSTRAR DIAGNOSTICO DE UNA CITA
    // ========================================
    private void mostrarDiagnosticoDeCita() {

        if (cargando) {
            return;
        }

        txtDiagnostico.setText("");
        txtNotas.setText("");

        CitaItem seleccion =
            (CitaItem) cmbCitas.getSelectedItem();

        if (seleccion == null) {
            return;
        }

        for (int i = 0;
             i < modelo.getRowCount(); i++) {

            int id = ((Number)
                modelo.getValueAt(i, 0)
            ).intValue();

            if (id == seleccion.id) {

                Object diagnostico =
                    modelo.getValueAt(i, 3);

                Object notas =
                    modelo.getValueAt(i, 4);

                txtDiagnostico.setText(
                    diagnostico == null
                        ? ""
                        : diagnostico.toString()
                );

                txtNotas.setText(
                    notas == null
                        ? ""
                        : notas.toString()
                );

                return;
            }
        }
    }

    // ========================================
    // GUARDAR DIAGNOSTICO EN MYSQL
    // ========================================
    private void guardarDiagnostico() {

        CitaItem seleccion =
            (CitaItem) cmbCitas.getSelectedItem();

        String diagnostico =
            txtDiagnostico.getText().trim();

        if (cedulaActual == null
                || seleccion == null
                || diagnostico.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Busque el paciente, seleccione una cita "
                + "e ingrese un diagnostico."
            );
            return;
        }

        int confirmar =
            JOptionPane.showConfirmDialog(
                this,
                "Guardar el diagnostico de la cita #"
                + seleccion.id + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
            );

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean guardado =
                dao.guardarDiagnosticoYNotas(
                    seleccion.id,
                    cedulaActual,
                    diagnostico,
                    txtNotas.getText().trim()
                );

            if (!guardado) {

                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el diagnostico."
                );

                return;
            }

            // ACTUALIZAR LA TABLA
            actualizarTabla();

            String[] paciente =
                dao.obtenerPacienteParaHistorial(
                    cedulaActual
                );

            if (paciente != null) {
                lblUltimaConsulta.setText(
                    paciente[2]
                );
            }

            mostrarDiagnosticoDeCita();

            JOptionPane.showMessageDialog(
                this,
                "Diagnostico y notas guardados."
            );

        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    // ========================================
    // LIMPIAR LOS CAMPOS
    // ========================================
    private void limpiarResultados() {

        cargando = true;

        try {

            cedulaActual = null;

            lblCedula.setText("-");
            lblPaciente.setText("-");
            lblUltimaConsulta.setText("-");

            cmbCitas.removeAllItems();

            modelo.setRowCount(0);

            txtDiagnostico.setText("");
            txtNotas.setText("");

            btnGuardar.setEnabled(false);

        } finally {
            cargando = false;
        }
    }

    // ========================================
    // MOSTRAR ERRORES DE MYSQL
    // ========================================
    private void mostrarError(SQLException ex) {

        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Error de MySQL",
            JOptionPane.ERROR_MESSAGE
        );
    }

    
}
