/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import dao.CitaDAO;
import model.Especialista;
import java.util.List;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class VistaPaciente extends javax.swing.JFrame {

    private List<Object[]> listaCitasPaciente;

    public VistaPaciente() {
        initComponents();
        this.setLocationRelativeTo(null); // Centra la ventana en pantalla
        cargarEspecialistas(); // Llena el ComboBox de médicos
        jtxtFecha.setEditable(false); // Evita que escriban manualmente la fecha
        
        // Cargar citas automáticamente al presionar Enter o al salir del campo cédula
        jtxtCedula.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                cargarCitasEnDesplegable();
            }
        });

        jtxtCedula.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    cargarCitasEnDesplegable();
                }
            }
        });
    }

    private void cargarEspecialistas() {
        CitaDAO dao = new CitaDAO();
        List<Especialista> lista = dao.listarEspecialistas();

        jcbxEspecialista.removeAllItems();
        for (Especialista esp : lista) {
            jcbxEspecialista.addItem(esp.getNombreCompleto() + " - " + esp.getEspecialidad());
        }
    }

    // Selector de Fecha y Hora (6:00 AM a 8:00 PM cada 30 min, años limpios y bisiestos)
    private void abrirSelectorFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();

        javax.swing.JComboBox<String> cbAnio = new javax.swing.JComboBox<>();
        int anioActual = ahora.getYear();
        for (int i = anioActual; i <= anioActual + 5; i++) {
            cbAnio.addItem(String.valueOf(i));
        }

        String[] mesesNombres = {"01 - Enero", "02 - Febrero", "03 - Marzo", "04 - Abril", 
                                 "05 - Mayo", "06 - Junio", "07 - Julio", "08 - Agosto", 
                                 "09 - Septiembre", "10 - Octubre", "11 - Noviembre", "12 - Diciembre"};
        javax.swing.JComboBox<String> cbMes = new javax.swing.JComboBox<>(mesesNombres);
        cbMes.setSelectedIndex(ahora.getMonthValue() - 1);

        javax.swing.JComboBox<String> cbDia = new javax.swing.JComboBox<>();

        // Horario de 6:00 a 20:00 cada 30 minutos
        javax.swing.JComboBox<String> cbHorarios = new javax.swing.JComboBox<>();
        for (int hora = 6; hora <= 20; hora++) {
            cbHorarios.addItem(String.format("%02d:00", hora));
            if (hora < 20) {
                cbHorarios.addItem(String.format("%02d:30", hora));
            }
        }

        Runnable actualizarDias = () -> {
            int anioSel = Integer.parseInt((String) cbAnio.getSelectedItem());
            int mesSel = cbMes.getSelectedIndex() + 1;
            
            YearMonth yearMonth = YearMonth.of(anioSel, mesSel);
            int diasEnMes = yearMonth.lengthOfMonth();

            int diaSelPrevio = cbDia.getSelectedIndex() + 1;
            cbDia.removeAllItems();

            for (int d = 1; d <= diasEnMes; d++) {
                cbDia.addItem(String.format("%02d", d));
            }

            if (diaSelPrevio > 0 && diaSelPrevio <= diasEnMes) {
                cbDia.setSelectedIndex(diaSelPrevio - 1);
            } else {
                cbDia.setSelectedIndex(0);
            }
        };

        cbAnio.addActionListener(e -> actualizarDias.run());
        cbMes.addActionListener(e -> actualizarDias.run());
        actualizarDias.run();

        javax.swing.JPanel panelPrincipal = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("SELECCIONE FECHA Y HORA DE LA CITA", javax.swing.SwingConstants.CENTER);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblTitulo.setForeground(new java.awt.Color(0, 102, 204));
        panelPrincipal.add(lblTitulo, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel panelContenido = new javax.swing.JPanel(new java.awt.GridLayout(4, 2, 10, 12));
        panelContenido.add(new javax.swing.JLabel("Año:"));
        panelContenido.add(cbAnio);
        panelContenido.add(new javax.swing.JLabel("Mes:"));
        panelContenido.add(cbMes);
        panelContenido.add(new javax.swing.JLabel("Día:"));
        panelContenido.add(cbDia);
        panelContenido.add(new javax.swing.JLabel("Horario (6:00 - 20:00):"));
        panelContenido.add(cbHorarios);

        panelPrincipal.add(panelContenido, java.awt.BorderLayout.CENTER);

        int resultado = JOptionPane.showConfirmDialog(
            this, panelPrincipal, "Agenda Médica", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            String anio = (String) cbAnio.getSelectedItem();
            String mes = String.format("%02d", cbMes.getSelectedIndex() + 1);
            String dia = (String) cbDia.getSelectedItem();
            String horaMinuto = (String) cbHorarios.getSelectedItem();

            String fechaHoraFormateada = anio + "-" + mes + "-" + dia + " " + horaMinuto + ":00";
            jtxtFecha.setText(fechaHoraFormateada);
        }
    }

    // Carga TODAS las citas históricas del paciente en el ComboBox
    private void cargarCitasEnDesplegable() {
        String cedula = jtxtCedula.getText().trim();
        if (cedula.isEmpty()) {
            return;
        }

        CitaDAO dao = new CitaDAO();
        listaCitasPaciente = dao.listarCitasPorCedula(cedula);

        jcbxMisCitas.removeAllItems();
        if (listaCitasPaciente == null || listaCitasPaciente.isEmpty()) {
            jcbxMisCitas.addItem("No hay citas registradas");
        } else {
            for (Object[] fila : listaCitasPaciente) {
                String itemTexto = fila[0] + " | " + fila[1] + " [" + fila[3] + "]";
                jcbxMisCitas.addItem(itemTexto);
            }
        }
    }

    // Abre el ticket de la cita seleccionada en el desplegable
    private void verTicketSeleccionado() {
        int index = jcbxMisCitas.getSelectedIndex();
        if (listaCitasPaciente == null || listaCitasPaciente.isEmpty() || index < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una cita válida de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] citaElegida = listaCitasPaciente.get(index);
        String fecha = citaElegida[0].toString();
        String medico = citaElegida[1].toString();
        String codigoBarras = citaElegida[2].toString(); 
        String nombrePaciente = jtxtNombre.getText().trim();
        if (nombrePaciente.isEmpty()) {
            nombrePaciente = "Paciente";
        }

        TicketDigita ticket = new TicketDigita(nombrePaciente, medico, fecha, codigoBarras);
        ticket.setVisible(true);
    }

    // Guardar nueva cita y actualizar el desplegable automáticamente
    private void guardarCitaRegistrada() {
        String cedula = jtxtCedula.getText().trim();
        String nombre = jtxtNombre.getText().trim();
        String telefono = jtxtTelefono.getText().trim();
        String fechaHora = jtxtFecha.getText().trim();

        if (cedula.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || fechaHora.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos y seleccione la fecha/hora.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int indexSeleccionado = jcbxEspecialista.getSelectedIndex();
        if (indexSeleccionado < 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un especialista.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CitaDAO dao = new CitaDAO();
        List<Especialista> listaEspecialistas = dao.listarEspecialistas();
        
        if (indexSeleccionado >= listaEspecialistas.size()) {
            JOptionPane.showMessageDialog(this, "Error al identificar al especialista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Especialista espSeleccionado = listaEspecialistas.get(indexSeleccionado);
        int idEspecialistaReal = espSeleccionado.getIdEspecialista();
        String nombreMedicoTexto = espSeleccionado.getNombreCompleto() + " - " + espSeleccionado.getEspecialidad();

        String codigoBarras = "CITA-" + cedula + "-" + (System.currentTimeMillis() % 10000);

        boolean exito = dao.guardarCita(cedula, nombre, "Sin Apellido", telefono, idEspecialistaReal, fechaHora, codigoBarras);

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Cita agendada con éxito!\nCódigo de ticket: " + codigoBarras, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            cargarCitasEnDesplegable();

            TicketDigita ticket = new TicketDigita(nombre, nombreMedicoTexto, fechaHora, codigoBarras);
            ticket.setVisible(true);
            
            jtxtFecha.setText("");
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

        jbtnVerTicket = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jtxtCedula = new javax.swing.JTextField();
        jtxtNombre = new javax.swing.JTextField();
        jtxtTelefono = new javax.swing.JTextField();
        jcbxEspecialista = new javax.swing.JComboBox<>();
        jtxtFecha = new javax.swing.JTextField();
        jtbnGuardar = new javax.swing.JButton();
        jtbnVolver = new javax.swing.JButton();
        jbtnElegirF = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jcbxMisCitas = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SmartMed - Agendamiento");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 17)); // NOI18N
        jLabel1.setText("REGISTRO DE NUEVA CITA MÉDICA");

        jLabel2.setText("Cedula");

        jLabel3.setText("Nombre");

        jLabel4.setText("Telefono");

        jLabel5.setText("Especialista");

        jLabel6.setText("Fecha y Hora");

        jcbxEspecialista.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jtbnGuardar.setText("Guardar Cita ");
        jtbnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtbnGuardarActionPerformed(evt);
            }
        });

        jtbnVolver.setText("Volver al menu");
        jtbnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtbnVolverActionPerformed(evt);
            }
        });

        jbtnElegirF.setText("Elegir Fecha");
        jbtnElegirF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnElegirFActionPerformed(evt);
            }
        });

        jButton1.setText("Ver Ticket Selecionado");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel7.setText("Ver Historial de mis Citas Medicas ");

        jcbxMisCitas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jbtnVerTicketLayout = new javax.swing.GroupLayout(jbtnVerTicket);
        jbtnVerTicket.setLayout(jbtnVerTicketLayout);
        jbtnVerTicketLayout.setHorizontalGroup(
            jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jbtnElegirF)
                    .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                            .addGap(64, 64, 64)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                            .addGap(36, 36, 36)
                            .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                                    .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                                    .addGap(31, 31, 31)
                                    .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jcbxEspecialista, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtFecha)
                                        .addComponent(jtxtNombre)
                                        .addComponent(jtxtTelefono)
                                        .addComponent(jtxtCedula)))
                                .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                                    .addComponent(jtbnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                                    .addComponent(jtbnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(jButton1))
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(jcbxMisCitas, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(146, Short.MAX_VALUE))
        );
        jbtnVerTicketLayout.setVerticalGroup(
            jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jbtnVerTicketLayout.createSequentialGroup()
                .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1)
                        .addGap(32, 32, 32)
                        .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jtxtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jtxtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jcbxMisCitas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jtxtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jcbxEspecialista, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jtxtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jbtnElegirF)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE))
                    .addGroup(jbtnVerTicketLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(4, 4, 4)))
                .addGroup(jbtnVerTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtbnGuardar)
                    .addComponent(jtbnVolver))
                .addGap(29, 29, 29))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jbtnVerTicket, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jbtnVerTicket, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jtbnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtbnGuardarActionPerformed
        // TODO add your handling code here:
        guardarCitaRegistrada();
    }//GEN-LAST:event_jtbnGuardarActionPerformed

    private void jtbnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtbnVolverActionPerformed
        // TODO add your handling code here:
        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jtbnVolverActionPerformed

    private void jbtnElegirFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnElegirFActionPerformed
        // TODO add your handling code here:
        abrirSelectorFechaHora();
    }//GEN-LAST:event_jbtnElegirFActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        verTicketSeleccionado();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(VistaPaciente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VistaPaciente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VistaPaciente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VistaPaciente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VistaPaciente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JButton jbtnElegirF;
    private javax.swing.JPanel jbtnVerTicket;
    private javax.swing.JComboBox<String> jcbxEspecialista;
    private javax.swing.JComboBox<String> jcbxMisCitas;
    private javax.swing.JButton jtbnGuardar;
    private javax.swing.JButton jtbnVolver;
    private javax.swing.JTextField jtxtCedula;
    private javax.swing.JTextField jtxtFecha;
    private javax.swing.JTextField jtxtNombre;
    private javax.swing.JTextField jtxtTelefono;
    // End of variables declaration//GEN-END:variables
}
