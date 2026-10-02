package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Página de resultados")
public class PaginaDTO<T> {

    private List<T> contenido;

    @Schema(description = "Número de página, empezando en 0")
    private int pagina;

    private int tamanio;

    private long totalElementos;

    private int totalPaginas;

}
