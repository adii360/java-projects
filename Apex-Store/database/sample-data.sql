-- =====================================================================
-- E-Commerce Sample Data
-- Database: ecommerce_db
-- =====================================================================

USE ecommerce_db;

-- 1. Sample Categories
INSERT INTO categories (id, name, slug, description, image_url) VALUES
(1, 'Electronics', 'electronics', 'High performance laptops, smartphones, headphones, and modern gadgets.', 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=600&q=80'),
(2, 'Fashion & Apparel', 'fashion-apparel', 'Trending apparel, footwear, accessories, and designer essentials.', 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80'),
(3, 'Home & Kitchen', 'home-kitchen', 'Smart home appliances, modern ergonomic furniture, and premium kitchenware.', 'https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600&q=80'),
(4, 'Audio & Accessories', 'audio-accessories', 'Noise-canceling headphones, Bluetooth speakers, and studio microphones.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80'),
(5, 'Sports & Fitness', 'sports-fitness', 'Workout gear, smart wearables, yoga mats, and athletic essentials.', 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&q=80');

-- 2. Sample Products
INSERT INTO products (id, category_id, name, slug, description, price, stock_quantity, image_url, active) VALUES
-- Electronics
(1, 1, 'ProBook Ultra 16-inch M3', 'probook-ultra-16-m3', 'Flagship workstation with 32GB RAM, 1TB NVMe SSD, ultra-bright 120Hz Liquid Retina display, and 18-hour battery life.', 149999.00, 15, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80', TRUE),
(2, 1, 'Galaxy S25 Ultra 512GB', 'galaxy-s25-ultra-512gb', 'Next-gen flagship smartphone with 200MP AI camera, titanium frame, S-Pen included, and dynamic AMOLED 2X display.', 119999.00, 25, 'https://images.unsplash.com/photo-1738830234395-a351829a1c7b?w=800&q=80', TRUE),
(3, 1, 'OLED Smart 4K Gaming Monitor 32"', 'oled-smart-4k-gaming-monitor-32', 'Blazing fast 240Hz refresh rate, 0.03ms response time, HDR TrueBlack 400, and integrated smart streaming apps.', 64999.00, 8, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&q=80', TRUE),

-- Fashion
(4, 2, 'Classic Merino Wool Trench Coat', 'classic-merino-wool-trench-coat', 'Handcrafted tailored coat made with 100% sustainably sourced Merino wool. Thermal lining and water-resistant finish.', 18999.00, 40, 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800&q=80', TRUE),
(5, 2, 'Heritage Leather Weekender Bag', 'heritage-leather-weekender-bag', 'Full-grain Italian leather duffel bag with brass hardware, detachable shoulder strap, and dedicated shoe compartment.', 12499.00, 30, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80', TRUE),
(6, 2, 'Apex Minimalist Running Sneakers', 'apex-minimalist-running-sneakers', 'Featherweight breathable mesh sneakers with responsive foam cushion soles and high-traction rubber outsoles.', 7999.00, 50, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', TRUE),

-- Home & Kitchen
(7, 3, 'Barista Touch Espresso Machine', 'barista-touch-espresso-machine', 'Automated touch-screen espresso maker with integrated burr grinder, digital temperature control, and micro-foam steam wand.', 49999.00, 12, 'https://images.pexels.com/photos/12122684/pexels-photo-12122684.jpeg?auto=compress&cs=tinysrgb&w=800', TRUE),
(8, 3, 'Cast Iron Dutch Oven 6-Quart', 'cast-iron-dutch-oven-6-quart', 'Enamelled heavy-duty cast iron pot ideal for slow cooking, braising, baking sourdough bread, and roasting.', 6499.00, 22, 'https://images.pexels.com/photos/29784333/pexels-photo-29784333.jpeg?auto=compress&cs=tinysrgb&w=800', TRUE),

-- Audio
(9, 4, 'Aura ANC Wireless Studio Headphones', 'aura-anc-wireless-studio-headphones', 'Active noise cancellation with spatial audio, high-resolution 40mm neodymium drivers, and plush memory foam earcups.', 24999.00, 35, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80', TRUE),
(10, 4, 'Pulse 360 Waterproof Bluetooth Speaker', 'pulse-360-waterproof-bluetooth-speaker', 'IPX7 waterproof portable speaker with rich 360-degree deep bass, ambient LED light ring, and 24-hour battery.', 5999.00, 45, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80', TRUE),

-- Sports & Fitness
(11, 5, 'FitPulse Smart GPS Activity Watch', 'fitpulse-smart-gps-activity-watch', 'Continuous heart rate monitor, multi-band GPS, VO2 max tracking, sleep score analysis, and 50m water resistance.', 14999.00, 20, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80', TRUE),
(12, 5, 'EcoGrip Pro Non-Slip Yoga Mat', 'ecogrip-pro-non-slip-yoga-mat', 'Extra-thick 6mm natural tree rubber yoga mat with alignment guide lines and antimicrobial surface.', 2999.00, 60, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&q=80', TRUE),

-- Electronics (Cont.)
(13, 1, 'iPad Pro 11-inch M4 256GB', 'ipad-pro-11-m4-256gb', 'Ultra Retina XDR OLED display, Apple M4 chip with 10-core GPU, Pro camera system with LiDAR scanner, and Thunderbolt / USB 4 support.', 99999.00, 20, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&q=80', TRUE),
(14, 1, 'Sony Alpha 7 IV Mirrorless Camera', 'sony-alpha-7-iv-mirrorless-camera', '33MP full-frame Exmor R CMOS sensor, 4K 60p 10-bit 4:2:2 recording, real-time eye AF for human/animal/bird, and 5-axis image stabilization.', 214999.00, 10, 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80', TRUE),

-- Fashion (Cont.)
(15, 2, 'Italian Silk Jacquard Tuxedo Blazer', 'italian-silk-jacquard-tuxedo-blazer', 'Handcrafted from pure Italian mulberry silk jacquard with satin peak lapels, tailored modern fit, and breathable cupro lining.', 24999.00, 18, 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&q=80', TRUE),
(16, 2, 'Handcrafted Suede Chelsea Boots', 'handcrafted-suede-chelsea-boots', 'Waterproof Italian calf suede with Goodyear welted rubber commando sole, dual elastic goring, and cushioned ortholite insole.', 8999.00, 35, 'https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=800&q=80', TRUE),

-- Home & Kitchen (Cont.)
(17, 3, 'Smart Vortex Dual-Basket Air Fryer 8.5L', 'smart-vortex-dual-basket-air-fryer-8-5l', 'SyncCook and SyncFinish dual independent cooking zones, 360-degree rapid heat circulation, 8 preset cooking programs, and dishwasher-safe crisper trays.', 11999.00, 28, 'https://plus.unsplash.com/premium_photo-1672192166833-c8ae84e5e127?w=800&q=80', TRUE),
(18, 3, 'Ergonomic High-Back Executive Mesh Chair', 'ergonomic-high-back-executive-mesh-chair', 'Breathable German elastomeric mesh with 4D adjustable armrests, dynamic lumbar support, tilt-lock recline, and heavy-duty aluminum base.', 16999.00, 15, 'https://images.unsplash.com/photo-1589384267710-7a170981ca78?w=800&q=80', TRUE),
(19, 3, 'Precision Gooseneck Electric Kettle 0.9L', 'precision-gooseneck-electric-kettle', 'Variable degree temperature control with digital LCD display, 60-minute keep-warm mode, count-up brew stopwatch, and barista pour spout.', 4499.00, 40, 'https://images.pexels.com/photos/29831518/pexels-photo-29831518.jpeg?auto=compress&cs=tinysrgb&w=800', TRUE),

-- Audio (Cont.)
(20, 4, 'Studio Pro Dynamic Broadcast Microphone', 'studio-pro-dynamic-broadcast-microphone', 'Broadcast-grade cardioid dynamic mic with built-in pop filter, shock isolation, dual XLR and USB-C outputs, and 24-bit/96kHz recording clarity.', 12499.00, 25, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&q=80', TRUE),
(21, 4, 'SoundTrue ANC Wireless Earbuds', 'soundtrue-anc-wireless-earbuds', 'Hybrid active noise cancellation, transparency mode, custom high-excursion driver, wireless charging case, and 32 hours total playtime.', 8999.00, 55, 'https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=800&q=80', TRUE),
(22, 4, 'CinemaWave Dolby Atmos 5.1ch Soundbar', 'cinemawave-dolby-atmos-soundbar', '450W output with wireless 8-inch subwoofer, up-firing Dolby Atmos height speakers, 4K HDR eARC passthrough, and Bluetooth 5.3 streaming.', 34999.00, 12, 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&q=80', TRUE),

-- Sports & Fitness (Cont.)
(23, 5, 'Apex Pro Quick-Select Dumbbell 24kg', 'apex-pro-adjustable-dumbbell-24kg', '15 weight increments in one compact dumbbell (2.5kg to 24kg), smooth dial selection mechanism, and durable silicone-coated steel plates.', 18999.00, 20, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800&q=80', TRUE),
(24, 5, 'HyperRelief Deep-Tissue Percussion Massage Gun', 'hyperrelief-percussion-massage-gun', 'Brushless high-torque motor delivering 3200 percussions per minute, 6 swappable massage heads, 20-speed digital touch control, and 6-hour battery.', 6999.00, 30, 'https://images.unsplash.com/photo-1746278925416-9d6c71f55c2d?w=800&q=80', TRUE),
(25, 5, 'HydraShield Vacuum Insulated Sports Bottle 1.2L', 'hydrashield-vacuum-sports-bottle-1-2l', 'Triple-wall copper insulated 18/8 food-grade stainless steel bottle. Keeps drinks cold for 36 hours or hot for 18 hours with leakproof chug lid.', 1699.00, 75, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&q=80', TRUE);

-- 3. Sample Coupons
INSERT INTO coupons (code, discount_type, discount_value, minimum_order_amount, expiry_date, active) VALUES
('WELCOME10', 'PERCENTAGE', 10.00, 999.00, '2028-12-31', TRUE),
('SAVE20', 'FIXED', 200.00, 1999.00, '2028-12-31', TRUE),
('MEGA50', 'FIXED', 500.00, 4999.00, '2028-12-31', TRUE),
('EXPIRED99', 'PERCENTAGE', 99.00, 999.00, '2022-01-01', FALSE);

-- =====================================================================
-- 4. Initial Users & Administrator Account Setup Instructions
-- =====================================================================
-- Note: Passwords below are hashed using BCrypt (rounds: 12).
-- Plaintext for both demo accounts: "Password@123"
-- BCrypt Hash: $2a$12$e8x5a3e.YJspZ6O1xG8HGeUqYp3bKk7q7j3l5oG0b9tq4o9yYh9wO
-- Alternatively, use the included AdminSetupCli / PasswordUtil to generate a unique hash.

INSERT INTO users (id, name, email, password_hash, phone, role) VALUES
(1, 'Administrator', 'admin@ecommerce.com', '$2a$10$3zR1iGv5X/6yWwJjZ8k5fehV30yK3J.u1q0v5Dq6QZ2Y5XwR6F2O.', '+1-800-555-0100', 'ADMIN'),
(2, 'Jane Customer', 'jane@example.com', '$2a$10$3zR1iGv5X/6yWwJjZ8k5fehV30yK3J.u1q0v5Dq6QZ2Y5XwR6F2O.', '+1-555-0199', 'CUSTOMER');

-- Sample address for demo customer
INSERT INTO addresses (id, user_id, recipient_name, phone, address_line, city, state, postal_code, country, is_default) VALUES
(1, 2, 'Jane Customer', '+1-555-0199', '742 Evergreen Terrace', 'Springfield', 'OR', '97477', 'United States', TRUE);

-- Sample reviews
INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(2, 1, 5, 'Superb build quality and incredible battery life! Extremely fast workstation for programming and creative tasks.'),
(2, 9, 4, 'Noise cancelling is top-tier. Soundstage is punchy and crisp.'),
(2, 13, 5, 'The OLED screen on this iPad Pro is mindblowing! Snappy performance with the M4 chip.'),
(2, 17, 5, 'Best air fryer I have ever owned. Dual zones make dinner prep twice as fast!'),
(2, 23, 5, 'Dial mechanism is rock solid. Saves so much space in my home gym.');

