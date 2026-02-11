package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Brand;

/**
 * Repository cho Brand Entity
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    // ========== CHECK EXIST ==========
    boolean existsByName(String name);
    boolean existsBySlug(String slug);

    // ========== FIND SINGLE ==========
    Optional<Brand> findBySlug(String slug);
    Optional<Brand> findByName(String name);

    // ========== FIND LIST / PAGE ==========
    
    // Tìm danh sách Active để hiển thị ở trang chủ (Sort theo thứ tự)
    List<Brand> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Tìm kiếm (Khớp với dòng gọi trong BrandServiceImpl)
    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ========== COUNT ==========
    
    // Đếm số lượng Active (Khớp với dòng gọi trong BrandServiceImpl)
    // Spring Data JPA tự động hiểu, không cần @Query
    long countByIsActiveTrue();

    // ========== CUSTOM QUERY ==========

    // Tìm những brand có ít nhất 1 sản phẩm đang active
    // Dùng subquery (EXISTS) an toàn hơn JOIN vì không phụ thuộc vào mapping OneToMany trong Entity
    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND " +
           "EXISTS (SELECT p FROM Product p WHERE p.brand = b AND p.isActive = true)")
    List<Brand> findBrandsWithActiveProducts();

    // (Tùy chọn) Nếu bạn vẫn muốn dùng searchBrands phức tạp (tìm cả description)
    // thì giữ lại, nhưng phải sửa Service gọi hàm này thay vì findByNameContainingIgnoreCase.
    // Tạm thời mình ẩn đi để code Service chạy được ngay.
    /*
    @Query("SELECT b FROM Brand b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Brand> searchBrands(@Param("keyword") String keyword, Pageable pageable);
    */
}