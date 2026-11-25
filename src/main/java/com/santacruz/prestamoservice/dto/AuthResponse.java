package com.santacruz.prestamoservice.dto;

public class AuthResponse {

    private String token;
    private String tipo = "Bearer";
    private long expiraEnMilisegundos;
    private String email;
    private String nombre;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiraEnMilisegundos, String email, String nombre) {
        this.token = token;
        this.expiraEnMilisegundos = expiraEnMilisegundos;
        this.email = email;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getExpiraEnMilisegundos() {
        return expiraEnMilisegundos;
    }

    public void setExpiraEnMilisegundos(long expiraEnMilisegundos) {
        this.expiraEnMilisegundos = expiraEnMilisegundos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
