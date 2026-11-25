package com.santacruz.prestamoservice.service;

import com.santacruz.prestamoservice.dto.PrestamoRequest;
import com.santacruz.prestamoservice.dto.PrestamoResponse;
import com.santacruz.prestamoservice.dto.ResumenResponse;
import com.santacruz.prestamoservice.entity.Prestamo;
import com.santacruz.prestamoservice.entity.Usuario;
import com.santacruz.prestamoservice.exception.NoAutorizadoException;
import com.santacruz.prestamoservice.exception.NoEncontradoException;
import com.santacruz.prestamoservice.repository.PrestamoRepository;
import com.santacruz.prestamoservice.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Todas las consultas usan el email del token, nunca un parámetro de la URL. */
    public List<PrestamoResponse> listar(String emailToken) {
        Long usuarioId = obtenerUsuarioId(emailToken);
        return prestamoRepository.findByUsuarioIdOrderByFechaVencimientoAsc(usuarioId)
                .stream()
                .map(PrestamoResponse::new)
                .toList();
    }

    public PrestamoResponse obtener(Long id, String emailToken) {
        Prestamo prestamo = buscarPropio(id, emailToken);
        return new PrestamoResponse(prestamo);
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request, String emailToken) {
        Usuario usuario = obtenerUsuario(emailToken);

        Prestamo prestamo = new Prestamo();
        aplicarDatos(prestamo, request);
        prestamo.setFechaPrestamo(LocalDate.now()); // La API asigna la fecha.
        prestamo.setPagado(request.getPagado() != null && request.getPagado());
        prestamo.setUsuario(usuario); // Dueño tomado del token.

        prestamoRepository.save(prestamo);
        return new PrestamoResponse(prestamo);
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request, String emailToken) {
        Prestamo prestamo = buscarPropio(id, emailToken);

        aplicarDatos(prestamo, request);
        if (request.getPagado() != null) {
            prestamo.setPagado(request.getPagado()); // Permite marcar como cobrado.
        }

        prestamoRepository.save(prestamo);
        return new PrestamoResponse(prestamo);
    }

    @Transactional
    public void eliminar(Long id, String emailToken) {
        Prestamo prestamo = buscarPropio(id, emailToken);
        prestamoRepository.delete(prestamo);
    }

    /** Reto complementario: resumen de la cartera del prestamista del token. */
    public ResumenResponse resumen(String emailToken) {
        List<PrestamoResponse> prestamos = listar(emailToken);

        BigDecimal capital = BigDecimal.ZERO;
        BigDecimal porCobrar = BigDecimal.ZERO;
        BigDecimal ganancia = BigDecimal.ZERO;
        long pendientes = 0;

        for (PrestamoResponse p : prestamos) {
            capital = capital.add(p.getMonto());
            ganancia = ganancia.add(PrestamoResponse.calcularGanancia(p.getMonto(), p.getInteres()));
            if (!p.isPagado()) {
                porCobrar = porCobrar.add(p.getTotalACobrar());
                pendientes++;
            }
        }

        return new ResumenResponse(
                redondear(capital), redondear(porCobrar), redondear(ganancia),
                prestamos.size(), pendientes);
    }

    private Prestamo buscarPropio(Long id, String emailToken) {
        Long usuarioId = obtenerUsuarioId(emailToken);
        // Si el préstamo es de otro prestamista, responde 404: no filtramos su existencia.
        return prestamoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Préstamo no encontrado"));
    }

    private void aplicarDatos(Prestamo prestamo, PrestamoRequest request) {
        prestamo.setCliente(request.getCliente().trim());
        prestamo.setDni(request.getDni().trim());
        prestamo.setMonto(request.getMonto());
        prestamo.setInteres(request.getInteres());
        prestamo.setFechaVencimiento(request.getFechaVencimiento());
    }

    private Usuario obtenerUsuario(String emailToken) {
        return usuarioRepository.findByEmail(emailToken)
                .orElseThrow(() -> new NoAutorizadoException("El usuario del token no existe"));
    }

    private Long obtenerUsuarioId(String emailToken) {
        return obtenerUsuario(emailToken).getId();
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
