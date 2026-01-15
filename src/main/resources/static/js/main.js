/**
 * ARGATY - Main JavaScript
 * Cosmic Gaming Gear E-commerce
 */

// ========== CSRF TOKEN ==========

// Get CSRF token from cookie
function getCsrfToken() {
  const name = "XSRF-TOKEN=";
  const decodedCookie = decodeURIComponent(document.cookie);
  const ca = decodedCookie.split(";");
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === " ") {
      c = c.substring(1);
    }
    if (c.indexOf(name) === 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

// Helper function for fetch with CSRF token
async function fetchWithCsrf(url, options = {}) {
  const defaultHeaders = {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": getCsrfToken(),
  };

  options.headers = { ...defaultHeaders, ...options.headers };
  return fetch(url, options);
}

// ========== GLOBAL UTILITIES ==========

// Format currency VND
function formatCurrency(amount) {
  return new Intl.NumberFormat("vi-VN").format(amount) + " ₫";
}

// Show toast notification
function showToast(type, title, message, duration = 4000) {
  const container = document.getElementById("toast-container");
  if (!container) return;

  const icons = {
    success: "bx-check-circle",
    error: "bx-error-circle",
    warning: "bx-error",
    info: "bx-info-circle",
  };

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
        <i class='bx ${icons[type]} toast-icon'></i>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class='bx bx-x'></i>
        </button>
    `;

  container.appendChild(toast);

  // Trigger animation
  setTimeout(() => toast.classList.add("show"), 10);

  // Auto remove
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// Update cart count in header
async function updateCartCount() {
  try {
    const response = await fetch("/api/cart/count");
    const data = await response.json();

    if (data.success) {
      const badges = document.querySelectorAll(
        ".header__action-btn .badge-count"
      );
      badges.forEach((badge) => {
        if (
          badge.closest('a[href="/cart"]') ||
          badge.closest('a[href*="cart"]')
        ) {
          if (data.data > 0) {
            badge.textContent = data.data;
            badge.style.display = "flex";
          } else {
            badge.style.display = "none";
          }
        }
      });
    }
  } catch (error) {
    console.error("Error updating cart count:", error);
  }
}

// ========== HEADER ==========

// Header scroll effect
function initHeader() {
  const header = document.getElementById("header");
  if (!header) return;

  let lastScroll = 0;

  window.addEventListener("scroll", () => {
    const currentScroll = window.pageYOffset;

    // Add scrolled class
    if (currentScroll > 50) {
      header.classList.add("scrolled");
    } else {
      header.classList.remove("scrolled");
    }

    // Hide/show on scroll (optional)
    // if (currentScroll > lastScroll && currentScroll > 200) {
    //     header. style.transform = 'translateY(-100%)';
    // } else {
    //     header.style.transform = 'translateY(0)';
    // }

    lastScroll = currentScroll;
  });
}

// Mobile menu
function initMobileMenu() {
  const toggle = document.getElementById("mobileMenuToggle");
  const nav = document.querySelector(".header__nav");

  if (!toggle || !nav) return;

  toggle.addEventListener("click", () => {
    nav.classList.toggle("active");
    toggle.classList.toggle("active");
  });
}

// ========== WISHLIST ==========

// Toggle wishlist
async function toggleWishlist(productId, button) {
  try {
    const response = await fetchWithCsrf(`/api/wishlist/${productId}/toggle`, {
      method: "POST",
    });

    const data = await response.json();

    if (data.success) {
      const isInWishlist = data.data;
      const icon = button.querySelector("i");

      if (isInWishlist) {
        button.classList.add("active");
        icon.classList.remove("bx-heart");
        icon.classList.add("bxs-heart");
      } else {
        button.classList.remove("active");
        icon.classList.remove("bxs-heart");
        icon.classList.add("bx-heart");
      }

      showToast("success", "Thành công", data.message);
    } else {
      if (response.status === 401) {
        showToast(
          "warning",
          "Cảnh báo",
          "Vui lòng đăng nhập để sử dụng tính năng này"
        );
        setTimeout(() => {
          window.location.href =
            "/auth/login? redirect=" +
            encodeURIComponent(window.location.pathname);
        }, 1500);
      } else {
        showToast("error", "Lỗi", data.message);
      }
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể cập nhật yêu thích");
  }
}

// Init wishlist buttons
function initWishlistButtons() {
  document.querySelectorAll(".wishlist-btn").forEach((btn) => {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      const productId = this.dataset.productId;
      toggleWishlist(productId, this);
    });
  });
}

// ========== ADD TO CART ==========

// Quick add to cart
async function quickAddToCart(productId, quantity = 1, variantId = null) {
  try {
    const response = await fetchWithCsrf("/api/cart/items", {
      method: "POST",
      body: JSON.stringify({
        productId: productId,
        variantId: variantId,
        quantity: quantity,
      }),
    });

    const data = await response.json();

    if (data.success) {
      showToast(
        "success",
        "Thành công",
        data.message || "Đã thêm vào giỏ hàng"
      );
      updateCartCount();
    } else {
      if (response.status === 401) {
        showToast("warning", "Cảnh báo", "Vui lòng đăng nhập để mua hàng");
      } else {
        showToast(
          "error",
          "Lỗi",
          data.message || "Không thể thêm vào giỏ hàng"
        );
      }
    }
  } catch (error) {
    showToast("error", "Lỗi", "Đã có lỗi xảy ra");
  }
}

// Init add to cart buttons
function initAddToCartButtons() {
  document.querySelectorAll(".add-to-cart-btn").forEach((btn) => {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      const productId = this.dataset.productId;
      quickAddToCart(productId);
    });
  });
}

// ========== SEARCH ==========

// Search autocomplete
function initSearchAutocomplete() {
  const searchInput = document.querySelector(".header__search-input");
  if (!searchInput) return;

  let debounceTimer;
  let suggestionBox;

  searchInput.addEventListener("input", function () {
    const query = this.value.trim();

    clearTimeout(debounceTimer);

    if (query.length < 2) {
      closeSuggestions();
      return;
    }

    debounceTimer = setTimeout(() => {
      fetchSuggestions(query);
    }, 300);
  });

  searchInput.addEventListener("focus", function () {
    if (this.value.trim().length >= 2) {
      fetchSuggestions(this.value.trim());
    }
  });

  document.addEventListener("click", function (e) {
    if (!e.target.closest(".header__search")) {
      closeSuggestions();
    }
  });

  async function fetchSuggestions(query) {
    try {
      const response = await fetch(
        `/api/products/search/suggestions?q=${encodeURIComponent(query)}`
      );
      const data = await response.json();

      if (data.success && data.data.length > 0) {
        showSuggestions(data.data);
      } else {
        closeSuggestions();
      }
    } catch (error) {
      console.error("Search error:", error);
    }
  }

  function showSuggestions(products) {
    closeSuggestions();

    suggestionBox = document.createElement("div");
    suggestionBox.className = "search-suggestions";
    suggestionBox.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius:  var(--border-radius);
            margin-top: 8px;
            max-height:  400px;
            overflow-y:  auto;
            z-index:  1000;
            backdrop-filter: blur(20px);
        `;

    products.forEach((product) => {
      const item = document.createElement("a");
      item.href = `/products/${product.slug}`;
      item.className = "search-suggestion-item";
      item.style.cssText = `
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 12px 16px;
                color: var(--text-primary);
                transition: background 0.2s;
            `;
      item.innerHTML = `
                <img src="${
                  product.mainImage || "/images/no-image.png"
                }" alt="" 
                     style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px;">
                <div style="flex:  1; min-width: 0;">
                    <div style="font-weight: 500; white-space: nowrap; overflow:  hidden; text-overflow: ellipsis;">
                        ${product.name}
                    </div>
                    <div style="color: var(--accent-cyan); font-weight: 600;">
                        ${formatCurrency(product.salePrice || product.price)}
                    </div>
                </div>
            `;

      item.addEventListener("mouseenter", () => {
        item.style.background = "rgba(139, 92, 246, 0.1)";
      });
      item.addEventListener("mouseleave", () => {
        item.style.background = "transparent";
      });

      suggestionBox.appendChild(item);
    });

    searchInput.parentElement.style.position = "relative";
    searchInput.parentElement.appendChild(suggestionBox);
  }

  function closeSuggestions() {
    if (suggestionBox) {
      suggestionBox.remove();
      suggestionBox = null;
    }
  }
}

