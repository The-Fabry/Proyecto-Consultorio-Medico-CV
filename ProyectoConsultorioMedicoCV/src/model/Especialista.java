/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Especialista {
    private int idEspecialista;
    private String nombreCompleto;
    private String especialidad;

    public Especialista(int idEspecialista, String nombreCompleto, String especialidad) {
        this.idEspecialista = idEspecialista;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
    }

    public int getIdEspecialista() { return idEspecialista; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEspecialidad() { return especialidad; }

    // Esto es vital para que el JComboBox muestre el nombre del médico y no códigos raros
    @Override
    public String toString() {
        return nombreCompleto + " (" + especialidad + ")";
    }
}
