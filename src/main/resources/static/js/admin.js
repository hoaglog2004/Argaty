// Admin JavaScript
/**
 * ARGATY - Admin JavaScript
 * Admin dashboard functionality
 */

// ========== SIDEBAR ==========

// Toggle sidebar
function toggleSidebar() {
  const sidebar = document.getElementById("adminSidebar");
  const main = document.querySelector(". admin-main");

  if (sidebar) {
    sidebar.classList.toggle("open");
    sidebar.classList.toggle("collapsed");
  }
}

// Initialize sidebar toggle
document
  .getElementById("sidebarToggle")
  ?.addEventListener("click", toggleSidebar);

// Close sidebar on mobile when clicking outside
document.addEventListener("click", function (e) {
  const sidebar = document.getElementById("adminSidebar");
  const toggle = document.getElementById("sidebarToggle");

  if (window.innerWidth <= 992 && sidebar?.classList.contains("open")) {
    if (!sidebar.contains(e.target) && !toggle?.contains(e.target)) {
      sidebar.classList.remove("open");
    }
  }
});

// ========== DATA TABLES ==========

// Delete confirmation
function confirmDelete(message, callback) {
  if (confirm(message || "Bạn có chắc muốn xóa? ")) {
    callback();
  }
}

// Submit delete form
function submitDelete(formId) {
  confirmDelete("Bạn có chắc muốn xóa mục này? ", function () {
    document.getElementById(formId)?.submit();
  });
}

// ========== IMAGE UPLOAD ==========

// Initialize image upload
function initImageUpload() {
  const uploadAreas = document.querySelectorAll(".image-upload");

  uploadAreas.forEach((area) => {
    const input = area.querySelector('input[type="file"]');
    const preview = area
      .closest(".form-group")
      ?.querySelector(".image-preview-grid");

    // Click to upload
    area.addEventListener("click", () => input?.click());

    // Drag and drop
    area.addEventListener("dragover", (e) => {
      e.preventDefault();
      area.classList.add("dragover");
    });

    area.addEventListener("dragleave", () => {
      area.classList.remove("dragover");
    });

    area.addEventListener("drop", (e) => {
      e.preventDefault();
      area.classList.remove("dragover");

      const files = e.dataTransfer.files;
      if (files.length && input) {
        input.files = files;
        handleFileSelect(input, preview);
      }
    });

    // File input change
    input?.addEventListener("change", function () {
      handleFileSelect(this, preview);
    });
  });
}

// Handle file selection
function handleFileSelect(input, previewContainer) {
  const files = input.files;

  const maxFilesAttr = input?.dataset?.maxFiles;
  const maxFiles = maxFilesAttr ? parseInt(maxFilesAttr, 10) : 0;
  if (maxFiles > 0 && files && files.length > maxFiles) {
    if (typeof showToast === "function") {
      showToast(
        "error",
        "Quá nhiều ảnh",
        `Chỉ được chọn tối đa ${maxFiles} ảnh mỗi lần.`
      );
    } else {
      alert(`Chỉ được chọn tối đa ${maxFiles} ảnh mỗi lần.`);
    }

    // Reset input to prevent submitting too many parts
    input.value = "";
    return;
  }

  if (!previewContainer) return;

  Array.from(files).forEach((file) => {
    if (!file.type.startsWith("image/")) return;

    const reader = new FileReader();
    reader.onload = function (e) {
      addImagePreview(previewContainer, e.target.result, file.name);
    };
    reader.readAsDataURL(file);
  });
}

// Add image preview
function addImagePreview(container, src, name) {
  const item = document.createElement("div");
  item.className = "image-preview-item";
  item.innerHTML = `
        <img src="${src}" alt="${name}">
        <button type="button" class="image-preview-item__remove" onclick="this.parentElement.remove()">
            <i class='bx bx-x'></i>
        </button>
    `;
  container.appendChild(item);
}