// ========== ANIMATIONS ==========

// Intersection Observer for scroll animations
function initScrollAnimations() {
  // Check if IntersectionObserver is supported
  if (!('IntersectionObserver' in window)) {
    return; // Do nothing if not supported
  }

  const observerOptions = {
    root: null,
    rootMargin: "0px",
    threshold: 0, // Changed to 0 to trigger as soon as any part is visible
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add("animate-in");
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  // Set initial state via class instead of inline style
  // to avoid specificity issues and provide fallback
  const style = document.createElement("style");
  style.textContent = `
      .scroll-hidden {
          opacity: 0;
          transform: translateY(20px);
          transition: opacity 0.5s ease, transform 0.5s ease;
      }
      .animate-in {
          opacity: 1 !important;
          transform: translateY(0) !important;
      }
  `;
  document.head.appendChild(style);

  document
    .querySelectorAll(".product-card, .category-card, .card")
    .forEach((el) => {
      el.classList.add("scroll-hidden");
      observer.observe(el);
    });
}
// Remove old style injection
// Add animate-in styles (REMOVED - moved inside function)

// ========== MODAL ==========

// Open modal
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("active");
    document.body.style.overflow = "hidden";
  }
}

// Close modal
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("active");
    document.body.style.overflow = "";
  }
}

