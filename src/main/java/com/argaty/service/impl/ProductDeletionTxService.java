package com.argaty.service.impl;

import com.argaty.entity.Product;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.CartItemRepository;
import com.argaty.repository.OrderItemRepository;
import com.argaty.repository.ProductRepository;
import com.argaty.repository.ReviewRepository;
import com.argaty.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDeletionTxService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void hardDelete(Long id) {
        reviewRepository.deleteByProductId(id);
        cartItemRepository.deleteByProductId(id);
        wishlistRepository.deleteByProductId(id);
        orderItemRepository.deleteByProductId(id);

        productRepository.deleteById(id);
        productRepository.flush();
        log.info("Deleted product physically: {}", id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void softDelete(Long id) {
        Product productToDisable = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productToDisable.setIsActive(false);
        productToDisable.setIsFeatured(false);
        productToDisable.setIsNew(false);
        productToDisable.setIsBestSeller(false);
        productRepository.save(productToDisable);

        log.warn("Product {} referenced by other records, fallback to soft delete (inactive)", id);
    }
}
