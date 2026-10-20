package com.ecommerce.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int categoryId;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Supplementary join/calculated fields
    private String categoryName;
    private double averageRating;
    private int reviewCount;
    private java.util.List<String> images = new java.util.ArrayList<>();

    public java.util.List<String> getImages() {
        if (images == null || images.isEmpty()) {
            if (imageUrl != null && !imageUrl.isBlank()) {
                return java.util.List.of(imageUrl);
            }
            return java.util.Collections.emptyList();
        }
        return images;
    }

    public void setImages(java.util.List<String> images) {
        this.images = (images != null) ? new java.util.ArrayList<>(images) : new java.util.ArrayList<>();
    }

    public int getImageCount() {
        return getImages().size();
    }

    public void addImage(String url) {
        if (images == null) {
            images = new java.util.ArrayList<>();
        }
        if (url != null && !url.isBlank()) {
            images.add(url);
        }
    }

    public Product() {
    }

    public Product(int id, int categoryId, String name, String slug, String description, BigDecimal price, int stockQuantity, String imageUrl, boolean active, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Product(int id, int categoryId, String name, String slug, String description, BigDecimal price, int stockQuantity, String imageUrl, boolean active, Timestamp createdAt, Timestamp updatedAt, String categoryName, double averageRating, int reviewCount) {
        this(id, categoryId, name, slug, description, price, stockQuantity, imageUrl, active, createdAt, updatedAt);
        this.categoryName = categoryName;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }
}
