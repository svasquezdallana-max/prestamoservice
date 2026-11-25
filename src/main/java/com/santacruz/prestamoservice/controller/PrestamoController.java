package com.santacruz.prestamoservice.controller;

import com.santacruz.prestamoservice.dto.PrestamoRequest;
import com.santacruz.prestamoservice.dto.PrestamoResponse;
import com.santacruz.prestamoservice.dto.ResumenResponse;
import com.santacruz.prestamoservice.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /** PROTEGIDO: el email viene del token (authentication.getName()). */
    @GetMapping
    public List<PrestamoResponse> listar(Authentication authentication) {
        return prestamoService.listar(authentication.getName());
    }

    /** Reto complementario. Declarado antes de /{id} para evitar ambigüedad. */
    @GetMapping("/resumen")
    public ResumenResponse resumen(Authentication authentication) {
        return prestamoService.resumen(authentication.getName());
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable Long id, Authentication authentication) {
        return prestamoService.obtener(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoResponse crear(@Valid @RequestBody PrestamoRequest request,
                                  Authentication authentication) {
        return prestamoService.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public PrestamoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody PrestamoRequest request,
                                       Authentication authentication) {
        return prestamoService.actualizar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        prestamoService.eliminar(id, authentication.getName());
    }
}
