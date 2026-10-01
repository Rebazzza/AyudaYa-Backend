package com.donaciones.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación del registro de local de acopio")
class LocalRequestValidationTest {

    private static final String DIRECCION = "Av. Ejemplo 123";
    private static final String TELEFONO_VALIDO = "987654321";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Acepta el local nuevo con dirección, capacidad 100 y teléfono de 9 dígitos")
    void registrarLocal_conDatosValidos_noGeneraViolaciones() {
        Set<ConstraintViolation<LocalRequest>> violations = validator.validate(localConTelefono(TELEFONO_VALIDO));

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest(name = "telefono {0} debe ser rechazado por no tener 9 dígitos")
    @ValueSource(strings = {"98765432", "9876543210"})
    @DisplayName("Rechaza el teléfono cuando no tiene exactamente 9 dígitos")
    void registrarLocal_conTelefonoDeLongitudDistintaA9Digitos_esRechazado(String telefono) {
        Set<ConstraintViolation<LocalRequest>> violations = validator.validate(localConTelefono(telefono));

        assertThat(violations)
                .extracting(violacion -> violacion.getPropertyPath().toString())
                .contains("telefonoLocal");
    }

    private LocalRequest localConTelefono(String telefono) {
        return LocalRequest.builder()
                .nombreLocal("Coliseo Manuel Bonilla")
                .direccionLocal(DIRECCION)
                .latitud(new BigDecimal("-12.04637400"))
                .longitud(new BigDecimal("-77.04279300"))
                .capacidadLocalM3(100.0)
                .telefonoLocal(telefono)
                .build();
    }

}