package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartModelTest {

    @Test
    public void testCartItemLineTotalCalculation() {
        Product product = new Product();
        product.setId(10);
        product.setName("Wireless Earbuds");
        product.setPrice(new BigDecimal("49.99"));

        CartItem item = new CartItem();
        item.setProductId(10);
        item.setQuantity(3);
        item.setProduct(product);

        BigDecimal expected = new BigDecimal("149.97");
        assertEquals(expected, item.getLineTotal());
    }

    @Test
    public void testCartItemNullProductReturnsZero() {
        CartItem item = new CartItem();
        item.setQuantity(2);
        assertEquals(BigDecimal.ZERO, item.getLineTotal());
    }
}
