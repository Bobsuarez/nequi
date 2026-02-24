package co.com.pragma.r2dbc.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("top_products_by_branch")
public class TopProductByBranchEntity {

    @Id
    private Integer id;
    private UUID franchise;
    private UUID branchId;
    private UUID productId;
    private String productName;
    private Integer stock;
    private LocalDateTime updatedAt;
}
