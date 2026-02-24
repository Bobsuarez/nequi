package co.com.pragma.usecase.product;

import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductId;
import co.com.pragma.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Void> execute(ProductId productId) {
        return Mono.defer(() ->
                productRepository.findById(productId)
                        .switchIfEmpty(Mono.error(new BusinessException(
                                BusinessException.PRODUCT_NOT_FOUND,
                                "Producto no encontrado. productId=" + productId.value())))
                        .map(Product::markDeleted)
                        .flatMap(deleted -> productRepository.deleteById(deleted.getId()))
                        .then()
        );
    }
}
