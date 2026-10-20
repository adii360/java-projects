package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CouponModelTest {

    @Test
    public void testPercentageDiscountCalculation() {
        Coupon coupon = new Coupon(
                1,
                "SAVE10",
                DiscountType.PERCENTAGE,
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                Date.valueOf(LocalDate.now().plusDays(10)),
                true
        );

        assertTrue(coupon.isValidForAmount(new BigDecimal("100.00")));
        BigDecimal discount = coupon.calculateDiscount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("10.00"), discount);

        // Subtotal below minimum order amount
        assertFalse(coupon.isValidForAmount(new BigDecimal("40.00")));
        assertEquals(BigDecimal.ZERO, coupon.calculateDiscount(new BigDecimal("40.00")));
    }

    @Test
    public void testFixedDiscountCalculation() {
        Coupon coupon = new Coupon(
                2,
                "FLAT25",
                DiscountType.FIXED,
                new BigDecimal("25.00"),
                new BigDecimal("50.00"),
                Date.valueOf(LocalDate.now().plusDays(5)),
                true
        );

        BigDecimal discount = coupon.calculateDiscount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("25.00"), discount);

        // Discount cannot exceed subtotal
        BigDecimal smallSubtotal = new BigDecimal("20.00");
        coupon.setMinimumOrderAmount(BigDecimal.ZERO);
        assertEquals(new BigDecimal("20.00"), coupon.calculateDiscount(smallSubtotal));
    }

    @Test
    public void testExpiredCouponIsNotValid() {
        Coupon coupon = new Coupon(
                3,
                "OLD",
                DiscountType.FIXED,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                Date.valueOf(LocalDate.now().minusDays(1)),
                true
        );

        assertTrue(coupon.isExpired());
        assertFalse(coupon.isValidForAmount(new BigDecimal("100.00")));
        assertEquals(BigDecimal.ZERO, coupon.calculateDiscount(new BigDecimal("100.00")));
    }
}
