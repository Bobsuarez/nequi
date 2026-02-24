package co.com.pragma.api.handler;

import co.com.pragma.model.franchise.FranchiseId;
import co.com.pragma.validator.dto.request.CreateFranchiseRequest;
import co.com.pragma.validator.dto.request.UpdateFranchiseNameRequest;
import co.com.pragma.validator.dto.respose.FranchiseResponse;
import co.com.pragma.usecase.franchise.CreateFranchiseUseCase;
import co.com.pragma.usecase.franchise.UpdateFranchiseNameUseCase;
import co.com.pragma.validator.engine.ValidatorEngine;
import co.com.pragma.validator.mappers.FranchiseApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final FranchiseApiMapper franchiseApiMapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        String traceId = extractTraceId(request);

        return request.bodyToMono(CreateFranchiseRequest.class)
                .doOnNext(ValidatorEngine::validate)
                .map(franchiseApiMapper::toDomain)
                .flatMap(createFranchiseUseCase::execute)
                .map(franchiseApiMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSubscribe(s -> log.info("POST /api/v1/franchises traceId={}", traceId))
                .doOnError(error -> log.error("Error en POST /api/v1/franchises traceId={}", traceId, error));
    }

    public Mono<ServerResponse> updateName(ServerRequest request) {
        String traceId = extractTraceId(request);
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));

        return request.bodyToMono(UpdateFranchiseNameRequest.class)
                .doOnNext(ValidatorEngine::validate)
                .flatMap(req -> updateFranchiseNameUseCase.execute(FranchiseId.of(franchiseId), req.name().trim()))
                .map(franchiseApiMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSubscribe(s -> log.info("PATCH /api/v1/franchises/{} name traceId={}", franchiseId, traceId))
                .doOnError(error -> log.error("Error en PATCH /api/v1/franchises/{} name traceId={}", franchiseId, traceId, error));
    }

    private String extractTraceId(ServerRequest request) {
        String traceId = request.headers().firstHeader("X-B3-TraceId");
        return traceId != null ? traceId.replace("\"", "") : UUID.randomUUID().toString();
    }
}
