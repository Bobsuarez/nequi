package co.com.pragma.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private ProductId id;
    private BranchId branchId;
    private String name;
    private int stock;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;

    public static Product create(ProductId id, BranchId branchId, String name, int stock) {
        Instant now = Instant.now();
        return Product.builder()
                .id(id)
                .branchId(branchId)
                .name(name != null ? name.trim() : null)
                .stock(stock)
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();
    }

    public Product withStock(int newStock) {
        return Product.builder()
                .id(this.id)
                .branchId(this.branchId)
                .name(this.name)
                .stock(newStock)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .deleted(this.deleted)
                .build();
    }

    public Product withName(String newName) {
        return Product.builder()
                .id(this.id)
                .branchId(this.branchId)
                .name(newName != null ? newName.trim() : null)
                .stock(this.stock)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .deleted(this.deleted)
                .build();
    }

    public Product markDeleted() {
        return Product.builder()
                .id(this.id)
                .branchId(this.branchId)
                .name(this.name)
                .stock(this.stock)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .deleted(true)
                .build();
    }
}
