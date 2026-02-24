package co.com.pragma.api.handler;

import co.com.pragma.model.branch.BranchId;
import co.com.pragma.model.exceptions.ValidationException;
import co.com.pragma.model.validations.ValidationError;
import co.com.pragma.usecase.branch.CreateBranchUseCase;
import co.com.pragma.usecase.branch.UpdateBranchNameUseCase;
import co.com.pragma.validator.dto.request.CreateBranchRequest;
import co.com.pragma.validator.dto.request.UpdateBranchNameRequest;
import co.com.pragma.validator.dto.response.BranchResponse;
import co.com.pragma.validator.engine.ValidatorEngine;
import co.com.pragma.validator.mappers.BranchApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final CreateBranchUseCase createBranchUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final BranchApiMapper branchApiMapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        String traceId = extractTraceId(request);

        return request.bodyToMono(CreateBranchRequest.class)
                .doOnNext(ValidatorEngine::validate)
                .map(branchApiMapper::toDomain)
                .flatMap(createBranchUseCase::execute)
                .map(branchApiMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSubscribe(s -> log.info("POST /api/v1/branches traceId={}", traceId))
                .doOnError(error -> log.error("Error en POST /api/v1/branches traceId={}", traceId, error));
    }

    public Mono<ServerResponse> updateName(ServerRequest request) {
        String traceId = extractTraceId(request);
        Mono<UUID> branchIdMono = Mono.fromCallable(() -> UUID.fromString(request.pathVariable("branchId")))
                .onErrorMap(IllegalArgumentException.class, e -> new ValidationException(
                        List.of(new ValidationError("branchId", "Identificador de sucursal inválido"))));

        return branchIdMono.flatMap(branchId -> request.bodyToMono(UpdateBranchNameRequest.class)
                .doOnNext(ValidatorEngine::validate)
                .flatMap(req -> updateBranchNameUseCase.execute(BranchId.of(branchId), req.name().trim()))
                .map(branchApiMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)))
                .doOnSubscribe(s -> log.info("PATCH /api/v1/branches/{{branchId}} name traceId={}", traceId))
                .doOnError(error -> log.error("Error en PATCH /api/v1/branches name traceId={}", traceId, error));
    }

    private String extractTraceId(ServerRequest request) {
        String traceId = request.headers().firstHeader("X-B3-TraceId");
        return traceId != null ? traceId.replace("\"", "") : UUID.randomUUID().toString();
    }
}
