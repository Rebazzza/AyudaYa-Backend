package com.donaciones.config;

import com.donaciones.entity.Rol;
import com.donaciones.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        seedRole("Donante", "Usuario que realiza donaciones de emergencia");
        seedRole("Personal de Apoyo", "Voluntario que asiste a los locales de recepción para ayudar en la entrega y atención de personas afectadas");
        seedRole("TrabajadorCentroAcopio", "Personal que gestiona el centro de acopio");
        seedRole("Organizacion", "Entidad organizadora responsable de la campaña");
        seedRole("Receptor", "ONG, comedor social u organización que solicita y recibe ayuda para personas afectadas");
        seedRole("Administrador", "Usuario con permisos de administración del sistema");
    }

    private void seedRole(String nombre, String descripcion) {
        if (rolRepository.findByNombreRol(nombre).isEmpty()) {
            rolRepository.save(Rol.builder()
                    .nombreRol(nombre)
                    .descripcionRol(descripcion)
                    .build());
            log.info("Rol '{}' creado", nombre);
        }
    }

}