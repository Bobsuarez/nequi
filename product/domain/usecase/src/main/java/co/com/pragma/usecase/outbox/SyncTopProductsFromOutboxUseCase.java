package co.com.pragma.usecase.outbox;

import co.com.pragma.model.product.ProductId;
import co.com.pragma.model.product.ProductCreatedEvent;
import co.com.pragma.model.product.ProductDeletedEvent;
import co.com.pragma.model.product.ProductRenamedEvent;
import co.com.pragma.model.product.ProductStockUpdatedEvent;
import co.com.pragma.model.product.TopProductByBranch;
import co.com.pragma.model.product.gateways.KnownBranchFranchiseRepository;
import co.com.pragma.model.product.gateways.OutboxRepository;
import co.com.pragma.model.product.gateways.ProductRepository;
import co.com.pragma.model.product.gateways.TopProductsByBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Procesa eventos del outbox y sincroniza en top_products_by_branch (ya no publica a RabbitMQ).
 */
@Log
@RequiredArgsConstructor
public class SyncTopProductsFromOutboxUseCase {

    private final OutboxRepository outboxRepository;
    private final KnownBranchFranchiseRepository knownBranchFranchiseRepository;
    private final TopProductsByBranchRepository topProductsByBranchRepository;
    private final ProductRepository productRepository;

    public Mono<Void> execute(int batchSize) {
        return outboxRepository.findUnpublished(batchSize)
                .flatMap(outboxEvent ->
                        syncEventToTopProducts(outboxEvent.event())
                                .then(outboxRepository.markAsPublished(outboxEvent.id()))
                                .doOnSuccess(v -> log.info("Outbox event sincronizado a top_products_by_branch id={}" + outboxEvent.id()))
                                .doOnError(e -> log.info("Error sincronizando outbox id={}: {}" + e.getMessage()))
                                .onErrorResume(e -> Mono.empty()))
                .then();
    }

    private Mono<Void> syncEventToTopProducts(Object event) {
        if (event instanceof ProductCreatedEvent e) {
            log.info("Sincronizando ProductCreatedEvent a top_products_by_branch productId="+ e.productId());
            return syncFromCreated(e);
        }
        if (event instanceof ProductStockUpdatedEvent e) {
            log.info("Sincronizando ProductStockUpdatedEvent a top_products_by_branch productId="+ e.productId());
            return syncFromStockUpdated(e);
        }
        if (event instanceof ProductRenamedEvent e) {
            log.info("Sincronizando ProductRenamedEvent a top_products_by_branch productId="+ e.productId());
            return syncFromRenamed(e);
        }
        if (event instanceof ProductDeletedEvent e) {
            log.info("Sincronizando ProductDeletedEvent a top_products_by_branch productId="+ e.productId());
            return syncFromDeleted(e);
        }
        return Mono.empty();
    }

    private Mono<Void> syncFromCreated(ProductCreatedEvent e) {
        return knownBranchFranchiseRepository.findByBranchId(e.branchId())
                .flatMap(kbf -> topProductsByBranchRepository.saveOrUpdate(TopProductByBranch.builder()
                        .franchiseId(kbf.getFranchiseId())
                        .branchId(e.branchId())
                        .productId(e.productId())
                        .productName(e.name())
                        .stock(e.stock())
                        .updatedAt(Instant.now())
                        .build()))
                .then();
    }

    private Mono<Void> syncFromStockUpdated(ProductStockUpdatedEvent e) {
        return productRepository.findById(ProductId.of(e.productId()))
                .switchIfEmpty(Mono.empty())
                .flatMap(product -> knownBranchFranchiseRepository.findByBranchId(product.getBranchId().value())
                        .flatMap(kbf -> topProductsByBranchRepository.saveOrUpdate(TopProductByBranch.builder()
                                .franchiseId(kbf.getFranchiseId())
                                .branchId(product.getBranchId().value())
                                .productId(e.productId())
                                .productName(product.getName())
                                .stock(e.stock())
                                .updatedAt(Instant.now())
                                .build())))
                .then();
    }

    private Mono<Void> syncFromRenamed(ProductRenamedEvent e) {
        return productRepository.findById(ProductId.of(e.productId()))
                .switchIfEmpty(Mono.empty())
                .flatMap(product -> knownBranchFranchiseRepository.findByBranchId(product.getBranchId().value())
                        .flatMap(kbf -> topProductsByBranchRepository.saveOrUpdate(TopProductByBranch.builder()
                                .franchiseId(kbf.getFranchiseId())
                                .branchId(product.getBranchId().value())
                                .productId(e.productId())
                                .productName(e.name())
                                .stock(product.getStock())
                                .updatedAt(Instant.now())
                                .build())))
                .then();
    }

    private Mono<Void> syncFromDeleted(ProductDeletedEvent e) {
        return productRepository.findById(ProductId.of(e.productId()))
                .switchIfEmpty(Mono.empty())
                .flatMap(product -> topProductsByBranchRepository.deleteByBranchId(product.getId().value()))
                .then();
    }
}
