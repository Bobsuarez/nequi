package co.com.pragma.api.handler.error;

import co.com.pragma.exception.commands.handlers.ExceptionHandlerRegistry;
import co.com.pragma.exception.dto.ResponseDTO;
import co.com.pragma.model.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static co.com.pragma.exception.utils.ResponseUtils.getErrorDTOMap;

@Component
public class BusinessHandlerError implements ExceptionHandlerRegistry {

    @Override
    public ResponseDTO<Object> handleException(Throwable ex) {
        return ResponseDTO.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .error(getErrorDTOMap(ex.getClass().getSimpleName(), ex.getMessage()))
                .build();
    }

    @Override
    public Class<? extends Throwable> getExceptionClass() {
        return BusinessException.class;
    }
}
