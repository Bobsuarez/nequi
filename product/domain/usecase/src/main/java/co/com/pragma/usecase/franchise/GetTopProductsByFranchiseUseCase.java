package co.com.pragma.usecase.franchise;

import co.com.pragma.model.product.TopProductByBranch;
import co.com.pragma.model.product.gateways.TopProductsByBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Log
@RequiredArgsConstructor
public class GetTopProductsByFranchiseUseCase {

    private final TopProductsByBranchRepository topProductsByBranchRepository;

    public Flux<TopProductByBranch> execute(UUID franchiseId) {
        return topProductsByBranchRepository.findByFranchiseId(franchiseId)
                .doOnComplete(() -> log.info("Top products por franquicia franchiseId={}" + franchiseId));
    }
}
