// Pagination logic for products and categories

// Event Listeners for Modals and UI specific to this view
document.addEventListener('DOMContentLoaded', function() {
    // --- Tab Persistence ---
    const mgmtTabs = document.getElementById('mgmtTabs');
    if (mgmtTabs) {
        // Restore tab from sessionStorage
        const lastTabId = sessionStorage.getItem('mgmtActiveTab');
        if (lastTabId) {
            const tabBtn = document.getElementById(lastTabId);
            if (tabBtn) {
                // Initialize Bootstrap tab if not already done and show it
                const tab = bootstrap.Tab.getOrCreateInstance(tabBtn);
                tab.show();
            }
        }

        // Save tab on change
        mgmtTabs.addEventListener('shown.bs.tab', function(e) {
            sessionStorage.setItem('mgmtActiveTab', e.target.id);
        });
    }

    const productImageUrlInput = document.getElementById('productImageUrl');
    const imagePreview = document.getElementById('imagePreview');
    if (productImageUrlInput && imagePreview) {
        productImageUrlInput.addEventListener('input', function() {
            if (this.value) {
                imagePreview.src = this.value;
                imagePreview.style.display = 'block';
            } else {
                imagePreview.style.display = 'none';
            }
        });
    }

    // --- Initial Data Load (Dynamic) ---
    // If we are on this standalone page, we trigger the filters immediately
    if (typeof runSharedBackendFilter === 'function') {
        runSharedBackendFilter(0);
    }
    if (typeof runSharedBackendCategoryFilter === 'function') {
        runSharedBackendCategoryFilter(0);
    }

    // Global exports for th:onclick
});
