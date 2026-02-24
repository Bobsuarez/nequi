package co.com.pragma.api.handler.error;

import co.com.pragma.exception.commands.handlers.ExceptionHandlerRegistry;
import co.com.pragma.exception.dto.ResponseDTO;
import co.com.pragma.model.exceptions.ValidationException;
import co.com.pragma.model.validations.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static co.com.pragma.exception.utils.ResponseUtils.getListErrorDTOMap;

@Slf4j
@Component
public class ValidationHandlerError implements ExceptionHandlerRegistry {

    @Override
    public ResponseDTO<Object> handleException(Throwable ex) {
        ValidationException exception = (ValidationException) ex;
        log.error("ValidationHandlerException: Handling ValidationException - {}", exception.getErrors());
        return ResponseDTO.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .error(getListErrorDTOMap(
                        exception.getErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                        ValidationError::fieldName,
                                        ValidationError::message
                                ))))
                .build();
    }

    @Override
    public Class<? extends Throwable> getExceptionClass() {
        return ValidationException.class;
    }
}
