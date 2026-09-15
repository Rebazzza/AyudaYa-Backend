package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Total de fondos recaudados y verificados")
public class TotalFondosDTO {

    @Schema(example = "1250.50")
    private BigDecimal totalRecaudadoPEN;

}