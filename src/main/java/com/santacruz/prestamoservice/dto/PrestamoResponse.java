package com.santacruz.prestamoservice.dto;

import com.santacruz.prestamoservice.entity.Prestamo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PrestamoResponse {

    private Long id;
    private String cliente;
    private String dni;
    private BigDecimal monto;
    private BigDecimal interes;
    /** Requisito de la guía: monto + interés, ya calculado en la API. */
    private BigDecimal totalACobrar;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private boolean pagado;
    /** Reto complementario: vencido = no pagado y fecha de vencimiento pasada. */
    private boolean vencido;

    public PrestamoResponse() {
    }

    public PrestamoResponse(Prestamo p) {
        this.id = p.getId();
        this.cliente = p.getCliente();
        this.dni = p.getDni();
        this.monto = p.getMonto();
        this.interes = p.getInteres();
        this.totalACobrar = calcularTotal(p.getMonto(), p.getInteres());
        this.fechaPrestamo = p.getFechaPrestamo();
        this.fechaVencimiento = p.getFechaVencimiento();
        this.pagado = p.isPagado();
        this.vencido = !p.isPagado() && p.getFechaVencimiento().isBefore(LocalDate.now());
    }

    public static BigDecimal calcularTotal(BigDecimal monto, BigDecimal interes) {
        if (monto == null || interes == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal ganancia = monto.multiply(interes)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return monto.add(ganancia).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularGanancia(BigDecimal monto, BigDecimal interes) {
        if (monto == null || interes == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return monto.multiply(interes)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }

    public BigDecimal getTotalACobrar() {
        return totalACobrar;
    }

    public void setTotalACobrar(BigDecimal totalACobrar) {
        this.totalACobrar = totalACobrar;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public boolean isVencido() {
        return vencido;
    }

    public void setVencido(boolean vencido) {
        this.vencido = vencido;
    }
}
