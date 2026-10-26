
package views;

import dao.CitaDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

public class PanelHistorialMedico extends JPanel {

    // CONEXION A LOS METODOS DEL DAO
    private final CitaDAO dao = new CitaDAO();

    // COMPONENTES DE BUSQUEDA
    private final JTextField txtCedula =
            new JTextField(15);

    private final JLabel lblPaciente = new JLabel("-");
    private final JLabel lblCedula = new JLabel("-");
    private final JLabel lblUltimaConsulta =
            new JLabel("-");

    // COMPONENTES DE HISTORIAL
    private final JComboBox<CitaOpcion> cmbCitas =
            new JComboBox<>();

    private final JTextArea txtDiagnostico =
            new JTextArea(3, 30);

    private final JTextArea txtNotas =
            new JTextArea(3, 30);

    private final JButton btnGuardar =
            new JButton("Guardar Diagnostico y Notas");

    // MODELO DE LA TABLA
    private final DefaultTableModel modeloHistorial =
        new DefaultTableModel(
            new String[]{
                "Cita #",
                "Fecha/Hora",
                "Medico",
                "Diagnostico",
                "Notas"
            }, 0) {

            @Override
            public boolean isCellEditable(
                    int fila, int columna) {
                return false;
            }
        };

    private final JTable tblHistorial =
            new JTable(modeloHistorial);

    private String cedulaBuscada = null;

    // CLASE PARA REPRESENTAR UNA CITA
    private static class CitaOpcion {

        final int id;
        final String descripcion;

        CitaOpcion(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    // ====================================
    // CONSTRUCTOR
    // ====================================
    public PanelHistorialMedico() {
        construirPanel();
        btnGuardar.setEnabled(false);
    }

    // ====================================
    // DISEÑO DE LA INTERFAZ
    // ====================================
    private void construirPanel() {

        setLayout(new BorderLayout(8, 8));

        setBorder(BorderFactory.createTitledBorder(
            "Historial Clinico del Paciente - Nando"
        ));

        setPreferredSize(new Dimension(620, 620));

        // PANEL DE BUSQUEDA
        JPanel encabezado =
            new JPanel(new BorderLayout(5, 5));

        JPanel buscar =
            new JPanel(new FlowLayout(FlowLayout.LEFT));

        buscar.add(new JLabel("Buscar Cedula:"));
        buscar.add(txtCedula);

        JButton btnBuscar =
            new JButton("Buscar Cedula");

        buscar.add(btnBuscar);

        encabezado.add(
            buscar, BorderLayout.NORTH
        );

        // DATOS DEL PACIENTE
        JPanel datos =
            new JPanel(new GridLayout(3, 2, 4, 4));

        datos.setBorder(
            BorderFactory.createTitledBorder(
                "Informacion del paciente"
            )
        );

        datos.add(new JLabel("Cedula:"));
        datos.add(lblCedula);

        datos.add(new JLabel("Paciente:"));
        datos.add(lblPaciente);

        datos.add(new JLabel("Ultima Consulta:"));
        datos.add(lblUltimaConsulta);

        encabezado.add(
            datos, BorderLayout.CENTER
        );

        add(encabezado, BorderLayout.NORTH);

        // ====================================
        // TABLA DE HISTORIAL
        // ====================================
        JPanel centro =
            new JPanel(new BorderLayout(5, 5));

        centro.setBorder(
            BorderFactory.createTitledBorder(
                "Historial de Atenciones Previas"
            )
        );

        tblHistorial.setRowHeight(24);
        tblHistorial.setAutoCreateRowSorter(true);

        JScrollPane tablaScroll =
            new JScrollPane(tblHistorial);

        centro.add(
            tablaScroll, BorderLayout.CENTER
        );

        add(centro, BorderLayout.CENTER);

        // ====================================
        // FORMULARIO DE DIAGNOSTICO
        // ====================================
        JPanel formulario =
            new JPanel(new BorderLayout(6, 6));

        formulario.setBorder(
            BorderFactory.createTitledBorder(
                "Notas Medicas / Diagnostico Actual"
            )
        );

        JPanel seleccionarCita =
            new JPanel(new BorderLayout(5, 5));

        seleccionarCita.add(
            new JLabel("Cita a atender:"),
            BorderLayout.WEST
        );

        seleccionarCita.add(
            cmbCitas, BorderLayout.CENTER
        );

        formulario.add(
            seleccionarCita, BorderLayout.NORTH
        );

        // CAMPOS DE TEXTO
        JPanel campos =
            new JPanel(new GridLayout(2, 1, 4, 4));

        txtDiagnostico.setLineWrap(true);
        txtDiagnostico.setWrapStyleWord(true);

        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);

        JPanel diagnostico =
            new JPanel(new BorderLayout(3, 3));

        diagnostico.add(
            new JLabel("Diagnostico:"),
            BorderLayout.NORTH
        );

        diagnostico.add(
            new JScrollPane(txtDiagnostico),
            BorderLayout.CENTER
        );

        JPanel notas =
            new JPanel(new BorderLayout(3, 3));

        notas.add(
            new JLabel("Observaciones del doctor:"),
            BorderLayout.NORTH
        );

        notas.add(
            new JScrollPane(txtNotas),
            BorderLayout.CENTER
        );

        campos.add(diagnostico);
        campos.add(notas);

        formulario.add(
            campos, BorderLayout.CENTER
        );

        // BOTON GUARDAR
        JPanel acciones =
            new JPanel(new FlowLayout(FlowLayout.RIGHT));

        acciones.add(btnGuardar);

        formulario.add(
            acciones, BorderLayout.SOUTH
        );

        formulario.setPreferredSize(
            new Dimension(600, 290)
        );

        add(formulario, BorderLayout.SOUTH);

        // ====================================
        // EVENTOS DE LA INTERFAZ
        // ====================================

        btnBuscar.addActionListener(e ->
            consultarHistorialPaciente()
        );

        txtCedula.addActionListener(e ->
            consultarHistorialPaciente()
        );

        btnGuardar.addActionListener(e ->
            guardarDiagnosticoYNotas()
        );

        cmbCitas.addActionListener(e ->
            mostrarDatosDeCitaSeleccionada()
        );

        tblHistorial.getSelectionModel()
            .addListSelectionListener(e -> {

                if (!e.getValueIsAdjusting()
                    && tblHistorial.getSelectedRow() >= 0) {

                    verConsultaSeleccionada();
                }
            });

        lblPaciente.setFont(
            lblPaciente.getFont().deriveFont(
                Font.BOLD
            )
        );
    }

