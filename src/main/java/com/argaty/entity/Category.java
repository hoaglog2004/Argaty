package com.argaty.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity Category - Danh mục sản phẩm (hỗ trợ nested categories)
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    // ========== RELATIONSHIPS ==========

    /**
     * Danh mục cha (self-referencing)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Danh sách danh mục con
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    /**
     * Danh sách sản phẩm thuộc danh mục
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra có phải danh mục gốc không
     */
    public boolean isRootCategory() {
        return parent == null;
    }

    /**
     * Kiểm tra có danh mục con không
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Lấy số lượng sản phẩm
     */
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }

    /**
     * Lấy đường dẫn đầy đủ của danh mục
     * VD: "Bàn phím > Bàn phím cơ"
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }
}