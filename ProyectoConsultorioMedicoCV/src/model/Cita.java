/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Cita {
    private int idCita;
    private int idCliente;
    private int idEspecialista;
    private String fechaCita;
    private String codigoBarras;
    private String estado;

    public Cita(int idCita, int idCliente, int idEspecialista, String fechaCita, String codigoBarras, String estado) {
        this.idCita = idCita;
        this.idCliente = idCliente;
        this.idEspecialista = idEspecialista;
        this.fechaCita = fechaCita;
        this.codigoBarras = codigoBarras;
        this.estado = estado;
    }

    public int getIdCita() { return idCita; }
    public int getIdCliente() { return idCliente; }
    public int getIdEspecialista() { return idEspecialista; }
    public String getFechaCita() { return fechaCita; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getEstado() { return estado; }
}
