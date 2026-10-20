package com.ecommerce.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Timestamp createdAt;

    public Category() {
    }

    public Category(int id, String name, String slug, String description, String imageUrl, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    private int itemCount = 0;

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getIconClass() {
        if (slug == null && name == null) return "fa-tag";
        String s = (slug != null ? slug : name).toLowerCase();
        if (s.contains("electron") || s.contains("laptop") || s.contains("phone")) return "fa-laptop";
        if (s.contains("fashion") || s.contains("apparel") || s.contains("cloth")) return "fa-shirt";
        if (s.contains("home") || s.contains("kitchen")) return "fa-kitchen-set";
        if (s.contains("audio") || s.contains("sound") || s.contains("headphone")) return "fa-headphones";
        if (s.contains("sport") || s.contains("fitness") || s.contains("gym")) return "fa-dumbbell";
        return "fa-shapes";
    }
}
