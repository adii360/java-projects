package com.ecommerce.dao;

import com.ecommerce.model.Product;
import com.ecommerce.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    // Whitelist allowed sort fields to prevent SQL injection in ORDER BY
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "p.price", "p.name", "p.created_at", "p.stock_quantity"
    );

    public List<Product> findAll(boolean onlyActive, int offset, int limit) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                "c.name AS category_name, " +
                "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                "COUNT(r.id) AS review_count " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN reviews r ON p.id = r.product_id "
        );

        if (onlyActive) {
            sql.append("WHERE p.active = TRUE ");
        }
        sql.append("GROUP BY p.id, c.name ");
        sql.append("ORDER BY p.id DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching products", e);
        }
        if (list.isEmpty()) {
            return getDefaultProducts().stream()
                    .filter(p -> !onlyActive || p.isActive())
                    .skip(Math.max(0, offset))
                    .limit(Math.max(1, limit))
                    .toList();
        }
        return list;
    }

    public int countAll(boolean onlyActive) {
        String sql = onlyActive ? "SELECT COUNT(*) FROM products WHERE active = TRUE" : "SELECT COUNT(*) FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int c = rs.getInt(1);
                if (c > 0) return c;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting products", e);
        }
        return (int) getDefaultProducts().stream().filter(p -> !onlyActive || p.isActive()).count();
    }

    public Optional<Product> findById(int id) {
        String sql = "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                     "c.name AS category_name, " +
                     "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                     "COUNT(r.id) AS review_count " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN reviews r ON p.id = r.product_id " +
                     "WHERE p.id = ? " +
                     "GROUP BY p.id, c.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching product by id: " + id, e);
        }
        return getDefaultProducts().stream().filter(p -> p.getId() == id).findFirst();
    }

    public Optional<Product> findBySlug(String slug) {
        String sql = "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                     "c.name AS category_name, " +
                     "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                     "COUNT(r.id) AS review_count " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN reviews r ON p.id = r.product_id " +
                     "WHERE p.slug = ? " +
                     "GROUP BY p.id, c.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching product by slug: " + slug, e);
        }
        if (slug != null) {
            return getDefaultProducts().stream().filter(p -> slug.equalsIgnoreCase(p.getSlug())).findFirst();
        }
        return Optional.empty();
    }

    public List<Product> getFeaturedProducts(int limit) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                     "c.name AS category_name, " +
                     "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                     "COUNT(r.id) AS review_count " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN reviews r ON p.id = r.product_id " +
                     "WHERE p.active = TRUE " +
                     "GROUP BY p.id, c.name " +
                     "ORDER BY p.id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching featured products", e);
        }
        if (list.isEmpty()) {
            return getDefaultProducts().stream().limit(limit).toList();
        }
        return list;
    }

    public List<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                        String sortBy, String sortOrder, int offset, int limit) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                "c.name AS category_name, " +
                "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                "COUNT(r.id) AS review_count " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN reviews r ON p.id = r.product_id " +
                "WHERE p.active = TRUE "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String wildcard = "%" + keyword.trim() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        sql.append("GROUP BY p.id, c.name ");

        // Strict whitelist validation for sorting
        String safeSort = "p.id";
        if (sortBy != null && ALLOWED_SORT_COLUMNS.contains(sortBy.trim().toLowerCase())) {
            safeSort = sortBy.trim().toLowerCase();
        }

        String safeOrder = "DESC";
        if (sortOrder != null && sortOrder.equalsIgnoreCase("ASC")) {
            safeOrder = "ASC";
        }

        sql.append("ORDER BY ").append(safeSort).append(" ").append(safeOrder).append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching products", e);
        }
        if (list.isEmpty()) {
            return filterDefaultProducts(keyword, categoryId, minPrice, maxPrice, safeSort, safeOrder, offset, limit);
        }
        return list;
    }

    public int countSearchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE p.active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String wildcard = "%" + keyword.trim() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int c = rs.getInt(1);
                    if (c > 0) return c;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting search products", e);
        }
        return countFilterDefaultProducts(keyword, categoryId, minPrice, maxPrice);
    }

    private List<Product> filterDefaultProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                String sortBy, String sortOrder, int offset, int limit) {
        List<Product> stream = new ArrayList<>(getDefaultProducts());
        return stream.stream()
                .filter(p -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.trim().toLowerCase();
                        boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(kw);
                        boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(kw);
                        if (!matchName && !matchDesc) return false;
                    }
                    if (categoryId != null && categoryId > 0) {
                        if (p.getCategoryId() != categoryId) return false;
                    }
                    if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
                        if (p.getPrice().compareTo(minPrice) < 0) return false;
                    }
                    if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                        if (p.getPrice().compareTo(maxPrice) > 0) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    boolean asc = "ASC".equalsIgnoreCase(sortOrder);
                    if ("p.price".equalsIgnoreCase(sortBy)) {
                        return asc ? a.getPrice().compareTo(b.getPrice()) : b.getPrice().compareTo(a.getPrice());
                    }
                    if ("p.name".equalsIgnoreCase(sortBy)) {
                        return asc ? a.getName().compareToIgnoreCase(b.getName()) : b.getName().compareToIgnoreCase(a.getName());
                    }
                    return asc ? Integer.compare(a.getId(), b.getId()) : Integer.compare(b.getId(), a.getId());
                })
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit))
                .toList();
    }

    private int countFilterDefaultProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return (int) getDefaultProducts().stream()
                .filter(p -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.trim().toLowerCase();
                        boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(kw);
                        boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(kw);
                        if (!matchName && !matchDesc) return false;
                    }
                    if (categoryId != null && categoryId > 0) {
                        if (p.getCategoryId() != categoryId) return false;
                    }
                    if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
                        if (p.getPrice().compareTo(minPrice) < 0) return false;
                    }
                    if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                        if (p.getPrice().compareTo(maxPrice) > 0) return false;
                    }
                    return true;
                })
                .count();
    }

    public static List<Product> getDefaultProducts() {
        List<Product> defaults = new ArrayList<>();
        defaults.add(new Product(1, 1, "ProBook Ultra 16-inch M3", "probook-ultra-16-m3",
                "Flagship workstation with 32GB RAM, 1TB NVMe SSD, ultra-bright 120Hz Liquid Retina display, and 18-hour battery life.",
                new BigDecimal("149999.00"), 15, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Electronics", 4.9, 12));

        defaults.add(new Product(2, 1, "Galaxy S25 Ultra 512GB", "galaxy-s25-ultra-512gb",
                "Next-gen flagship smartphone with 200MP AI camera, titanium frame, S-Pen included, and dynamic AMOLED 2X display.",
                new BigDecimal("119999.00"), 25, "https://images.unsplash.com/photo-1738830234395-a351829a1c7b?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Electronics", 4.8, 18));

        defaults.add(new Product(3, 1, "OLED Smart 4K Gaming Monitor 32\"", "oled-smart-4k-gaming-monitor-32",
                "Blazing fast 240Hz refresh rate, 0.03ms response time, HDR TrueBlack 400, and integrated smart streaming apps.",
                new BigDecimal("64999.00"), 8, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Electronics", 4.7, 9));

        defaults.add(new Product(4, 2, "Classic Merino Wool Trench Coat", "classic-merino-wool-trench-coat",
                "Handcrafted tailored coat made with 100% sustainably sourced Merino wool. Thermal lining and water-resistant finish.",
                new BigDecimal("18999.00"), 40, "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Fashion & Apparel", 4.9, 15));

        defaults.add(new Product(5, 2, "Heritage Leather Weekender Bag", "heritage-leather-weekender-bag",
                "Full-grain Italian leather duffel bag with brass hardware, detachable shoulder strap, and dedicated shoe compartment.",
                new BigDecimal("12499.00"), 30, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Fashion & Apparel", 4.6, 8));

        defaults.add(new Product(6, 2, "Apex Minimalist Running Sneakers", "apex-minimalist-running-sneakers",
                "Featherweight breathable mesh sneakers with responsive foam cushion soles and high-traction rubber outsoles.",
                new BigDecimal("7999.00"), 50, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Fashion & Apparel", 4.7, 21));

        defaults.add(new Product(7, 3, "Barista Touch Espresso Machine", "barista-touch-espresso-machine",
                "Automated touch-screen espresso maker with integrated burr grinder, digital temperature control, and micro-foam steam wand.",
                new BigDecimal("49999.00"), 12, "https://images.pexels.com/photos/12122684/pexels-photo-12122684.jpeg?auto=compress&cs=tinysrgb&w=800",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Home & Kitchen", 4.9, 14));

        defaults.add(new Product(8, 3, "Cast Iron Dutch Oven 6-Quart", "cast-iron-dutch-oven-6-quart",
                "Enamelled heavy-duty cast iron pot ideal for slow cooking, braising, baking sourdough bread, and roasting.",
                new BigDecimal("6499.00"), 22, "https://images.pexels.com/photos/29784333/pexels-photo-29784333.jpeg?auto=compress&cs=tinysrgb&w=800",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Home & Kitchen", 4.8, 11));

        defaults.add(new Product(9, 4, "Aura ANC Wireless Studio Headphones", "aura-anc-wireless-studio-headphones",
                "Active noise cancellation with spatial audio, high-resolution 40mm neodymium drivers, and plush memory foam earcups.",
                new BigDecimal("24999.00"), 35, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Audio & Accessories", 4.9, 32));

        defaults.add(new Product(10, 4, "Pulse 360 Waterproof Bluetooth Speaker", "pulse-360-waterproof-bluetooth-speaker",
                "IPX7 waterproof portable speaker with rich 360-degree deep bass, ambient LED light ring, and 24-hour battery.",
                new BigDecimal("5999.00"), 45, "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Audio & Accessories", 4.5, 7));

        defaults.add(new Product(11, 5, "FitPulse Smart GPS Activity Watch", "fitpulse-smart-gps-activity-watch",
                "Continuous heart rate monitor, multi-band GPS, VO2 max tracking, sleep score analysis, and 50m water resistance.",
                new BigDecimal("14999.00"), 20, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Sports & Fitness", 4.8, 19));

        defaults.add(new Product(12, 5, "EcoGrip Pro Non-Slip Yoga Mat", "ecogrip-pro-non-slip-yoga-mat",
                "Extra-thick 6mm natural tree rubber yoga mat with alignment guide lines and antimicrobial surface.",
                new BigDecimal("2999.00"), 60, "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Sports & Fitness", 4.6, 6));

        // Category 1 - Electronics
        defaults.add(new Product(13, 1, "iPad Pro 11-inch M4 256GB", "ipad-pro-11-m4-256gb",
                "Ultra Retina XDR OLED display, Apple M4 chip with 10-core GPU, Pro camera system with LiDAR scanner, and Thunderbolt / USB 4 support.",
                new BigDecimal("99999.00"), 20, "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Electronics", 4.9, 27));

        defaults.add(new Product(14, 1, "Sony Alpha 7 IV Mirrorless Camera", "sony-alpha-7-iv-mirrorless-camera",
                "33MP full-frame Exmor R CMOS sensor, 4K 60p 10-bit 4:2:2 recording, real-time eye AF for human/animal/bird, and 5-axis image stabilization.",
                new BigDecimal("214999.00"), 10, "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Electronics", 4.8, 16));

        // Category 2 - Fashion & Apparel
        defaults.add(new Product(15, 2, "Italian Silk Jacquard Tuxedo Blazer", "italian-silk-jacquard-tuxedo-blazer",
                "Handcrafted from pure Italian mulberry silk jacquard with satin peak lapels, tailored modern fit, and breathable cupro lining.",
                new BigDecimal("24999.00"), 18, "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Fashion & Apparel", 4.8, 14));

        defaults.add(new Product(16, 2, "Handcrafted Suede Chelsea Boots", "handcrafted-suede-chelsea-boots",
                "Waterproof Italian calf suede with Goodyear welted rubber commando sole, dual elastic goring, and cushioned ortholite insole.",
                new BigDecimal("8999.00"), 35, "https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Fashion & Apparel", 4.7, 22));

        // Category 3 - Home & Kitchen
        defaults.add(new Product(17, 3, "Smart Vortex Dual-Basket Air Fryer 8.5L", "smart-vortex-dual-basket-air-fryer-8-5l",
                "SyncCook and SyncFinish dual independent cooking zones, 360-degree rapid heat circulation, 8 preset cooking programs, and dishwasher-safe crisper trays.",
                new BigDecimal("11999.00"), 28, "https://plus.unsplash.com/premium_photo-1672192166833-c8ae84e5e127?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Home & Kitchen", 4.8, 31));

        defaults.add(new Product(18, 3, "Ergonomic High-Back Executive Mesh Chair", "ergonomic-high-back-executive-mesh-chair",
                "Breathable German elastomeric mesh with 4D adjustable armrests, dynamic lumbar support, tilt-lock recline, and heavy-duty aluminum base.",
                new BigDecimal("16999.00"), 15, "https://images.unsplash.com/photo-1589384267710-7a170981ca78?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Home & Kitchen", 4.9, 25));

        defaults.add(new Product(19, 3, "Precision Gooseneck Electric Kettle 0.9L", "precision-gooseneck-electric-kettle",
                "Variable degree temperature control with digital LCD display, 60-minute keep-warm mode, count-up brew stopwatch, and barista pour spout.",
                new BigDecimal("4499.00"), 40, "https://images.pexels.com/photos/29831518/pexels-photo-29831518.jpeg?auto=compress&cs=tinysrgb&w=800",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Home & Kitchen", 4.7, 19));

        // Category 4 - Audio & Accessories
        defaults.add(new Product(20, 4, "Studio Pro Dynamic Broadcast Microphone", "studio-pro-dynamic-broadcast-microphone",
                "Broadcast-grade cardioid dynamic mic with built-in pop filter, shock isolation, dual XLR and USB-C outputs, and 24-bit/96kHz recording clarity.",
                new BigDecimal("12499.00"), 25, "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Audio & Accessories", 4.9, 42));

        defaults.add(new Product(21, 4, "SoundTrue ANC Wireless Earbuds", "soundtrue-anc-wireless-earbuds",
                "Hybrid active noise cancellation, transparency mode, custom high-excursion driver, wireless charging case, and 32 hours total playtime.",
                new BigDecimal("8999.00"), 55, "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Audio & Accessories", 4.7, 38));

        defaults.add(new Product(22, 4, "CinemaWave Dolby Atmos 5.1ch Soundbar", "cinemawave-dolby-atmos-soundbar",
                "450W output with wireless 8-inch subwoofer, up-firing Dolby Atmos height speakers, 4K HDR eARC passthrough, and Bluetooth 5.3 streaming.",
                new BigDecimal("34999.00"), 12, "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Audio & Accessories", 4.8, 17));

        // Category 5 - Sports & Fitness
        defaults.add(new Product(23, 5, "Apex Pro Quick-Select Dumbbell 24kg", "apex-pro-adjustable-dumbbell-24kg",
                "15 weight increments in one compact dumbbell (2.5kg to 24kg), smooth dial selection mechanism, and durable silicone-coated steel plates.",
                new BigDecimal("18999.00"), 20, "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Sports & Fitness", 4.9, 33));

        defaults.add(new Product(24, 5, "HyperRelief Deep-Tissue Percussion Massage Gun", "hyperrelief-percussion-massage-gun",
                "Brushless high-torque motor delivering 3200 percussions per minute, 6 swappable massage heads, 20-speed digital touch control, and 6-hour battery.",
                new BigDecimal("6999.00"), 30, "https://images.unsplash.com/photo-1746278925416-9d6c71f55c2d?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Sports & Fitness", 4.8, 29));

        defaults.add(new Product(25, 5, "HydraShield Vacuum Insulated Sports Bottle 1.2L", "hydrashield-vacuum-sports-bottle-1-2l",
                "Triple-wall copper insulated 18/8 food-grade stainless steel bottle. Keeps drinks cold for 36 hours or hot for 18 hours with leakproof chug lid.",
                new BigDecimal("1699.00"), 75, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&q=80",
                true, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Sports & Fitness", 4.9, 54));

        for (Product p : defaults) {
            p.setImages(getGalleryImagesForProduct(p.getId(), p.getImageUrl()));
        }

        return defaults;
    }

    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (category_id, name, slug, description, price, stock_quantity, image_url, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getSlug());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getPrice());
            ps.setInt(6, product.getStockQuantity());
            ps.setString(7, product.getImageUrl());
            ps.setBoolean(8, product.isActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating product: " + product.getName(), e);
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET category_id = ?, name = ?, slug = ?, description = ?, price = ?, stock_quantity = ?, image_url = ?, active = ? " +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getSlug());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getPrice());
            ps.setInt(6, product.getStockQuantity());
            ps.setString(7, product.getImageUrl());
            ps.setBoolean(8, product.isActive());
            ps.setInt(9, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product id: " + product.getId(), e);
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product id: " + id, e);
            return false;
        }
    }

    public boolean updateStockQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating stock for product: " + productId, e);
            return false;
        }
    }

    /**
     * Deducts stock atomically inside a transaction.
     * Prevents negative stock via SQL condition "AND stock_quantity >= ?".
     */
    public boolean deductStockInTransaction(Connection conn, int productId, int quantityToDeduct) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityToDeduct);
            ps.setInt(2, productId);
            ps.setInt(3, quantityToDeduct);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Restores stock atomically inside a transaction when an order is cancelled.
     */
    public boolean restoreStockInTransaction(Connection conn, int productId, int quantityToRestore) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityToRestore);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Restores stock in in-memory fallback list.
     */
    public void restoreStockDirect(int productId, int quantityToRestore) {
        getDefaultProducts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst()
                .ifPresent(p -> p.setStockQuantity(p.getStockQuantity() + quantityToRestore));
    }

    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.category_id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.image_url, p.active, p.created_at, p.updated_at, " +
                     "c.name AS category_name, 0 AS avg_rating, 0 AS review_count " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.stock_quantity <= ? ORDER BY p.stock_quantity ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching low stock products", e);
        }
        return list;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setName(rs.getString("name"));
        p.setSlug(rs.getString("slug"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));

        p.setCategoryName(rs.getString("category_name"));
        p.setAverageRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        p.setImages(getGalleryImagesForProduct(p.getId(), p.getImageUrl()));
        return p;
    }

    public static List<String> getGalleryImagesForProduct(int productId, String fallbackImg) {
        return switch (productId) {
            case 1 -> List.of(
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80",
                    "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&q=80",
                    "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80"
            );
            case 2 -> List.of(
                    "https://images.unsplash.com/photo-1738830234395-a351829a1c7b?w=800&q=80",
                    "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80",
                    "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=800&q=80"
            );
            case 3 -> List.of(
                    "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&q=80",
                    "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                    "https://images.unsplash.com/photo-1593642702821-c8da6771f0c6?w=800&q=80"
            );
            case 4 -> List.of(
                    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800&q=80",
                    "https://images.unsplash.com/photo-1544441893-675973e31985?w=800&q=80",
                    "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=800&q=80"
            );
            case 5 -> List.of(
                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
                    "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80",
                    "https://images.unsplash.com/photo-1577733966973-d680bffd2e80?w=800&q=80"
            );
            case 6 -> List.of(
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
                    "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800&q=80",
                    "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&q=80"
            );
            case 7 -> List.of(
                    "https://images.pexels.com/photos/12122684/pexels-photo-12122684.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/3936163/pexels-photo-3936163.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg?auto=compress&cs=tinysrgb&w=800"
            );
            case 8 -> List.of(
                    "https://images.pexels.com/photos/29784333/pexels-photo-29784333.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/1132560/pexels-photo-1132560.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/5764069/pexels-photo-5764069.jpeg?auto=compress&cs=tinysrgb&w=800"
            );
            case 9 -> List.of(
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
                    "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&q=80",
                    "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&q=80"
            );
            case 10 -> List.of(
                    "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80",
                    "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&q=80",
                    "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800&q=80"
            );
            case 11 -> List.of(
                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80",
                    "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80",
                    "https://images.unsplash.com/photo-1508615039623-a25605d2b022?w=800&q=80"
            );
            case 12 -> List.of(
                    "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&q=80",
                    "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=800&q=80",
                    "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&q=80"
            );
            case 13 -> List.of(
                    "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&q=80",
                    "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800&q=80",
                    "https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=800&q=80"
            );
            case 14 -> List.of(
                    "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80",
                    "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&q=80",
                    "https://images.unsplash.com/photo-1512790182412-b19e6d62bc39?w=800&q=80"
            );
            case 15 -> List.of(
                    "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&q=80",
                    "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80",
                    "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=800&q=80"
            );
            case 16 -> List.of(
                    "https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=800&q=80",
                    "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=800&q=80",
                    "https://images.unsplash.com/photo-1582588678413-dbf45f4823e9?w=800&q=80"
            );
            case 17 -> List.of(
                    "https://plus.unsplash.com/premium_photo-1672192166833-c8ae84e5e127?w=800&q=80",
                    "https://images.pexels.com/photos/35285814/pexels-photo-35285814.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/4099467/pexels-photo-4099467.jpeg?auto=compress&cs=tinysrgb&w=800"
            );
            case 18 -> List.of(
                    "https://images.unsplash.com/photo-1589384267710-7a170981ca78?w=800&q=80",
                    "https://images.pexels.com/photos/1957478/pexels-photo-1957478.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/7005296/pexels-photo-7005296.jpeg?auto=compress&cs=tinysrgb&w=800"
            );
            case 19 -> List.of(
                    "https://images.pexels.com/photos/29831518/pexels-photo-29831518.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/30693514/pexels-photo-30693514.jpeg?auto=compress&cs=tinysrgb&w=800",
                    "https://images.pexels.com/photos/6373307/pexels-photo-6373307.jpeg?auto=compress&cs=tinysrgb&w=800"
            );
            case 20 -> List.of(
                    "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&q=80",
                    "https://images.unsplash.com/photo-1583244532610-2a234e7c3eca?w=800&q=80",
                    "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&q=80"
            );
            case 21 -> List.of(
                    "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=800&q=80",
                    "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb?w=800&q=80",
                    "https://images.unsplash.com/photo-1598331668826-20cecc596b86?w=800&q=80"
            );
            case 22 -> List.of(
                    "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&q=80",
                    "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80",
                    "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&q=80"
            );
            case 23 -> List.of(
                    "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800&q=80",
                    "https://images.unsplash.com/photo-1586401100295-7a8096fd231a?w=800&q=80",
                    "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&q=80"
            );
            case 24 -> List.of(
                    "https://images.unsplash.com/photo-1746278925416-9d6c71f55c2d?w=800&q=80",
                    "https://plus.unsplash.com/premium_photo-1661721529633-db1a51e1d9bc?w=800&q=80",
                    "https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800&q=80"
            );
            case 25 -> List.of(
                    "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&q=80",
                    "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=800&q=80",
                    "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80"
            );
            default -> (fallbackImg != null && !fallbackImg.isBlank()) ? List.of(fallbackImg) : List.of();
        };
    }
}