// Init modals
function initModals() {
  // Close on overlay click
  document.querySelectorAll(".modal-overlay").forEach((overlay) => {
    overlay.addEventListener("click", function (e) {
      if (e.target === this) {
        this.classList.remove("active");
        document.body.style.overflow = "";
      }
    });
  });

  // Close on ESC key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      document.querySelectorAll(".modal-overlay. active").forEach((modal) => {
        modal.classList.remove("active");
      });
      document.body.style.overflow = "";
    }
  });
}

// ========== LAZY LOADING ==========

function initLazyLoading() {
  const lazyImages = document.querySelectorAll("img[data-src]");

  const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        img.removeAttribute("data-src");
        imageObserver.unobserve(img);
      }
    });
  });

  lazyImages.forEach((img) => imageObserver.observe(img));
}

// ========== BACK TO TOP ==========

function initBackToTop() {
  const btn = document.createElement("button");
  btn.id = "backToTop";
  btn.className = "btn btn-primary btn-icon";
  btn.innerHTML = '<i class="bx bx-up-arrow-alt"></i>';
  btn.style.cssText = `
        position: fixed;
        bottom: 30px;
        right: 30px;
        z-index: 999;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s ease;
    `;

  document.body.appendChild(btn);

  window.addEventListener("scroll", () => {
    if (window.pageYOffset > 500) {
      btn.style.opacity = "1";
      btn.style.visibility = "visible";
    } else {
      btn.style.opacity = "0";
      btn.style.visibility = "hidden";
    }
  });

  btn.addEventListener("click", () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  });
}

// ========== INITIALIZE ==========

document.addEventListener("DOMContentLoaded", function () {
  initHeader();
  initMobileMenu();
  initWishlistButtons();
  initAddToCartButtons();
  initSearchAutocomplete();
  initScrollAnimations();
  initModals();
  initLazyLoading();
  initBackToTop();

  // Update cart count on page load
  updateCartCount();
});

// Export functions for global use
window.showToast = showToast;
window.updateCartCount = updateCartCount;
window.formatCurrency = formatCurrency;
window.openModal = openModal;
window.closeModal = closeModal;
window.quickAddToCart = quickAddToCart;
