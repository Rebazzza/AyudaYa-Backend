package com.donaciones.service;

import com.donaciones.dto.request.LoginRequest;
import com.donaciones.dto.response.UsuarioResponse;
import com.donaciones.entity.Rol;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.InvalidCredentialsException;
import com.donaciones.repository.RolRepository;
import com.donaciones.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de autenticación - Flujo de Login")
class   AuthServiceTest {

    private static final String CORREO = "maria.garcia@example.com";
    private static final String CONTRASENA = "Password123";
    private static final String CONTRASENA_ENCRIPTADA = "$2a$10$hashSimuladoNoUtilizable";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Retorna los datos del usuario cuando correo y contraseña coinciden")
    void login_conCredencialesValidas_retornaUsuarioAutenticado() {
        Usuario usuario = usuarioPersistido();

        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(CONTRASENA, CONTRASENA_ENCRIPTADA)).thenReturn(true);

        UsuarioResponse response = authService.login(credenciales(CORREO, CONTRASENA));

        assertThat(response.getIdUsuario()).isEqualTo(7L);
        assertThat(response.getDniUsuario()).isEqualTo("87654321");
        assertThat(response.getNombreUsuario()).isEqualTo("María");
        assertThat(response.getApellidosUsuario()).isEqualTo("García López");
        assertThat(response.getCorreoUsuario()).isEqualTo(CORREO);
        assertThat(response.getTelefonoUsuario()).isEqualTo("987654321");
        assertThat(response.getNombreRol()).isEqualTo("DONANTE");
        assertThat(response.getFechaRegistro()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 30));
    }

    @Test
    @DisplayName("Valida la contraseña contra el hash almacenado y nunca escribe en la base de datos")
    void login_conCredencialesValidas_verificaHashYNoPersisteDatos() {
        Usuario usuario = usuarioPersistido();

        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(CONTRASENA, CONTRASENA_ENCRIPTADA)).thenReturn(true);

        authService.login(credenciales(CORREO, CONTRASENA));

        verify(passwordEncoder).matches(CONTRASENA, CONTRASENA_ENCRIPTADA);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(rolRepository);
    }

    @Test
    @DisplayName("Rechaza el login cuando el correo no está registrado")
    void login_conCorreoInexistente_lanzaInvalidCredentialsException() {
        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(credenciales(CORREO, CONTRASENA)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales incorrectas");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Rechaza el login cuando la contraseña no corresponde al usuario")
    void login_conContrasenaIncorrecta_lanzaInvalidCredentialsException() {
        Usuario usuario = usuarioPersistido();

        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(CONTRASENA, CONTRASENA_ENCRIPTADA)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(credenciales(CORREO, CONTRASENA)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales incorrectas");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private LoginRequest credenciales(String correo, String contrasena) {
        return LoginRequest.builder()
                .correoUsuario(correo)
                .contraseña(contrasena)
                .build();
    }

    private Usuario usuarioPersistido() {
        return Usuario.builder()
                .idUsuario(7L)
                .rol(Rol.builder().idRol(1L).nombreRol("DONANTE").build())
                .dniUsuario("87654321")
                .dniVerificado(true)
                .nombreUsuario("María")
                .apellidosUsuario("García López")
                .correoUsuario(CORREO)
                .contraseña(CONTRASENA_ENCRIPTADA)
                .telefonoUsuario("987654321")
                .fechaRegistro(LocalDateTime.of(2026, 1, 15, 10, 30))
                .build();
    }

}