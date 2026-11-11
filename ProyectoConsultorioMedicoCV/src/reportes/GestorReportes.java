package reportes;

import Conexion.ConexionBD;
import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Gestor centralizado de reportes con JasperReports.
 * Optimizado para Java moderno (Java 17 / 21) mediante:
 * 1. Uso de compilación pura en Java (evitando dependencias reflexivas de Groovy).
 * 2. Carga prioritaria de binarios compilados (.jasper) para máxima velocidad.
 * 3. Compilación al vuelo de .jrxml si el binario no existe.
 */
public class GestorReportes {

    private static final String RUTA_REPORTES = "src/reportes/";

    static {
        // Desactiva validaciones estrictas de esquemas XML que fallan en entornos sin internet
        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
        context.setProperty("net.sf.jasperreports.compiler.xml.validation", "false");
        context.setProperty("net.sf.jasperreports.compiler.keep.java.file", "false");
        System.setProperty("net.sf.jasperreports.compiler.xml.validation", "false");

        // En Java 9 / 17 / 21, forzar el uso del compilador javac del sistema en vez del anticuado JDT
        System.setProperty("net.sf.jasperreports.compiler.java", "net.sf.jasperreports.engine.design.JRJavacCompiler");
        context.setProperty("net.sf.jasperreports.compiler.java", "net.sf.jasperreports.engine.design.JRJavacCompiler");
    }

    /**
     * Reporte 1: Agenda General de Citas.
     */
    public static void abrirReporteCitasGeneral(Component parent) {
        ejecutarReporte(parent, "reporte_citas", new HashMap<>());
    }

    /**
     * Reporte 2: Citas por Rango de Fechas.
     */
    public static void abrirReporteCitasPorRango(Component parent, String fechaInicio, String fechaFin) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("FECHA_INICIO", fechaInicio);
        parametros.put("FECHA_FIN", fechaFin);
        ejecutarReporte(parent, "reporte_citas_rango", parametros);
    }

    /**
     * Reporte 3: Historial Clínico de Pacientes.
     * Si cedulaPaciente es null o vacía, muestra todas las historias clínicas.
     */
    public static void abrirHistorialClinico(Component parent, String cedulaPaciente) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("CEDULA_PACIENTE", cedulaPaciente == null ? "" : cedulaPaciente.trim());
        ejecutarReporte(parent, "historial_clinico_paciente", parametros);
    }

    /**
     * Compila y ejecuta el reporte solicitado.
     */
    public static void ejecutarReporte(Component parent, String nombreReporte, Map<String, Object> parametros) {
        ConexionBD conexion = new ConexionBD();
        Connection cn = conexion.conectar();

        if (cn == null) {
            JOptionPane.showMessageDialog(parent, 
                "No se pudo establecer conexión con la base de datos MySQL.", 
                "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            JasperReport reporteCompilado = obtenerReporteCompilado(nombreReporte);

            if (reporteCompilado == null) {
                JOptionPane.showMessageDialog(parent, 
                    "No se encontró el archivo del reporte: " + nombreReporte + ".jrxml", 
                    "Archivo No Encontrado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llenado del reporte con parámetros y conexión SQL
            JasperPrint print = JasperFillManager.fillReport(reporteCompilado, parametros, cn);

            if (print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(parent, 
                    "El reporte no contiene registros para los criterios especificados.", 
                    "Reporte Sin Datos", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Visualizar en ventana independiente sin cerrar el sistema al salir
            JasperViewer visor = new JasperViewer(print, false);
            visor.setTitle("SmartMed - " + nombreReporte.replace("_", " ").toUpperCase());
            visor.setLocationRelativeTo(parent);
            visor.setVisible(true);

        } catch (Exception ex) {
            String mensaje = ex.getMessage();
            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = ex.toString();
            }
            JOptionPane.showMessageDialog(parent, 
                "Error al generar el reporte: " + mensaje + 
                "\n\nSugerencia: Asegúrese de tener compilado el archivo .jasper correspondiente.", 
                "Error JasperReports", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (cn != null && !cn.isClosed()) {
                    cn.close();
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Busca el archivo de reporte en múltiples rutas relativas para soportar ejecuciones
     * tanto desde la raíz del proyecto como desde la carpeta interna de NetBeans.
     */
    private static File buscarArchivoReporte(String nombreReporte, String extension) {
        String[] rutasCandidatas = {
            "src/reportes/" + nombreReporte + extension,
            "ProyectoConsultorioMedicoCV/src/reportes/" + nombreReporte + extension,
            "build/classes/reportes/" + nombreReporte + extension,
            "ProyectoConsultorioMedicoCV/build/classes/reportes/" + nombreReporte + extension
        };
        for (String ruta : rutasCandidatas) {
            File f = new File(ruta);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Carga el reporte compilado (.jasper) o lo compila al vuelo desde el .jrxml.
     */
    private static JasperReport obtenerReporteCompilado(String nombreReporte) throws Exception {
        // 1. Si ya existe el binario .jasper, cargarlo directamente (alta velocidad y evita errores de reflexión)
        File archivoJasper = buscarArchivoReporte(nombreReporte, ".jasper");
        if (archivoJasper != null && archivoJasper.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(archivoJasper);
            } catch (Exception e) {
                System.err.println("Aviso: No se pudo cargar .jasper existente, se recompilará: " + e.getMessage());
            }
        }

        // 2. Compilar desde el archivo fuente .jrxml
        File archivoJrxml = buscarArchivoReporte(nombreReporte, ".jrxml");
        if (archivoJrxml != null && archivoJrxml.exists()) {
            JasperReport compilado = JasperCompileManager.compileReport(archivoJrxml.getAbsolutePath());
            // Guardar automáticamente el .jasper en el mismo directorio del .jrxml para ejecuciones futuras
            try {
                File destinoJasper = new File(archivoJrxml.getParentFile(), nombreReporte + ".jasper");
                JasperCompileManager.compileReportToFile(archivoJrxml.getAbsolutePath(), destinoJasper.getAbsolutePath());
            } catch (Exception ignored) {}
            return compilado;
        }

        // 3. Fallback: buscar dentro del Classpath (JAR / build/classes)
        InputStream stream = GestorReportes.class.getResourceAsStream("/reportes/" + nombreReporte + ".jrxml");
        if (stream != null) {
            return JasperCompileManager.compileReport(stream);
        }

        return null;
    }

    /**
     * Utilidad para precompilar todos los reportes desde la consola o NetBeans.
     */
    public static void main(String[] args) {
        System.out.println("Compilando reportes .jrxml a .jasper...");
        String[] reportes = {"reporte_citas", "reporte_citas_rango", "historial_clinico_paciente"};
        for (String r : reportes) {
            try {
                File jrxml = buscarArchivoReporte(r, ".jrxml");
                if (jrxml != null && jrxml.exists()) {
                    File jasper = new File(jrxml.getParentFile(), r + ".jasper");
                    JasperCompileManager.compileReportToFile(jrxml.getAbsolutePath(), jasper.getAbsolutePath());
                    System.out.println("✅ " + jasper.getName() + " compilado con éxito en: " + jasper.getAbsolutePath());
                } else {
                    System.err.println("❌ No se encontró " + r + ".jrxml");
                }
            } catch (Exception e) {
                System.err.println("Error compilando " + r + ": " + e.getMessage());
            }
        }
        System.out.println("Proceso finalizado.");
    }
}
