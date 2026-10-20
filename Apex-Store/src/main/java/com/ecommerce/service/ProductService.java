package com.ecommerce.service;

import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final ReviewDAO reviewDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.reviewDAO = new ReviewDAO();
    }

    public ProductService(ProductDAO productDAO, CategoryDAO categoryDAO, ReviewDAO reviewDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.reviewDAO = reviewDAO;
    }

    public List<Product> getFeaturedProducts(int limit) {
        return productDAO.getFeaturedProducts(limit);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Optional<Category> getCategoryById(int id) {
        return categoryDAO.findById(id);
    }

    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryDAO.findBySlug(slug);
    }

    public Optional<Product> getProductById(int id) {
        return productDAO.findById(id);
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productDAO.findBySlug(slug);
    }

    public record ProductPageResult(List<Product> products, int totalCount, int currentPage, int totalPages) {}

    public ProductPageResult searchAndFilterProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                    String sortBy, String sortOrder, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(pageSize, 50));
        int offset = (safePage - 1) * safePageSize;

        List<Product> products = productDAO.searchProducts(keyword, categoryId, minPrice, maxPrice, sortBy, sortOrder, offset, safePageSize);
        int totalCount = productDAO.countSearchProducts(keyword, categoryId, minPrice, maxPrice);
        int totalPages = (int) Math.ceil((double) totalCount / safePageSize);

        return new ProductPageResult(products, totalCount, safePage, totalPages);
    }

    public List<Review> getProductReviews(int productId) {
        return reviewDAO.findByProductId(productId);
    }

    public boolean addProductReview(int userId, int productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        return reviewDAO.createReview(review);
    }

    public boolean canUserReview(int userId, int productId) {
        return reviewDAO.hasUserPurchasedProduct(userId, productId) && !reviewDAO.hasUserReviewed(userId, productId);
    }
}