    // ====================================
    // BUSCAR PACIENTE POR CEDULA
    // ====================================
    private void consultarHistorialPaciente() {

        String cedula =
            txtCedula.getText().trim();

        limpiarResultados();

        if (cedula.isEmpty()) {

            JOptionPane.showMessageDialog(
                this, "Ingrese una cedula para buscar."
            );

            return;
        }

        try {

            String[] paciente =
                dao.obtenerPacienteParaHistorial(
                    cedula
                );

            if (paciente == null) {

                JOptionPane.showMessageDialog(
                    this,
                    "No existe un paciente con esa cedula."
                );

                return;
            }

            cedulaBuscada = paciente[0];

            lblCedula.setText(paciente[0]);
            lblPaciente.setText(paciente[1]);
            lblUltimaConsulta.setText(paciente[2]);

            // CONSULTAR LAS CITAS
            List<Object[]> citas =
                dao.listarCitasParaHistorial(
                    cedulaBuscada
                );

            for (Object[] cita : citas) {

                int id =
                    ((Number) cita[0]).intValue();

                String descripcion =
                    "Cita #" + id
                    + " | " + cita[1]
                    + " | " + cita[2]
                    + " | " + cita[3];

                cmbCitas.addItem(
                    new CitaOpcion(id, descripcion)
                );
            }

            cargarTablaHistorial();

            mostrarDatosDeCitaSeleccionada();

            btnGuardar.setEnabled(
                cmbCitas.getItemCount() > 0
            );

            if (citas.isEmpty()) {

                JOptionPane.showMessageDialog(
                    this,
                    "Paciente encontrado sin citas registradas."
                );
            }

        } catch (SQLException ex) {

            limpiarResultados();
            mostrarError(ex);
        }
    }

    // ====================================
    // LLENAR TABLA DE HISTORIAL CLINICO
    // ====================================
    private void cargarTablaHistorial()
            throws SQLException {

        modeloHistorial.setRowCount(0);

        List<Object[]> historial =
            dao.listarHistorialClinicoPorCedula(
                cedulaBuscada
            );

        for (Object[] fila : historial) {
            modeloHistorial.addRow(fila);
        }
    }

