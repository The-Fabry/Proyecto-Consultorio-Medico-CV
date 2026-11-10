/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.awt.datatransfer.StringSelection;
import java.util.EnumMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * Vista de Comprobante / Ticket Digital de Cita Médica.
 * Diseñado con optimización óptica para lectores láser 1D (3nStar SC050)
 * y una interfaz clínica profesional, sobria y balanceada.
 */
public class TicketDigita extends javax.swing.JFrame {

    /**
     * Constructor por defecto (para vista de diseño y previsualización).
     */
    public TicketDigita() {
        initComponents();
        configurarEstilos();
        cargarDatos("Paciente de Muestra", "Dr. Médico General - Consulta", "2026-11-10 10:00", "CITA-1802-3474");
    }

    /**
     * Constructor con parámetros para la emisión real del ticket desde el módulo de citas.
     * 
     * @param paciente Nombre completo del paciente.
     * @param medico Nombre del médico y su especialidad.
     * @param fecha Fecha y hora programada de la cita.
     * @param codigoBarras Código de barras único registrado en la BD (ej. CITA-1802-3474).
     */
    public TicketDigita(String paciente, String medico, String fecha, String codigoBarras) {
        initComponents();
        configurarEstilos();
        cargarDatos(paciente, medico, fecha, codigoBarras);
    }

    /**
     * Aplica configuraciones visuales avanzadas a la ventana y sus componentes.
     */
    private void configurarEstilos() {
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("SmartMed - Comprobante Digital de Cita");
        this.setResizable(false);
        UIClinico.estilizarVentana(this);

        if (jPanel1 != null) {
            UIClinico.estilizarTarjeta(jPanel1);
        }

        jpnlCodigoBarras.setLayout(new BorderLayout());
        jpnlCodigoBarras.setBackground(UIClinico.COLOR_BLANCO);
        jpnlCodigoBarras.setBorder(new CompoundBorder(
            new LineBorder(UIClinico.COLOR_BORDE, 1),
            new EmptyBorder(8, 8, 8, 8)
        ));

        UIClinico.estilizarBotonSecundario(jbtnCerrar);

        jlblPaciente.setFont(UIClinico.FONT_REGULAR);
        jlblPaciente.setForeground(UIClinico.COLOR_TEXTO_NORMAL);

        jlblMedico.setFont(UIClinico.FONT_REGULAR);
        jlblMedico.setForeground(UIClinico.COLOR_TEXTO_NORMAL);

        jlblFecha.setFont(UIClinico.FONT_REGULAR);
        jlblFecha.setForeground(UIClinico.COLOR_TEXTO_NORMAL);

        jLabel1.setFont(UIClinico.FONT_TITULO_GRANDE);
        jLabel1.setForeground(UIClinico.COLOR_PRIMARIO);

        jLabel5.setFont(UIClinico.FONT_PEQUENA);
        jLabel5.setForeground(UIClinico.COLOR_TEXTO_MUTED);

        // Menú contextual de clic derecho para impresión física o copiado
        JPopupMenu menuAcciones = new JPopupMenu();
        JMenuItem itemImprimir = new JMenuItem("Imprimir Comprobante");
        itemImprimir.setFont(UIClinico.FONT_REGULAR);
        itemImprimir.addActionListener(e -> imprimirComprobante());
        JMenuItem itemCopiar = new JMenuItem("Copiar Código de Ticket");
        itemCopiar.setFont(UIClinico.FONT_REGULAR);
        itemCopiar.addActionListener(e -> copiarCodigo());

        menuAcciones.add(itemImprimir);
        menuAcciones.add(itemCopiar);
        jPanel1.setComponentPopupMenu(menuAcciones);
        jpnlCodigoBarras.setComponentPopupMenu(menuAcciones);
    }

    /**
     * Asigna la información del paciente y dispara el renderizado del código de barras.
     */
    public void cargarDatos(String paciente, String medico, String fecha, String codigoBarras) {
        this.codigoActual = (codigoBarras != null ? codigoBarras.trim() : "");
        jlblPaciente.setText("Paciente: " + (paciente != null ? paciente : ""));
        jlblMedico.setText("Médico: " + (medico != null ? medico : ""));
        jlblFecha.setText("Fecha y Hora: " + (fecha != null ? fecha : ""));

        renderizarCodigoBarras(codigoBarras);

        this.pack();
        this.setLocationRelativeTo(null); // Centrar en la pantalla
    }

