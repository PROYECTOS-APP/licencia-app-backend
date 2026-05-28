package com.licencia.licenciabackendapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "licencias")
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_licencia", unique = true)
    private String codigoLicencia;

    private String producto;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto productoRel;

    private String tipo;
    private LocalDate fechaVencimiento;

    @Column(name = "cantidad_usuarios")
    private Integer cantidadUsuarios;

    @Column(name = "cantidad_licencias")
    private Integer cantidadLicencias = 1;

    @Column(name = "precio_total_usd")
    private Double precioTotalUSD;

    @Column(name = "precio_total_pen")
    private Double precioTotalPEN;

    private String moneda = "USD";
    private String cliente;
    private String empresa;
    private String correo;
    private String notas;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (codigoLicencia == null || codigoLicencia.isEmpty()) {
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            codigoLicencia = "LIC-" + uuid + "-" + System.currentTimeMillis();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoLicencia() { return codigoLicencia; }
    public void setCodigoLicencia(String codigoLicencia) { this.codigoLicencia = codigoLicencia; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public Producto getProductoRel() { return productoRel; }
    public void setProductoRel(Producto productoRel) { this.productoRel = productoRel; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Integer getCantidadUsuarios() { return cantidadUsuarios; }
    public void setCantidadUsuarios(Integer cantidadUsuarios) { this.cantidadUsuarios = cantidadUsuarios; }
    public Integer getCantidadLicencias() { return cantidadLicencias; }
    public void setCantidadLicencias(Integer cantidadLicencias) { this.cantidadLicencias = cantidadLicencias; }
    public Double getPrecioTotalUSD() { return precioTotalUSD; }
    public void setPrecioTotalUSD(Double precioTotalUSD) { this.precioTotalUSD = precioTotalUSD; }
    public Double getPrecioTotalPEN() { return precioTotalPEN; }
    public void setPrecioTotalPEN(Double precioTotalPEN) { this.precioTotalPEN = precioTotalPEN; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}