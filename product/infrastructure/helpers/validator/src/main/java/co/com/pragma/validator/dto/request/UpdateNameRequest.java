package co.com.pragma.validator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNameRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name
) {}
