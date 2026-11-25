package com.santacruz.prestamoservice.dto;

import java.math.BigDecimal;

/** Reto complementario: resumen protegido de la cartera del prestamista. */
public class ResumenResponse {

    private BigDecimal capitalPrestado;
    private BigDecimal totalPorCobrar;
    private BigDecimal gananciaEsperada;
    private long cantidadPrestamos;
    private long cantidadPendientes;

    public ResumenResponse() {
    }

    public ResumenResponse(BigDecimal capitalPrestado, BigDecimal totalPorCobrar,
                           BigDecimal gananciaEsperada, long cantidadPrestamos, long cantidadPendientes) {
        this.capitalPrestado = capitalPrestado;
        this.totalPorCobrar = totalPorCobrar;
        this.gananciaEsperada = gananciaEsperada;
        this.cantidadPrestamos = cantidadPrestamos;
        this.cantidadPendientes = cantidadPendientes;
    }

    public BigDecimal getCapitalPrestado() {
        return capitalPrestado;
    }

    public void setCapitalPrestado(BigDecimal capitalPrestado) {
        this.capitalPrestado = capitalPrestado;
    }

    public BigDecimal getTotalPorCobrar() {
        return totalPorCobrar;
    }

    public void setTotalPorCobrar(BigDecimal totalPorCobrar) {
        this.totalPorCobrar = totalPorCobrar;
    }

    public BigDecimal getGananciaEsperada() {
        return gananciaEsperada;
    }

    public void setGananciaEsperada(BigDecimal gananciaEsperada) {
        this.gananciaEsperada = gananciaEsperada;
    }

    public long getCantidadPrestamos() {
        return cantidadPrestamos;
    }

    public void setCantidadPrestamos(long cantidadPrestamos) {
        this.cantidadPrestamos = cantidadPrestamos;
    }

    public long getCantidadPendientes() {
        return cantidadPendientes;
    }

    public void setCantidadPendientes(long cantidadPendientes) {
        this.cantidadPendientes = cantidadPendientes;
    }
}
