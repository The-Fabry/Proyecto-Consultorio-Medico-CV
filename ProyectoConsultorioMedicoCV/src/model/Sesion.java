/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author User
 */
public class Sesion {
    private static String usuarioLogueado;

    public static String getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public static void setUsuarioLogueado(String usuario) {
        Sesion.usuarioLogueado = usuario;
    }
    
}