    /**
     * Genera y dibuja el código de barras en CODE_128 con optimización óptica para láseres 1D:
     * - Generación estricta de barras numéricas (CODE_128 Subset C) que duplica el grosor de cada módulo.
     * - Imagen binaria pura (BufferedImage.TYPE_BYTE_BINARY) para 100% contraste.
     * - Cálculo exacto de Quiet Zones laterales para evitar que el layout de NetBeans recorte los bordes.
     * - Formato legible original completo impreso abajo con tipografía nítida para el usuario.
     */
    private void renderizarCodigoBarras(String codigoBarras) {
        jpnlCodigoBarras.removeAll();

        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            JLabel lblSinCodigo = new JLabel("Código no disponible", SwingConstants.CENTER);
            lblSinCodigo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblSinCodigo.setForeground(new Color(220, 38, 38));
            jpnlCodigoBarras.add(lblSinCodigo, BorderLayout.CENTER);
            jpnlCodigoBarras.revalidate();
            jpnlCodigoBarras.repaint();
            return;
        }

        final String textoLegible = codigoBarras.trim();

        // 💡 OPTIMIZACIÓN LÁSER 1D (3nStar SC050):
        // Extraemos los dígitos numéricos para codificar en CODE_128 C.
        // En CODE_128 C cada símbolo codifica 2 dígitos simultáneos, generando
        // la mitad de barras pero mucho más anchas, facilitando la lectura óptica en monitores LCD.
        String soloNumeros = textoLegible.replaceAll("[^0-9]", "");
        if (soloNumeros.isEmpty()) {
            soloNumeros = "12345678"; // Respaldo numérico si no hay dígitos
        }
        // Asegurar longitud par para CODE_128 C
        if (soloNumeros.length() % 2 != 0) {
            soloNumeros = "0" + soloNumeros;
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0); // Manejo manual milimétrico de la Quiet Zone

