package views;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Sistema de Diseño Centralizado para SmartMed.
 * Define la paleta de colores clínicos corporativos, tipografías,
 * estilos de tablas, botones, campos y tarjetas para toda la aplicación.
 */
public final class UIClinico {

    // =========================================================================
    // PALETA CLÍNICA INSTITUCIONAL (SMARTMED DESIGN SYSTEM)
    // =========================================================================
    public static final Color COLOR_PRIMARIO      = new Color(15, 76, 129);   // #0F4C81 - Azul médico principal
    public static final Color COLOR_INTERACTIVO   = new Color(2, 132, 199);   // #0284C7 - Azul botones de acción
    public static final Color COLOR_INTERACTIVO_H = new Color(3, 105, 161);   // #0369A1 - Hover azul
    public static final Color COLOR_EXITO         = new Color(16, 185, 129);  // #10B981 - Verde médico confirmación
    public static final Color COLOR_PELIGRO       = new Color(239, 68, 68);   // #EF4444 - Rojo alertas
    public static final Color COLOR_ADVERTENCIA   = new Color(245, 158, 11);  // #F59E0B - Ámbar preventivo
    
    public static final Color COLOR_FONDO         = new Color(248, 250, 252); // #F8FAFC - Gris clínico limpio
    public static final Color COLOR_BLANCO        = Color.WHITE;              // #FFFFFF - Blanco puro
    public static final Color COLOR_BORDE         = new Color(226, 232, 240); // #E2E8F0 - Borde sutil
    public static final Color COLOR_BORDE_INPUT   = new Color(203, 213, 225); // #CBD5E1 - Borde campos
    public static final Color COLOR_SELECCION     = new Color(224, 242, 254); // #E0F2FE - Selección celeste

    public static final Color COLOR_TEXTO_TITULO  = new Color(15, 23, 42);    // #0F172A - Texto oscuro
    public static final Color COLOR_TEXTO_NORMAL  = new Color(30, 41, 59);    // #1E293B - Texto contenido
    public static final Color COLOR_TEXTO_MUTED   = new Color(100, 116, 139); // #64748B - Gris secundario

    // =========================================================================
    // TIPOGRAFÍAS UNIFICADAS
    // =========================================================================
    public static final Font FONT_TITULO_GRANDE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TITULO_MEDIO  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITULO     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_PEQUENA       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CODIGO        = new Font("Consolas", Font.BOLD, 14);

    private UIClinico() {}

    // =========================================================================
    // ESTILIZACIÓN DE BOTONES
    // =========================================================================
    public static void estilizarBotonPrimario(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(COLOR_INTERACTIVO);
        btn.setForeground(COLOR_BLANCO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(COLOR_INTERACTIVO_H, 1, true),
            new EmptyBorder(7, 16, 7, 16)
        ));
    }

    public static void estilizarBotonExito(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(COLOR_EXITO);
        btn.setForeground(COLOR_BLANCO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(new Color(5, 150, 105), 1, true),
            new EmptyBorder(7, 16, 7, 16)
        ));
    }

    public static void estilizarBotonSecundario(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(COLOR_TEXTO_MUTED);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDE, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
    }

    public static void estilizarBotonPeligro(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(COLOR_PELIGRO);
        btn.setForeground(COLOR_BLANCO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 38, 38), 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
    }

    // =========================================================================
    // ESTILIZACIÓN DE CAMPOS DE ENTRADA (TEXTFIELD, PASSWORD, COMBOBOX)
    // =========================================================================
    public static void estilizarCampoTexto(JTextField txt) {
        txt.setFont(FONT_REGULAR);
        txt.setForeground(COLOR_TEXTO_NORMAL);
        txt.setBackground(COLOR_BLANCO);
        txt.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDE_INPUT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void estilizarCombo(JComboBox<?> cb) {
        cb.setFont(FONT_REGULAR);
        cb.setBackground(COLOR_BLANCO);
        cb.setForeground(COLOR_TEXTO_NORMAL);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =========================================================================
    // ESTILIZACIÓN DE TABLAS CLÍNICAS (JTABLE)
    // =========================================================================
    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(241, 245, 249));
        tabla.setSelectionBackground(COLOR_SELECCION);
        tabla.setSelectionForeground(COLOR_TEXTO_TITULO);

        if (tabla.getTableHeader() != null) {
            tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabla.getTableHeader().setBackground(new Color(241, 245, 249));
            tabla.getTableHeader().setForeground(COLOR_TEXTO_NORMAL);
            tabla.getTableHeader().setPreferredSize(new Dimension(0, 32));
            tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        if (tabla.getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }
    }

    // =========================================================================
    // ESTILIZACIÓN DE PANELES Y TARJETAS (CARDS)
    // =========================================================================
    public static void estilizarTarjeta(JPanel panel) {
        panel.setBackground(COLOR_BLANCO);
        panel.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDE, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
    }

    public static void estilizarVentana(JFrame frame) {
        frame.getContentPane().setBackground(COLOR_FONDO);
        frame.setLocationRelativeTo(null);
    }
}

