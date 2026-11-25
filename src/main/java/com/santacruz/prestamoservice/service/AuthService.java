package com.santacruz.prestamoservice.service;

import com.santacruz.prestamoservice.dto.AuthResponse;
import com.santacruz.prestamoservice.dto.LoginRequest;
import com.santacruz.prestamoservice.dto.RegistroRequest;
import com.santacruz.prestamoservice.entity.Usuario;
import com.santacruz.prestamoservice.exception.ConflictoException;
import com.santacruz.prestamoservice.exception.NoAutorizadoException;
import com.santacruz.prestamoservice.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Registro público: guarda contraseña cifrada y devuelve un token ya listo para usar. */
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizar(request.getEmail());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe una cuenta con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol("USER");

        usuarioRepository.save(usuario);
        return construirRespuesta(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizar(request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoAutorizadoException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new NoAutorizadoException("Email o contraseña incorrectos");
        }

        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario usuario) {
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, jwtService.getExpiracionMs(),
                usuario.getEmail(), usuario.getNombre());
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
