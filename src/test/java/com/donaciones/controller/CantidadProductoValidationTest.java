package com.donaciones.controller;

import com.donaciones.exception.GlobalExceptionHandler;
import com.donaciones.service.DonacionService;
import com.donaciones.service.HistorialEstadoService;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Validación de cantidades al registrar un producto donado")
class CantidadProductoValidationTest {

    private static final String RUTA = "/api/v1/donaciones";

    @Mock
    private DonacionService donacionService;

    @Mock
    private HistorialEstadoService historialEstadoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        LocalValidatorFactoryBean validador = new LocalValidatorFactoryBean();
        validador.setProviderClass(HibernateValidator.class);
        validador.setMessageInterpolator(new ParameterMessageInterpolator());
        validador.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DonacionController(donacionService, historialEstadoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validador)
                .build();
    }

    // CA2: el backend acepta la cantidad numérica positiva y no la rechaza por validación.
    @Test
    @DisplayName("CA2: Acepta la cantidad 50 y delega el registro al servicio")
    void registrarProducto_conCantidadCincuenta_llegaAlServicio() throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCantidadCruda("50")))
                .andExpect(status().isCreated());

        verify(donacionService).registrarDonacion(any());
    }

    // CA2: cantidad negativa -> error de validación y no se guarda nada.
    @Test
    @DisplayName("CA2: Rechaza la cantidad -5 con error de validación y no guarda el registro")
    void registrarProducto_conCantidadNegativa_esRechazadoYNoSeGuarda() throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCantidadCruda("-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cantidadDeclarada")))
                .andExpect(jsonPath("$.message", containsString("La cantidad declarada debe ser mayor a 0")));

        verifyNoInteractions(donacionService);
    }

    @Test
    @DisplayName("CA2: Rechaza la cantidad 0 con error de validación y no guarda el registro")
    void registrarProducto_conCantidadCero_esRechazadoYNoSeGuarda() throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCantidadCruda("0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cantidadDeclarada")))
                .andExpect(jsonPath("$.message", containsString("La cantidad declarada debe ser mayor a 0")));

        verifyNoInteractions(donacionService);
    }

    // CA2: valores no numéricos -> Jackson no puede deserializar el BigDecimal.
    @ParameterizedTest(name = "cantidad {0} no es numérica")
    @ValueSource(strings = {"abc", "10#"})
    @DisplayName("CA2: Rechaza cantidades no numéricas ('abc' y '10#') y no guarda el registro")
    void registrarProducto_conCantidadNoNumerica_esRechazadoYNoSeGuarda(String cantidad) throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCantidadCruda("\"" + cantidad + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El cuerpo de la solicitud no es válido o está vacío"));

        verifyNoInteractions(donacionService);
    }

    @ParameterizedTest(name = "cantidad numérica {0} es rechazada si no supera 0")
    @CsvSource({"-0.01", "-100"})
    @DisplayName("CA2: Rechaza otras cantidades negativas y no guarda el registro")
    void registrarProducto_conOtrasCantidadesNegativas_esRechazado(String cantidad) throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCantidadCruda(cantidad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(donacionService);
    }

    private String cuerpoConCantidadCruda(String tokenCantidad) {
        return """
                {
                  "idUsuario": 7,
                  "idLocalRecepcion": 2,
                  "detalles": [
                    {
                      "idCategoria": 1,
                      "descripcionDetalle": "Arroz extra 1 kg",
                      "cantidadDeclarada": %s
                    }
                  ]
                }
                """.formatted(tokenCantidad);
    }

}