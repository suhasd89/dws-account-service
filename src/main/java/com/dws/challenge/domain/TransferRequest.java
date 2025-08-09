package com.dws.challenge.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @Schema(description = "Source account ID", example = "12345")
    @NotNull
    private String accountFromId;

    @Schema(description = "Destination account ID", example = "67890")
    @NotNull
    private String accountToId;

    @Schema(description = "Amount to transfer", example = "100.00")
    @NotNull
    @Min(value = 1, message = "Transfer amount must be positive.")
    private BigDecimal amount;
}
