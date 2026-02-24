package co.com.pragma.api.handler;

import co.com.pragma.usecase.franchise.GetTopProductsByFranchiseUseCase;
import co.com.pragma.validator.mappers.FranchiseApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final GetTopProductsByFranchiseUseCase getTopProductsByFranchiseUseCase;
    private final FranchiseApiMapper franchiseApiMapper;

    public Mono<ServerResponse> getTopProductsByFranchise(ServerRequest request) {
        String traceId = extractTraceId(request);
        return Mono.justOrEmpty(request.pathVariable("franchiseId"))
                .map(UUID::fromString)
                .flatMap(franchiseId -> getTopProductsByFranchiseUseCase.execute(franchiseId)
                        .map(franchiseApiMapper::toResponse)
                        .collectList()
                        .flatMap(list -> ok().contentType(MediaType.APPLICATION_JSON).bodyValue(list)))
                .switchIfEmpty(Mono.defer(() -> ok().contentType(MediaType.APPLICATION_JSON).bodyValue(java.util.List.of())))
                .doOnSubscribe(s -> log.info("GET /franchises/{}/top-products traceId={}", request.pathVariable("franchiseId"), traceId))
                .doOnError(e -> log.error("Error GET top-products traceId={}", traceId, e));
    }

    private String extractTraceId(ServerRequest request) {
        String header = request.headers().firstHeader("X-B3-TraceId");
        return header != null ? header.replace("\"", "") : UUID.randomUUID().toString();
    }
}
