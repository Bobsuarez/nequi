package co.com.pragma.model.product.gateways;

import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductId;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findById(ProductId id);

    /**
     * Actualiza el producto y registra el evento en outbox en la misma transacción.
     * @param event evento de dominio (ProductDeletedEvent, ProductStockUpdatedEvent, ProductRenamedEvent)
     */
    Mono<Product> update(Product product, Object event);

    Mono<Void> deleteById(ProductId id);
}