// Upload file to server
async function uploadFile(file, directory = "general") {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("directory", directory);

  try {
    const response = await fetch("/api/files/upload", {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    if (data.success) {
      return data.data.url;
    } else {
      showToast("error", "Lỗi", data.message);
      return null;
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể tải lên file");
    return null;
  }
}

// ========== FORM VALIDATION ==========

// Validate admin form
function validateAdminForm(form) {
  let isValid = true;

  // Clear previous errors
  form
    .querySelectorAll(".is-invalid")
    .forEach((el) => el.classList.remove("is-invalid"));
  form.querySelectorAll(".form-error").forEach((el) => el.remove());

  // Required fields
  form.querySelectorAll("[required]").forEach((field) => {
    if (!field.value.trim()) {
      isValid = false;
      showFieldError(field, "Trường này là bắt buộc");
    }
  });

  // Email validation
  form.querySelectorAll('input[type="email"]').forEach((field) => {
    if (field.value && !isValidEmail(field.value)) {
      isValid = false;
      showFieldError(field, "Email không hợp lệ");
    }
  });

  // Number validation
  form.querySelectorAll('input[type="number"]').forEach((field) => {
    const min = parseFloat(field.min);
    const max = parseFloat(field.max);
    const value = parseFloat(field.value);

    if (field.value && isNaN(value)) {
      isValid = false;
      showFieldError(field, "Giá trị phải là số");
    } else if (!isNaN(min) && value < min) {
      isValid = false;
      showFieldError(field, `Giá trị tối thiểu là ${min}`);
    } else if (!isNaN(max) && value > max) {
      isValid = false;
      showFieldError(field, `Giá trị tối đa là ${max}`);
    }
  });

  return isValid;
}

// Show field error
function showFieldError(field, message) {
  field.classList.add("is-invalid");

  const error = document.createElement("div");
  error.className = "form-error";
  error.innerHTML = `<i class='bx bx-error'></i> ${message}`;

  field.parentElement.appendChild(error);
}

// Validate email
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ========== CHARTS ==========

// Initialize dashboard charts
function initDashboardCharts() {
  // Revenue chart (using simple CSS bars if no chart library)
  const revenueChart = document.getElementById("revenueChart");
  if (revenueChart && typeof Chart !== "undefined") {
    // Use Chart.js if available
    new Chart(revenueChart, {
      type: "line",
      data: {
        labels: revenueChart.dataset.labels?.split(",") || [],
        datasets: [
          {
            label: "Doanh thu",
            data: revenueChart.dataset.values?.split(",").map(Number) || [],
            borderColor: "#8b5cf6",
            backgroundColor: "rgba(139, 92, 246, 0.1)",
            fill: true,
            tension: 0.4,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: false,
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: "rgba(139, 92, 246, 0.1)",
            },
          },
          x: {
            grid: {
              display: false,
            },
          },
        },
      },
    });
  }
}

// ========== STATUS UPDATES ==========

// Update order status
async function updateOrderStatus(orderId, status, note = "") {
  try {
    const response = await fetch(`/admin/orders/${orderId}/status`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        status: status,
        note: note,
        _csrf: document.querySelector('[name="_csrf"]')?.value || "",
      }),
    });

    if (response.redirected) {
      window.location.href = response.url;
    } else {
      const data = await response.json();
      if (data.success) {
        showToast("success", "Thành công", data.message);
        location.reload();
      } else {
        showToast("error", "Lỗi", data.message);
      }
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể cập nhật trạng thái");
  }
}

// Toggle active status
async function toggleActiveStatus(type, id) {
  try {
    const response = await fetch(`/admin/${type}/${id}/toggle-active`, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        _csrf: document.querySelector('[name="_csrf"]')?.value || "",
      }),
    });

    if (response.redirected) {
      window.location.href = response.url;
    }
  } catch (error) {
    showToast("error", "Lỗi", "Không thể cập nhật trạng thái");
  }
}

// ========== SEARCH & FILTER ==========

// Debounce search
let searchTimeout;
function debounceSearch(callback, delay = 300) {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(callback, delay);
}

// Initialize admin search
function initAdminSearch() {
  const searchInput = document.querySelector(".admin-search input");

  if (searchInput) {
    searchInput.addEventListener("input", function () {
      debounceSearch(() => {
        const url = new URL(window.location.href);
        if (this.value.trim()) {
          url.searchParams.set("q", this.value.trim());
        } else {
          url.searchParams.delete("q");
        }
        url.searchParams.set("page", "0");
        window.location.href = url.toString();
      });
    });
  }
}

// Filter change handler
document.querySelectorAll(".admin-filter-select").forEach((select) => {
  select.addEventListener("change", function () {
    const url = new URL(window.location.href);
    const param = this.name || this.dataset.param;

    if (this.value) {
      url.searchParams.set(param, this.value);
    } else {
      url.searchParams.delete(param);
    }
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
  });
});

// ========== BULK ACTIONS ==========

// Select all table rows
function toggleSelectAllRows(checkbox) {
  const table = checkbox.closest("table");
  const checkboxes = table.querySelectorAll('tbody input[type="checkbox"]');

  checkboxes.forEach((cb) => {
    cb.checked = checkbox.checked;
  });

  updateBulkActions();
}

// Update bulk actions visibility
function updateBulkActions() {
  const checkedBoxes = document.querySelectorAll(
    '. admin-table tbody input[type="checkbox"]:checked'
  );
  const bulkActions = document.querySelector(".bulk-actions");

  if (bulkActions) {
    if (checkedBoxes.length > 0) {
      bulkActions.style.display = "flex";
      bulkActions.querySelector(". selected-count").textContent =
        checkedBoxes.length;
    } else {
      bulkActions.style.display = "none";
    }
  }
}

// Execute bulk action
async function executeBulkAction(action) {
  const checkedBoxes = document.querySelectorAll(
    '.admin-table tbody input[type="checkbox"]:checked'
  );
  const ids = Array.from(checkedBoxes).map((cb) => cb.value);

  if (ids.length === 0) {
    showToast("warning", "Cảnh báo", "Vui lòng chọn ít nhất một mục");
    return;
  }

  if (
    action === "delete" &&
    !confirm(`Bạn có chắc muốn xóa ${ids.length} mục đã chọn?`)
  ) {
    return;
  }

  // Implement bulk action based on current page
  console.log("Bulk action:", action, "IDs:", ids);
}

// ========== INITIALIZATION ==========

document.addEventListener("DOMContentLoaded", function () {
  initImageUpload();
  initAdminSearch();
  initDashboardCharts();

  // Form submit validation
  document.querySelectorAll(".admin-form-card form").forEach((form) => {
    form.addEventListener("submit", function (e) {
      if (!validateAdminForm(this)) {
        e.preventDefault();
        showToast("error", "Lỗi", "Vui lòng kiểm tra lại thông tin");
      }
    });
  });

  // Table row checkbox change
  document
    .querySelectorAll('.admin-table tbody input[type="checkbox"]')
    .forEach((cb) => {
      cb.addEventListener("change", updateBulkActions);
    });
});
