package com.santacruz.prestamoservice.repository;

import com.santacruz.prestamoservice.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** Solo los préstamos del prestamista que viene en el token. */
    List<Prestamo> findByUsuarioIdOrderByFechaVencimientoAsc(Long usuarioId);

    /** El id SOLO se busca dentro de la cartera de ese prestamista. */
    Optional<Prestamo> findByIdAndUsuarioId(Long id, Long usuarioId);
}
