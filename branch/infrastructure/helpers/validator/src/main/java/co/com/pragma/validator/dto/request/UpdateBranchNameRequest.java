package co.com.pragma.validator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBranchNameRequest(

        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name
) {
}
