// Cart JavaScript
/**
 * ARGATY - Cart JavaScript
 * Cart page functionality
 */

// ========== CART OPERATIONS ==========

// Update cart item quantity
async function updateCartItem(itemId, quantity) {
  try {
    const response = await fetch(`/api/cart/items/${itemId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quantity: quantity }),
    });

    const data = await response.json();

    if (data.success) {
      // Update UI
      updateCartUI(data.data);
      showToast("success", "Thành công", "Đã cập nhật giỏ hàng");
    } else {
      showToast("error", "Lỗi", data.message);
    }

    return data;
  } catch (error) {
    showToast("error", "Lỗi", "Không thể cập nhật giỏ hàng");
    return null;
  }
}

// Remove cart item
async function removeCartItem(itemId) {
  try {
    const response = await fetch(`/api/cart/items/${itemId}`, {
      method: "DELETE",
    });

    const data = await response.json();

    if (data.success) {
      // Remove from UI
      const itemEl = document.querySelector(
        `.cart-item[data-item-id="${itemId}"]`
      );
      if (itemEl) {
        itemEl.style.animation = "fadeOut 0.3s ease forwards";
        setTimeout(() => {
          itemEl.remove();
          checkEmptyCart();
        }, 300);
      }

      updateCartCount();
      showToast("success", "Thành công", "Đã xóa sản phẩm khỏi giỏ hàng");
    } else {
      showToast("error", "Lỗi", data.message);
    }

    return data;
  } catch (error) {
    showToast("error", "Lỗi", "Không thể xóa sản phẩm");
    return null;
  }
}

// Clear all cart items
async function clearCart() {
  if (!confirm("Bạn có chắc muốn xóa toàn bộ giỏ hàng?")) return;

  try {
    const response = await fetch("/api/cart", {
      method: "DELETE",
    });

    const data = await response.json();

    if (data.success) {
      location.reload();
    } else {
      showToast("error", "Lỗi", data.message);
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể xóa giỏ hàng");
  }
}

// Toggle item selection
async function toggleItemSelected(itemId) {
  try {
    const response = await fetch(`/api/cart/items/${itemId}/toggle`, {
      method: "PATCH",
    });

    const data = await response.json();

    if (data.success) {
      recalculateCartSummary();
    }
  } catch (error) {
    console.error("Error toggling item:", error);
  }
}

// Select/Deselect all items
async function toggleSelectAll(selected) {
  try {
    const response = await fetch(`/api/cart/select-all? selected=${selected}`, {
      method: "PATCH",
    });

    const data = await response.json();

    if (data.success) {
      document.querySelectorAll(".item-checkbox").forEach((cb) => {
        cb.checked = selected;
      });
      recalculateCartSummary();
    }
  } catch (error) {
    console.error("Error selecting all:", error);
  }
}

// ========== UI UPDATES ==========

// Update cart UI after changes
function updateCartUI(cartItem) {
  if (!cartItem) return;

  const itemEl = document.querySelector(
    `.cart-item[data-item-id="${cartItem.id}"]`
  );
  if (!itemEl) return;

  // Update quantity input
  const qtyInput = itemEl.querySelector(". quantity-input");
  if (qtyInput) qtyInput.value = cartItem.quantity;

  // Update subtotal
  const subtotalEl = itemEl.querySelector(".cart-item__subtotal");
  if (subtotalEl) subtotalEl.textContent = formatCurrency(cartItem.subtotal);

  recalculateCartSummary();
}

// Recalculate cart summary
function recalculateCartSummary() {
  let subtotal = 0;
  let selectedCount = 0;

  document.querySelectorAll(".cart-item").forEach((item) => {
    const checkbox = item.querySelector(". item-checkbox");
    if (checkbox && checkbox.checked) {
      const subtotalText = item.querySelector(
        ".cart-item__subtotal"
      ).textContent;
      const value = parseInt(subtotalText.replace(/[^\d]/g, ""));
      subtotal += value;
      selectedCount++;
    }
  });

  // Update summary
  const subtotalEl = document.getElementById("subtotal");
  if (subtotalEl) subtotalEl.textContent = formatCurrency(subtotal);

  // Update shipping
  const shippingFee = subtotal >= 500000 ? 0 : 30000;
  const shippingEl = document.getElementById("shippingFee");
  if (shippingEl) {
    shippingEl.innerHTML =
      shippingFee === 0
        ? '<span class="text-success">Miễn phí</span>'
        : formatCurrency(shippingFee);
  }

  // Update total
  const total = subtotal + shippingFee;
  const totalEl = document.getElementById("totalAmount");
  if (totalEl) totalEl.textContent = formatCurrency(total);

  // Update selected count
  const countEl = document.querySelector(".cart-summary__label span");
  if (countEl) countEl.textContent = selectedCount;

  // Update checkout button
  const checkoutBtn = document.querySelector('a[href="/checkout"]');
  if (checkoutBtn) {
    if (selectedCount === 0) {
      checkoutBtn.classList.add("disabled");
      checkoutBtn.style.pointerEvents = "none";
    } else {
      checkoutBtn.classList.remove("disabled");
      checkoutBtn.style.pointerEvents = "auto";
    }
  }

  // Update free shipping progress
  updateFreeShippingProgress(subtotal);
}

// Update free shipping progress bar
function updateFreeShippingProgress(subtotal) {
  const threshold = 500000;
  const progressWrapper = document.querySelector(".free-shipping-progress");

  if (!progressWrapper) return;

  if (subtotal >= threshold) {
    progressWrapper.style.display = "none";
  } else {
    progressWrapper.style.display = "block";
    const remaining = threshold - subtotal;
    const percent = Math.min((subtotal / threshold) * 100, 100);

    progressWrapper.querySelector(".text-cyan").textContent =
      formatCurrency(remaining);
    progressWrapper.querySelector(
      ".rating-bar__fill"
    ).style.width = `${percent}%`;
  }
}

// Check if cart is empty
function checkEmptyCart() {
  const items = document.querySelectorAll(". cart-item");
  if (items.length === 0) {
    location.reload();
  }
}

// ========== VOUCHER ==========

// Apply voucher code
async function applyVoucher(code) {
  if (!code) {
    code = document.getElementById("voucherCode")?.value?.trim();
  }

  if (!code) {
    showToast("warning", "Cảnh báo", "Vui lòng nhập mã giảm giá");
    return;
  }

  try {
    // Get current subtotal
    const subtotalText =
      document.getElementById("subtotal")?.textContent || "0";
    const subtotal = parseInt(subtotalText.replace(/[^\d]/g, ""));

    const response = await fetch("/api/vouchers/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: code, orderAmount: subtotal }),
    });

    const data = await response.json();
    const messageEl = document.getElementById("voucherMessage");

    if (data.success) {
      if (messageEl) {
        messageEl.innerHTML = `<span class="text-success"><i class='bx bx-check'></i> ${data.message}</span>`;
      }

      // Show discount row
      const discountRow = document.getElementById("discountRow");
      if (discountRow) discountRow.style.display = "flex";

      // Update discount value
      const discountEl = document.getElementById("discountAmount");
      if (discountEl)
        discountEl.textContent = "-" + formatCurrency(data.data.discountAmount);

      // Update total
      const totalEl = document.getElementById("totalAmount");
      if (totalEl) totalEl.textContent = formatCurrency(data.data.finalAmount);

      // Store voucher code
      sessionStorage.setItem("voucherCode", code);

      showToast("success", "Thành công", "Áp dụng mã giảm giá thành công");
    } else {
      if (messageEl) {
        messageEl.innerHTML = `<span class="text-danger"><i class='bx bx-x'></i> ${data.message}</span>`;
      }
      showToast("error", "Lỗi", data.message);
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể kiểm tra mã giảm giá");
  }
}

// Remove voucher
function removeVoucher() {
  sessionStorage.removeItem("voucherCode");
  document.getElementById("voucherCode").value = "";
  document.getElementById("voucherMessage").innerHTML = "";
  document.getElementById("discountRow").style.display = "none";
  recalculateCartSummary();
}

// ========== INITIALIZATION ==========

document.addEventListener("DOMContentLoaded", function () {
  // Quantity change handlers
  document.querySelectorAll(".quantity-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      const itemId = this.dataset.itemId;
      const input = this.parentElement.querySelector(". quantity-input");
      const currentQty = parseInt(input.value) || 1;
      const delta = this.querySelector(". bx-plus") ? 1 : -1;
      const newQty = Math.max(
        1,
        Math.min(currentQty + delta, parseInt(input.max) || 999)
      );

      if (newQty !== currentQty) {
        input.value = newQty;
        updateCartItem(itemId, newQty);
      }
    });
  });

  // Direct quantity input
  document.querySelectorAll(".quantity-input").forEach((input) => {
    input.addEventListener("change", function () {
      const itemId = this.dataset.itemId;
      let newQty = parseInt(this.value) || 1;
      newQty = Math.max(1, Math.min(newQty, parseInt(this.max) || 999));
      this.value = newQty;
      updateCartItem(itemId, newQty);
    });
  });

  // Remove item handlers
  document.querySelectorAll(".cart-item__remove").forEach((btn) => {
    btn.addEventListener("click", function () {
      const itemId = this.dataset.itemId;
      if (confirm("Bạn có chắc muốn xóa sản phẩm này? ")) {
        removeCartItem(itemId);
      }
    });
  });

  // Select all checkbox
  const selectAllCb = document.getElementById("selectAll");
  if (selectAllCb) {
    selectAllCb.addEventListener("change", function () {
      toggleSelectAll(this.checked);
    });
  }

  // Individual item checkboxes
  document.querySelectorAll(".item-checkbox").forEach((cb) => {
    cb.addEventListener("change", function () {
      toggleItemSelected(this.dataset.itemId);

      // Update select all checkbox
      const allCheckboxes = document.querySelectorAll(".item-checkbox");
      const checkedBoxes = document.querySelectorAll(".item-checkbox:checked");
      const selectAllCb = document.getElementById("selectAll");
      if (selectAllCb) {
        selectAllCb.checked = allCheckboxes.length === checkedBoxes.length;
        selectAllCb.indeterminate =
          checkedBoxes.length > 0 && checkedBoxes.length < allCheckboxes.length;
      }
    });
  });

  // Voucher apply
  const voucherBtn = document.querySelector(". voucher-form . btn");
  if (voucherBtn) {
    voucherBtn.addEventListener("click", () => applyVoucher());
  }

  // Voucher input enter key
  const voucherInput = document.getElementById("voucherCode");
  if (voucherInput) {
    voucherInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        e.preventDefault();
        applyVoucher();
      }
    });

    // Check for stored voucher
    const storedVoucher = sessionStorage.getItem("voucherCode");
    if (storedVoucher) {
      voucherInput.value = storedVoucher;
      applyVoucher(storedVoucher);
    }
  }

  // Initial calculation
  recalculateCartSummary();
});

// Add fadeOut animation
const style = document.createElement("style");
style.textContent = `
    @keyframes fadeOut {
        to {
            opacity: 0;
            transform: translateX(-20px);
        }
    }
`;
document.head.appendChild(style);
