package com.proconsi.electrobazar.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class IneIpcResponse {

    @JsonProperty("Data")
    private List<IneDataPoint> data;

    @Data
    public static class IneDataPoint {
        @JsonProperty("Valor")
        private BigDecimal valor;

        @JsonProperty("Anyo")
        private Integer anyo;

        @JsonProperty("Mes")
        private Integer mes;
    }
}
