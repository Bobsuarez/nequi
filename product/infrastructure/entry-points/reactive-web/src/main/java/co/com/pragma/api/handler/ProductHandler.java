package co.com.pragma.api.handler;

import co.com.pragma.model.product.ProductId;
import co.com.pragma.validator.dto.request.CreateProductRequest;
import co.com.pragma.validator.dto.request.UpdateNameRequest;
import co.com.pragma.validator.dto.request.UpdateStockRequest;
import co.com.pragma.validator.engine.ValidatorEngine;
import co.com.pragma.validator.mappers.ProductApiMapper;
import co.com.pragma.usecase.product.CreateProductUseCase;
import co.com.pragma.usecase.product.DeleteProductUseCase;
import co.com.pragma.usecase.product.UpdateProductNameUseCase;
import co.com.pragma.usecase.product.UpdateProductStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final ProductApiMapper productApiMapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        String traceId = extractTraceId(request);
        return request.bodyToMono(CreateProductRequest.class)
                .doOnNext(ValidatorEngine::validate)
                .map(productApiMapper::toDomain)
                .flatMap(createProductUseCase::execute)
                .map(productApiMapper::toResponse)
                .flatMap(response -> status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSubscribe(s -> log.info("POST /products traceId={}", traceId))
                .doOnError(e -> log.error("Error POST /products traceId={}", traceId, e));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String traceId = extractTraceId(request);
        return Mono.justOrEmpty(request.pathVariable("productId"))
                .map(UUID::fromString)
                .map(ProductId::of)
                .flatMap(deleteProductUseCase::execute)
                .then(noContent().build())
                .doOnSubscribe(s -> log.info("DELETE /products/{} traceId={}", request.pathVariable("productId"), traceId))
                .doOnError(e -> log.error("Error DELETE /products traceId={}", traceId, e));
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        String traceId = extractTraceId(request);
        return Mono.zip(
                        Mono.justOrEmpty(request.pathVariable("productId")).map(UUID::fromString).map(ProductId::of),
                        request.bodyToMono(UpdateStockRequest.class).doOnNext(ValidatorEngine::validate))
                .flatMap(tuple -> updateProductStockUseCase.execute(tuple.getT1(), tuple.getT2().stock()))
                .map(productApiMapper::toResponse)
                .flatMap(response -> ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response))
                .doOnSubscribe(s -> log.info("PATCH /products/{}/stock traceId={}", request.pathVariable("productId"), traceId))
                .doOnError(e -> log.error("Error PATCH stock traceId={}", traceId, e));
    }

    public Mono<ServerResponse> updateName(ServerRequest request) {
        String traceId = extractTraceId(request);
        return Mono.zip(
                        Mono.justOrEmpty(request.pathVariable("productId")).map(UUID::fromString).map(ProductId::of),
                        request.bodyToMono(UpdateNameRequest.class).doOnNext(ValidatorEngine::validate))
                .flatMap(tuple -> updateProductNameUseCase.execute(tuple.getT1(), tuple.getT2().name()))
                .map(productApiMapper::toResponse)
                .flatMap(response -> ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response))
                .doOnSubscribe(s -> log.info("PATCH /products/{}/name traceId={}", request.pathVariable("productId"), traceId))
                .doOnError(e -> log.error("Error PATCH name traceId={}", traceId, e));
    }

    private String extractTraceId(ServerRequest request) {
        String header = request.headers().firstHeader("X-B3-TraceId");
        return header != null ? header.replace("\"", "") : UUID.randomUUID().toString();
    }
}
