package co.com.pragma.validator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBranchRequest(

        @NotNull(message = "El identificador de la franquicia es obligatorio")
        UUID franchiseId,

        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name
) {
}
