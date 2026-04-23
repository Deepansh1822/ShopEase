package in.ds.ShopEase.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Product product;
    private int quantity;
}
