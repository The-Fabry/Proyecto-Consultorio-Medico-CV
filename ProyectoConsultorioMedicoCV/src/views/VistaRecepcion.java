/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import Conexion.ConexionBD;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 * Módulo de Recepción y Control de Llegada de Pacientes.
 * Gestiona el escaneo láser automático, búsqueda manual y el cambio de estado
 * de la cita hacia 'En Proceso' mediante confirmación explícita del recepcionista.
 */
public class VistaRecepcion extends javax.swing.JFrame {

    // Variables de control de la cita actualmente cargada en pantalla
    private int idCitaCargada = -1;
    private String estadoCitaCargada = "";
    private String codigoCitaCargada = "";
    private boolean busquedaEnProceso = false;

    public VistaRecepcion() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar ventana
        aplicarEstilosClinicos();
        agregarMenuReportes();
        limpiarFichaDetalle();
        jtxtEscaneo.requestFocus(); // Pone el cursor parpadeando listo para el escáner
    }

    private void agregarMenuReportes() {
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu menuReportes = new javax.swing.JMenu("📊 Reportes");
        menuReportes.setFont(UIClinico.FONT_SUBTITULO);

        javax.swing.JMenuItem itemGeneral = new javax.swing.JMenuItem("📅 Reporte General de Citas");
        itemGeneral.setFont(UIClinico.FONT_REGULAR);
        itemGeneral.addActionListener(e -> reportes.GestorReportes.abrirReporteCitasGeneral(this));

        javax.swing.JMenuItem itemRango = new javax.swing.JMenuItem("⏱️ Reporte de Citas por Rango de Fechas");
        itemRango.setFont(UIClinico.FONT_REGULAR);
        itemRango.addActionListener(e -> solicitarRangoFechasYReportar());

        menuReportes.add(itemGeneral);
        menuReportes.add(itemRango);
        menuBar.add(menuReportes);
        this.setJMenuBar(menuBar);
    }

    private void solicitarRangoFechasYReportar() {
        javax.swing.JTextField txtInicio = new javax.swing.JTextField(java.time.LocalDate.now().toString() + " 00:00:00");
        javax.swing.JTextField txtFin = new javax.swing.JTextField(java.time.LocalDate.now().plusMonths(1).toString() + " 23:59:59");

        Object[] formulario = {
            "Fecha Inicio (YYYY-MM-DD HH:mm:ss):", txtInicio,
            "Fecha Fin (YYYY-MM-DD HH:mm:ss):", txtFin
        };

        int res = JOptionPane.showConfirmDialog(this, formulario, "Filtro de Reporte por Fechas", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String ini = txtInicio.getText().trim();
            String fin = txtFin.getText().trim();
            if (!ini.isEmpty() && !fin.isEmpty()) {
                reportes.GestorReportes.abrirReporteCitasPorRango(this, ini, fin);
            }
        }
    }

    private void aplicarEstilosClinicos() {
        this.setTitle("SmartMed - Recepción y Control de Acceso");
        UIClinico.estilizarVentana(this);
        UIClinico.estilizarTarjeta(jPanel1);
        UIClinico.estilizarTarjeta(JPanel2);

        jLabel1.setFont(UIClinico.FONT_TITULO_GRANDE);
        jLabel1.setForeground(UIClinico.COLOR_PRIMARIO);

        jLabel4.setFont(UIClinico.FONT_TITULO_MEDIO);
        jLabel4.setForeground(UIClinico.COLOR_PRIMARIO);

        jtxtEscaneo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        jtxtEscaneo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtEscaneo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(147, 197, 253), 2, true),
            new javax.swing.border.EmptyBorder(4, 8, 4, 8)
        ));

        UIClinico.estilizarBotonPrimario(jbtnValidar);
        UIClinico.estilizarBotonExito(jbtnIngresar);
        UIClinico.estilizarBotonSecundario(jbtnVolver);

        java.awt.Font fuenteDetalle = UIClinico.FONT_REGULAR;
        jlblCedula.setFont(fuenteDetalle);
        jlblPacuente.setFont(fuenteDetalle);
        jlblCita.setFont(fuenteDetalle);
        jlblFechaH.setFont(fuenteDetalle);
        jlblEspecialista.setFont(fuenteDetalle);
    }

    /**
     * Limpia la ficha de datos del paciente en pantalla.
     */
    private void limpiarFichaDetalle() {
        this.idCitaCargada = -1;
        this.estadoCitaCargada = "";
        this.codigoCitaCargada = "";

        jlblCedula.setText("Cédula: -");
        jlblPacuente.setText("Paciente: -");
        jlblCita.setText("Número de Cita: -");
        jlblFechaH.setText("Fecha/Hora: -");
        jlblEspecialista.setText("Médico: -");
        jbtnIngresar.setEnabled(false);
    }

    /**
     * REQUERIMIENTO 3: Procesa la lectura del escáner láser de forma limpia.
     * Si el lector envía Enter y el campo ya fue procesado o está vacío, no muestra alertas falsas.
     */
    public void procesarLecturaLaser() {
        if (busquedaEnProceso) {
            return;
        }
        String codigoLeido = jtxtEscaneo.getText().trim();
        if (codigoLeido.isEmpty()) {
            return; // No lanza advertencia si el escáner envía Enter con campo vacío
        }
        ejecutarBusquedaYMostrar(false);
    }

    /**
     * REQUERIMIENTO 3: Búsqueda manual al presionar 'Validar Ticket'.
     * Solo si el usuario hace clic con el campo vacío se muestra la advertencia.
     */
    public void validarTicketManual() {
        if (busquedaEnProceso) {
            return;
        }
        String codigoLeido = jtxtEscaneo.getText().trim();
        if (codigoLeido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor escanee con el lector o ingrese un código de ticket.", "Aviso", JOptionPane.WARNING_MESSAGE);
            jtxtEscaneo.requestFocus();
            return;
        }
        ejecutarBusquedaYMostrar(true);
    }

    /**
     * Método público para compatibilidad externa.
     */
    public void buscarYMostrarTicket() {
        validarTicketManual();
    }

    /**
     * Realiza la consulta SQL y el rellenado de campos sin cambiar el estado.
     */
    private void ejecutarBusquedaYMostrar(boolean esManual) {
        if (busquedaEnProceso) {
            return;
        }

        String codigoLeido = jtxtEscaneo.getText().trim();
        if (codigoLeido.isEmpty()) {
            if (esManual) {
                JOptionPane.showMessageDialog(this, "Por favor escanee con el lector o ingrese un código de ticket.", "Aviso", JOptionPane.WARNING_MESSAGE);
                jtxtEscaneo.requestFocus();
            }
            return;
        }

        busquedaEnProceso = true;

        // Filtro numérico de apoyo para escáneres láser que leen solo dígitos
        String digitosLeidos = codigoLeido.replaceAll("[^0-9]", "");

        ConexionBD conexion = new ConexionBD();
        Connection cn = conexion.conectar();

        try {
            // Consulta SQL optimizada para búsqueda flexible (código completo o solo números)
            String sql = "SELECT c.id_cita, cl.nombre AS nombre_paciente, cl.cedula, c.fecha_cita, "
                    + "CONCAT(e.nombre_completo, ' - ', e.especialidad) AS especialista, c.estado, c.codigo_barras "
                    + "FROM citas c "
                    + "JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                    + "JOIN especialistas e ON c.id_especialista = e.id_especialista "
                    + "WHERE c.codigo_barras = ? "
                    + "   OR REPLACE(REPLACE(c.codigo_barras, 'CITA-', ''), '-', '') = ? "
                    + "   OR c.codigo_barras LIKE ? LIMIT 1";

            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, codigoLeido);
            pst.setString(2, digitosLeidos.isEmpty() ? codigoLeido : digitosLeidos);
            pst.setString(3, "%" + (digitosLeidos.isEmpty() ? codigoLeido : digitosLeidos) + "%");
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                this.idCitaCargada = rs.getInt("id_cita");
                this.estadoCitaCargada = rs.getString("estado");
                this.codigoCitaCargada = rs.getString("codigo_barras");

                String nombrePaciente = rs.getString("nombre_paciente");
                String cedula = rs.getString("cedula");
                String fechaCita = rs.getString("fecha_cita");
                String especialista = rs.getString("especialista");

                // Rellenar automáticamente los datos informativos en pantalla
                jlblCedula.setText("Cédula: " + cedula);
                jlblPacuente.setText("Paciente: " + nombrePaciente);
                jlblCita.setText("Número de Cita: #" + this.idCitaCargada + " (" + this.codigoCitaCargada + ")");
                jlblFechaH.setText("Fecha/Hora: " + fechaCita);
                jlblEspecialista.setText("Médico: " + especialista);

                // Validaciones y advertencias según el estado actual de la cita
                if ("Finalizada".equalsIgnoreCase(this.estadoCitaCargada) || "Finalizado".equalsIgnoreCase(this.estadoCitaCargada)) {
                    jbtnIngresar.setEnabled(false); // Bloquea la operación
                    jLabel3.setText("❌ Cita Finalizada - Atendida previamente.");
                    jLabel3.setForeground(UIClinico.COLOR_PELIGRO);
                    JOptionPane.showMessageDialog(this, 
                        "❌ Cita Finalizada - Este ticket ya fue atendido por el médico", 
                        "Cita Finalizada", 
                        JOptionPane.ERROR_MESSAGE);

                } else if ("En Proceso".equalsIgnoreCase(this.estadoCitaCargada) || "Asistió".equalsIgnoreCase(this.estadoCitaCargada)) {
                    jbtnIngresar.setEnabled(false); // Ya se encuentra en proceso
                    jLabel3.setText("⚠️ El ticket ya se encuentra en proceso de atención.");
                    jLabel3.setForeground(UIClinico.COLOR_ADVERTENCIA);
                    JOptionPane.showMessageDialog(this, 
                        "⚠️ El ticket ya se encuentra en proceso de atención", 
                        "Aviso", 
                        JOptionPane.WARNING_MESSAGE);

                } else {
                    // Estado 'Pendiente': Permite consultar cuantas veces sea necesario y habilita el botón de ingreso
                    jbtnIngresar.setEnabled(true);
                    jLabel3.setText("ℹ️ Ticket válido. Presione 'Ingresar a la Cita' para registrar llegada.");
                    jLabel3.setForeground(UIClinico.COLOR_PRIMARIO);
                }

            } else {
                limpiarFichaDetalle();
                jLabel3.setText("❌ Código de ticket no encontrado en el sistema.");
                jLabel3.setForeground(UIClinico.COLOR_PELIGRO);
                JOptionPane.showMessageDialog(this, "❌ El código de barras no existe en el sistema.", "Error de Búsqueda", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            pst.close();
            cn.close();

            // Limpiar campo de texto y reenfocar para el siguiente escaneo
            jtxtEscaneo.setText("");
            jtxtEscaneo.requestFocus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al consultar la cita: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            busquedaEnProceso = false;
        }
    }

    /**
     * REGLA 2: El cambio de estado a 'En Proceso' ocurre ÚNICA Y EXCLUSIVAMENTE
     * cuando el recepcionista hace clic en el botón 'Ingresar a la Cita'.
     */
    public void ingresarPacienteACita() {
        if (this.idCitaCargada <= 0) {
            JOptionPane.showMessageDialog(this, "Primero debe escanear o validar un ticket de cita.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Finalizada".equalsIgnoreCase(this.estadoCitaCargada) || "Finalizado".equalsIgnoreCase(this.estadoCitaCargada)) {
            JOptionPane.showMessageDialog(this, "❌ Cita Finalizada - Este ticket ya fue atendido por el médico.", "Operación Bloqueada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("En Proceso".equalsIgnoreCase(this.estadoCitaCargada)) {
            JOptionPane.showMessageDialog(this, "⚠️ El ticket ya se encuentra en proceso de atención", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ejecutar la actualización en MySQL de 'Pendiente' a 'En Proceso'
        ConexionBD conexion = new ConexionBD();
        Connection cn = conexion.conectar();

        try {
            String updateSql = "UPDATE citas SET estado = 'En Proceso' WHERE id_cita = ?";
            PreparedStatement pstUpdate = cn.prepareStatement(updateSql);
            pstUpdate.setInt(1, this.idCitaCargada);
            int filasModificadas = pstUpdate.executeUpdate();
            pstUpdate.close();
            cn.close();

            if (filasModificadas > 0) {
                this.estadoCitaCargada = "En Proceso";
                jbtnIngresar.setEnabled(false);
                jLabel3.setText("✅ ¡Llegada confirmada! Estado actualizado a: EN PROCESO");
                jLabel3.setForeground(UIClinico.COLOR_EXITO);

                JOptionPane.showMessageDialog(this, 
                    "✅ ¡Paciente ingresado a la cita con éxito!\nEstado actualizado a: EN PROCESO\nEl médico ya puede visualizarlo en su agenda.", 
                    "Llegada Confirmada", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el estado de la cita.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la llegada del paciente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jtxtEscaneo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jbtnVolver = new javax.swing.JButton();
        jbtnValidar = new javax.swing.JButton();
        JPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jlblCedula = new javax.swing.JLabel();
        jlblPacuente = new javax.swing.JLabel();
        jlblCita = new javax.swing.JLabel();
        jlblFechaH = new javax.swing.JLabel();
        jlblEspecialista = new javax.swing.JLabel();
        jbtnIngresar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SmartMed - Recepcion");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("CONTROL DE LLEGADA A RECEPCIÓN");

        jLabel2.setText("Por favor, escanee el código del ticket:");

        jtxtEscaneo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtEscaneoActionPerformed(evt);
            }
        });
        jtxtEscaneo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtxtEscaneoKeyPressed(evt);
            }
        });

        jLabel3.setText("Esperando lectura de código...");

        jbtnVolver.setText("Volver al Menu");
        jbtnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnVolverActionPerformed(evt);
            }
        });

        jbtnValidar.setText("Validar Ticket");
        jbtnValidar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnValidarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jtxtEscaneo, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(90, 90, 90))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(129, 129, 129)
                .addComponent(jbtnValidar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jbtnVolver)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jtxtEscaneo, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jbtnValidar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jbtnVolver)
                .addGap(19, 19, 19))
        );

        JPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel4.setText("Detalle de la Cita");

        jlblCedula.setText("Cedula:");

        jlblPacuente.setText("Paciente: ");

        jlblCita.setText("Numero de Cita:");

        jlblFechaH.setText("Fecha/Hora:");

        jlblEspecialista.setText("Especialista:");

        jbtnIngresar.setText("Ingresar a la Cita");
        jbtnIngresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnIngresarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout JPanel2Layout = new javax.swing.GroupLayout(JPanel2);
        JPanel2.setLayout(JPanel2Layout);
        JPanel2Layout.setHorizontalGroup(
            JPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanel2Layout.createSequentialGroup()
                .addGroup(JPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(JPanel2Layout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addComponent(jbtnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(JPanel2Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(147, Short.MAX_VALUE))
            .addGroup(JPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(JPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jlblEspecialista, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblFechaH, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblCita, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblPacuente, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        JPanel2Layout.setVerticalGroup(
            JPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jlblCedula)
                .addGap(18, 18, 18)
                .addComponent(jlblPacuente)
                .addGap(18, 18, 18)
                .addComponent(jlblCita)
                .addGap(18, 18, 18)
                .addComponent(jlblFechaH)
                .addGap(18, 18, 18)
                .addComponent(jlblEspecialista)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jbtnIngresar)
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(JPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 71, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jtxtEscaneoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtxtEscaneoKeyPressed
        // El lector láser envía automáticamente Enter al final del escaneo
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            procesarLecturaLaser();
        }
    }//GEN-LAST:event_jtxtEscaneoKeyPressed

    private void jbtnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnVolverActionPerformed
        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jbtnVolverActionPerformed

    private void jtxtEscaneoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtEscaneoActionPerformed
        procesarLecturaLaser();
    }//GEN-LAST:event_jtxtEscaneoActionPerformed

    private void jbtnValidarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnValidarActionPerformed
        // Búsqueda manual al presionar el botón "Validar Ticket"
        validarTicketManual();
    }//GEN-LAST:event_jbtnValidarActionPerformed

    private void jbtnIngresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnIngresarActionPerformed
        // Acción explícita para cambiar el estado de 'Pendiente' a 'En Proceso'
        ingresarPacienteACita();
    }//GEN-LAST:event_jbtnIngresarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VistaRecepcion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VistaRecepcion().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JPanel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbtnIngresar;
    private javax.swing.JButton jbtnValidar;
    private javax.swing.JButton jbtnVolver;
    private javax.swing.JLabel jlblCedula;
    private javax.swing.JLabel jlblCita;
    private javax.swing.JLabel jlblEspecialista;
    private javax.swing.JLabel jlblFechaH;
    private javax.swing.JLabel jlblPacuente;
    private javax.swing.JTextField jtxtEscaneo;
    // End of variables declaration//GEN-END:variables
}