    // ====================================
    // MOSTRAR DATOS DE LA CITA SELECCIONADA
    // ====================================
    private void mostrarDatosDeCitaSeleccionada() {

        txtDiagnostico.setText("");
        txtNotas.setText("");

        if (cedulaBuscada == null) {
            return;
        }

        CitaOpcion cita =
            (CitaOpcion) cmbCitas.getSelectedItem();

        if (cita == null) {
            return;
        }

        for (int i = 0;
             i < modeloHistorial.getRowCount(); i++) {

            int id = ((Number)
                modeloHistorial.getValueAt(i, 0)
            ).intValue();

            if (id == cita.id) {

                Object diagnostico =
                    modeloHistorial.getValueAt(i, 3);

                Object notas =
                    modeloHistorial.getValueAt(i, 4);

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

    // ====================================
    // SELECCIONAR CONSULTA DESDE LA TABLA
    // ====================================
    private void verConsultaSeleccionada() {

        int fila = tblHistorial.convertRowIndexToModel(
            tblHistorial.getSelectedRow()
        );

        int idCita =
            ((Number) modeloHistorial.getValueAt(
                fila, 0
            )).intValue();

        for (int i = 0;
             i < cmbCitas.getItemCount(); i++) {

            if (cmbCitas.getItemAt(i).id == idCita) {

                cmbCitas.setSelectedIndex(i);

                mostrarDatosDeCitaSeleccionada();

                break;
            }
        }
    }

    // ====================================
    // GUARDAR DIAGNOSTICO Y NOTAS EN MYSQL
    // ====================================
    private void guardarDiagnosticoYNotas() {

        CitaOpcion cita =
            (CitaOpcion) cmbCitas.getSelectedItem();

        String diagnostico =
            txtDiagnostico.getText().trim();

        String notas =
            txtNotas.getText().trim();

        if (cedulaBuscada == null
            || cita == null) {

            JOptionPane.showMessageDialog(
                this,
                "Primero busque un paciente "
                + "y seleccione una cita."
            );

            return;
        }

        if (diagnostico.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Debe ingresar el diagnostico."
            );

            return;
        }

        int respuesta =
            JOptionPane.showConfirmDialog(
                this,
                "Guardar el diagnostico "
                + "de la cita #" + cita.id + "?",
                "Confirmar guardado",
                JOptionPane.YES_NO_OPTION
            );

        if (respuesta !=
            JOptionPane.YES_OPTION) {

            return;
        }

        try {

            boolean guardado =
                dao.guardarDiagnosticoYNotas(
                    cita.id,
                    cedulaBuscada,
                    diagnostico,
                    notas
                );

            if (guardado) {

                cargarTablaHistorial();

                mostrarDatosDeCitaSeleccionada();

                String[] paciente =
                    dao.obtenerPacienteParaHistorial(
                        cedulaBuscada
                    );

                if (paciente != null) {
                    lblUltimaConsulta.setText(
                        paciente[2]
                    );
                }

                JOptionPane.showMessageDialog(
                    this,
                    "Diagnostico y notas "
                    + "guardados correctamente."
                );

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar. "
                    + "Verifique la cita y cedula."
                );
            }

        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    // ====================================
    // LIMPIAR FORMULARIO
    // ====================================
    private void limpiarResultados() {

        cedulaBuscada = null;

        lblCedula.setText("-");
        lblPaciente.setText("-");
        lblUltimaConsulta.setText("-");

        cmbCitas.removeAllItems();

        modeloHistorial.setRowCount(0);

        txtDiagnostico.setText("");
        txtNotas.setText("");

        btnGuardar.setEnabled(false);
    }

    // ====================================
    // MOSTRAR ERRORES
    // ====================================
    private void mostrarError(SQLException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Error consultando consultorio_db: "
            + ex.getMessage(),
            "Error de base de datos",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // ====================================
    // EJECUTAR PANEL INDEPENDIENTEMENTE
    // ====================================
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame ventana = new JFrame(
                "SmartMed - Historial Clinico - Nando"
            );

            ventana.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
            );

            ventana.setContentPane(
                new PanelHistorialMedico()
            );

            ventana.pack();

            ventana.setLocationRelativeTo(null);

            ventana.setVisible(true);
        });
    }
}
