package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kit de ayuda armado")
public class KitResponseDTO {

    private Integer idKit;

    private String codigoKit;

    private String nombreKit;

    private String estado;

    private LocalDateTime fechaCreacion;

    private Long idLocal;

    private String nombreLocal;

    private List<KitDetalleResponseDTO> detalles;

}