            // MultiFormatWriter genera la matriz básica
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                soloNumeros,
                BarcodeFormat.CODE_128,
                1, 
                1, 
                hints
            );

            int matrixWidth = bitMatrix.getWidth(); // Cantidad exacta de módulos

            // Dimensionamiento discreto entero:
            // Al ser numérico (CODE_128 C), matrixWidth es de ~60-80 módulos.
            // Una escala de 3 píxeles por módulo da un código amplio, grueso y ultrasensible.
            int scale = 3;
            int quietZoneModules = 12; // Mínimo 10 módulos según ISO/IEC 15417
            int quietZonePx = quietZoneModules * scale;
            int totalImageWidth = (matrixWidth * scale) + (2 * quietZonePx);

            // Si el ancho excede el espacio disponible (380 px), ajustamos escala a 2
            if (totalImageWidth > 380) {
                scale = 2;
                quietZoneModules = 15;
                quietZonePx = quietZoneModules * scale;
                totalImageWidth = (matrixWidth * scale) + (2 * quietZonePx);
            }

            int barHeight = 70; // Altura generosa de barras para permitir inclinación del escáner
            int totalImageHeight = barHeight + 35; // Barras + texto HRI + espaciado

            // Renderizado en BufferedImage binaria pura (1-bit B/N puro, contraste absoluto)
            BufferedImage barcodeImage = new BufferedImage(totalImageWidth, totalImageHeight, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g2d = barcodeImage.createGraphics();

            // Fondo 100% blanco puro (#FFFFFF)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, totalImageWidth, totalImageHeight);

            // Dibujar las barras con negro puro sin antialiasing (Nearest Neighbor)
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < matrixWidth; i++) {
                if (bitMatrix.get(i, 0)) {
                    int x = quietZonePx + (i * scale);
                    g2d.fillRect(x, 6, scale, barHeight);
                }
            }

            // Dibujar el texto HRI con el formato original legible completo (con guiones: ej. CITA-1802-3474)
            g2d.setFont(UIClinico.FONT_CODIGO);
            g2d.setColor(UIClinico.COLOR_TEXTO_TITULO);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(textoLegible);
            int textX = Math.max(0, (totalImageWidth - textWidth) / 2);
            int textY = barHeight + 6 + fm.getAscent() + 6;
            g2d.drawString(textoLegible, textX, textY);

            g2d.dispose();

            // Insertar la imagen en el panel dentro de un JLabel centrado
            JLabel lblImagenBarras = new JLabel(new ImageIcon(barcodeImage), SwingConstants.CENTER);
            lblImagenBarras.setOpaque(true);
            lblImagenBarras.setBackground(Color.WHITE);
            jpnlCodigoBarras.add(lblImagenBarras, BorderLayout.CENTER);

        } catch (Exception e) {
            JLabel lblFallback = new JLabel("<html><center><b>" + textoLegible + "</b><br><small style='color:red;'>Error al generar código</small></center></html>", SwingConstants.CENTER);
            lblFallback.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            jpnlCodigoBarras.add(lblFallback, BorderLayout.CENTER);
            e.printStackTrace();
        }

        jpnlCodigoBarras.revalidate();
        jpnlCodigoBarras.repaint();
    }

    private String codigoActual = "";

    /**
     * Envía el comprobante digital a la impresora (soporta tanto ticketeras térmicas POS
     * de 58mm/80mm como impresoras de oficina láser/tinta convencionales).
     */
    public void imprimirComprobante() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Ticket SmartMed - " + jlblPaciente.getText());

        job.setPrintable((Graphics pg, PageFormat pf, int pageNum) -> {
            if (pageNum > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) pg;
            g2.translate(pf.getImageableX(), pf.getImageableY());

            // Escala automática para adaptarse al ancho de papel de la impresora
            double panelWidth = jPanel1.getWidth();
            double pageWidth = pf.getImageableWidth();
            double scale = Math.min(1.0, pageWidth / panelWidth);

            g2.scale(scale, scale);
            jPanel1.paint(g2);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir el comprobante: " + ex.getMessage(), "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Copia el código alfanumérico al portapapeles del sistema operativo.
     */
    public void copiarCodigo() {
        if (codigoActual != null && !codigoActual.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(codigoActual), null);
            JOptionPane.showMessageDialog(this, "Código copiado al portapapeles:\n" + codigoActual, "SmartMed", JOptionPane.INFORMATION_MESSAGE);
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
        jlblPaciente = new javax.swing.JLabel();
        jlblMedico = new javax.swing.JLabel();
        jlblFecha = new javax.swing.JLabel();
        jpnlCodigoBarras = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jbtnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SmartMed - Ticket Digital");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("COMPROBANTE DE CITA");

        jlblPaciente.setText("Paciente:");

        jlblMedico.setText("Médico:");

        jlblFecha.setText("Fecha:");

        jpnlCodigoBarras.setBackground(new java.awt.Color(255, 255, 255));
        jpnlCodigoBarras.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        jpnlCodigoBarras.setPreferredSize(new java.awt.Dimension(380, 115));

        javax.swing.GroupLayout jpnlCodigoBarrasLayout = new javax.swing.GroupLayout(jpnlCodigoBarras);
        jpnlCodigoBarras.setLayout(jpnlCodigoBarrasLayout);
        jpnlCodigoBarrasLayout.setHorizontalGroup(
            jpnlCodigoBarrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jpnlCodigoBarrasLayout.setVerticalGroup(
            jpnlCodigoBarrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 113, Short.MAX_VALUE)
        );

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Muestre este código en la recepción al llegar");

        jbtnCerrar.setText("Cerrar");
        jbtnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCerrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(jlblPaciente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jlblMedico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jlblFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpnlCodigoBarras, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jbtnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(155, 155, 155))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jlblPaciente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jlblMedico)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jlblFecha)
                .addGap(18, 18, 18)
                .addComponent(jpnlCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jbtnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCerrarActionPerformed
        this.dispose();
    }//GEN-LAST:event_jbtnCerrarActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TicketDigita.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TicketDigita().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbtnCerrar;
    private javax.swing.JLabel jlblFecha;
    private javax.swing.JLabel jlblMedico;
    private javax.swing.JLabel jlblPaciente;
    private javax.swing.JPanel jpnlCodigoBarras;
    // End of variables declaration//GEN-END:variables
}
