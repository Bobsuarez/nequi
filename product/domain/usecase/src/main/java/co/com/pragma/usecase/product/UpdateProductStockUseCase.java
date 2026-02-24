package co.com.pragma.usecase.product;

import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductId;
import co.com.pragma.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateProductStockUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> execute(ProductId productId, int stock) {
        if (stock < 0) {
            return Mono.error(new BusinessException("El stock no puede ser negativo"));
        }
        return Mono.defer(() ->
                productRepository.findById(productId)
                        .switchIfEmpty(Mono.error(new BusinessException(
                                BusinessException.PRODUCT_NOT_FOUND,
                                "Producto no encontrado. productId=" + productId.value())))
                        .filter(p -> !p.isDeleted())
                        .switchIfEmpty(Mono.error(new BusinessException("El producto está eliminado")))
                        .map(p -> p.withStock(stock))
                        .flatMap(updated -> productRepository.update(updated, co.com.pragma.model.product.ProductStockUpdatedEvent.of(updated)))
        );
    }
}
