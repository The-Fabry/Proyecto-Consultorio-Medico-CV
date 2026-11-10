/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class VistaRecepcion extends javax.swing.JFrame {

    public VistaRecepcion() {
        initComponents();
        this.setLocationRelativeTo(null); // Centrar ventana
        aplicarEstilosClinicos();
        jbtnValidar.addActionListener(evt -> validarAsistenciaTicket()); // Permite validar por clic además del Enter del escáner
        jtxtEscaneo.requestFocus(); // Pone el cursor parpadeando listo para el escáner
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

    // MÉTODO PARA VALIDAR EL CÓDIGO ESCANEADO (COMPATIBILIDAD DUAL: ALFANUMÉRICO Y LÁSER NUMÉRICO)
    private void validarAsistenciaTicket() {
        String codigoLeido = jtxtEscaneo.getText().trim();

        if (codigoLeido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor escanee o ingrese un código.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Filtro numérico para escáneres láser que leen solo dígitos
        String digitosLeidos = codigoLeido.replaceAll("[^0-9]", "");

        ConexionBD conexion = new ConexionBD();
        Connection cn = conexion.conectar();

        try {
            // Búsqueda flexible: Coincidencia exacta o por los dígitos numéricos del código
            String sql = "SELECT c.id_cita, cl.nombre, cl.apellido, cl.cedula, c.fecha_cita, "
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
                int idCita = rs.getInt("id_cita");
                String nombrePaciente = rs.getString("nombre") + " " + rs.getString("apellido");
                String cedula = rs.getString("cedula");
                String fechaCita = rs.getString("fecha_cita");
                String especialista = rs.getString("especialista");
                String estadoActual = rs.getString("estado");
                String codigoOriginal = rs.getString("codigo_barras");

                // Actualizar panel de detalles en pantalla
                jlblCedula.setText("Cédula: " + cedula);
                jlblPacuente.setText("Paciente: " + nombrePaciente);
                jlblCita.setText("Número de Cita: #" + idCita + " (" + codigoOriginal + ")");
                jlblFechaH.setText("Fecha/Hora: " + fechaCita);
                jlblEspecialista.setText("Médico: " + especialista);

                if ("Asistió".equalsIgnoreCase(estadoActual)) {
                    jLabel3.setText("⚠️ Ticket ya registrado previamente.");
                    jLabel3.setForeground(new java.awt.Color(217, 119, 6));
                    JOptionPane.showMessageDialog(this, "⚠️ Este ticket ya fue utilizado anteriormente para:\n" + nombrePaciente, "Aviso de Asistencia", JOptionPane.WARNING_MESSAGE);
                } else {
                    // Actualizar el estado de la cita a "Asistió" usando el ID de la cita
                    String updateSql = "UPDATE citas SET estado = 'Asistió' WHERE id_cita = ?";
                    PreparedStatement pstUpdate = cn.prepareStatement(updateSql);
                    pstUpdate.setInt(1, idCita);
                    pstUpdate.executeUpdate();
                    pstUpdate.close();

                    jLabel3.setText("✅ ¡Asistencia confirmada con éxito!");
                    jLabel3.setForeground(new java.awt.Color(16, 185, 129));
                    JOptionPane.showMessageDialog(this, "✅ ¡Asistencia confirmada con éxito!\nPaciente: " + nombrePaciente + "\nEspecialista: " + especialista, "Bienvenido", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                jLabel3.setText("❌ Código no encontrado en el sistema.");
                jLabel3.setForeground(new java.awt.Color(220, 38, 38));
                JOptionPane.showMessageDialog(this, "❌ El código de barras no existe en el sistema.", "Error de Búsqueda", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            pst.close();
            cn.close();

            // Limpiar campo y reenfocar para el siguiente paciente
            jtxtEscaneo.setText("");
            jtxtEscaneo.requestFocus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al procesar la asistencia: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                .addGap(17, 17, 17)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jbtnVolver)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(129, 129, 129)
                .addComponent(jbtnValidar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(jbtnVolver)
                .addContainerGap())
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
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(JPanel2Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(JPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jlblPacuente, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblCita, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblFechaH, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblEspecialista, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(147, Short.MAX_VALUE))
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
                .addGap(26, 26, 26)
                .addComponent(JPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jtxtEscaneoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtxtEscaneoKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            validarAsistenciaTicket();
        }
    }//GEN-LAST:event_jtxtEscaneoKeyPressed

    private void jbtnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnVolverActionPerformed
        // TODO add your handling code here:
        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jbtnVolverActionPerformed

    private void jtxtEscaneoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtEscaneoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxtEscaneoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VistaRecepcion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VistaRecepcion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VistaRecepcion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VistaRecepcion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
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
