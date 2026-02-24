package co.com.pragma.usecase.product;

import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.product.BranchId;
import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductId;
import co.com.pragma.model.product.gateways.KnownBranchFranchiseRepository;
import co.com.pragma.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateProductUseCase {

    private final KnownBranchFranchiseRepository knownBranchFranchiseRepository;
    private final ProductRepository productRepository;

    public Mono<Product> execute(Product product) {
        return Mono.defer(() ->
                validateBranchSynchronized(product.getBranchId().value())
                        .thenReturn(product)
                        .flatMap(this::persistProduct)
        );
    }

    private Mono<Void> validateBranchSynchronized(UUID branchId) {
        return knownBranchFranchiseRepository.existsByBranchId(branchId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new BusinessException(
                        BusinessException.BRANCH_NOT_SYNCHRONIZED,
                        "La sucursal no está sincronizada. branchId=" + branchId)))
                .then();
    }

    private Mono<Product> persistProduct(Product product) {
        Product toSave = product.getId() == null
                ? Product.create(ProductId.generate(), product.getBranchId(), product.getName(), product.getStock())
                : product;
        return productRepository.save(toSave);
    }
}
