-- Script cập nhật các sản phẩm để hiển thị trên trang Home
-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio

-- Đánh dấu một số sản phẩm là Featured (sản phẩm nổi bật)
UPDATE TOP (8) products 
SET is_featured = 1 
WHERE is_active = 1 
ORDER BY NEWID();

-- Đánh dấu một số sản phẩm là New (sản phẩm mới)
UPDATE TOP (8) products 
SET is_new = 1 
WHERE is_active = 1 
ORDER BY created_at DESC;

-- Đánh dấu một số sản phẩm là Best Seller
UPDATE TOP (8) products 
SET is_best_seller = 1 
WHERE is_active = 1 AND sold_count > 0 
ORDER BY sold_count DESC;

-- Nếu không có sản phẩm nào có sold_count > 0, đánh dấu random
UPDATE TOP (8) products 
SET is_best_seller = 1 
WHERE is_active = 1 AND is_best_seller = 0 
ORDER BY NEWID();

-- Đánh dấu một số categories là Featured (danh mục nổi bật)
WITH TopCategories AS (
    SELECT TOP (6) c.id -- <-- Thay 'id' bằng tên cột khóa chính của bảng categories nếu khác
    FROM categories c
    LEFT JOIN products p ON c.id = p.category_id -- <-- Thay 'category_id' bằng khóa ngoại trong bảng products
    WHERE c.is_active = 1 
    GROUP BY c.id
    ORDER BY COUNT(p.id) DESC
)
UPDATE categories 
SET is_featured = 1 
WHERE id IN (SELECT id FROM TopCategories);

-- Kiểm tra kết quả
SELECT 'Featured Products' as Type, COUNT(*) as Count FROM products WHERE is_featured = 1 AND is_active = 1
UNION ALL
SELECT 'New Products' as Type, COUNT(*) as Count FROM products WHERE is_new = 1 AND is_active = 1
UNION ALL
SELECT 'Best Seller Products' as Type, COUNT(*) as Count FROM products WHERE is_best_seller = 1 AND is_active = 1
UNION ALL
SELECT 'Featured Categories' as Type, COUNT(*) as Count FROM categories WHERE is_featured = 1 AND is_active = 1;
