package co.com.pragma.validator.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para solicitar el registro de una nueva franquicia")
public class CreateFranchiseRequest {

    @NotBlank(message = "El nombre de la franquicia es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    public String name;
}
