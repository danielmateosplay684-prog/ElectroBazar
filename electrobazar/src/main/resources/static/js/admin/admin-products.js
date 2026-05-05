/**
 * admin-products.js
 * Product management functions, pagination, and table rendering.
 */

function openProductModal(id) {
    const form = document.getElementById('productForm');
    if (form) form.reset();
    document.getElementById('productId').value = id || '';
    
    const imagePreview = document.getElementById('imagePreview');
    if (imagePreview) {
        imagePreview.src = '';
        imagePreview.style.display = 'none';
    }
    document.getElementById('productModalLabel').textContent = id ? 'Editar Producto' : 'Nuevo Producto';

    if (id) {
        fetch('/api/products/' + id)
            .then(res => res.json())
            .then(p => {
                document.getElementById('productName').value = p.name;
                document.getElementById('productDescription').value = p.description || '';
                document.getElementById('productPrice').value = p.price;
                document.getElementById('productStock').value = p.stock || 0;
                document.getElementById('productCategory').value = p.category ? p.category.id : '';
                document.getElementById('productTaxRate').value = p.taxRate ? p.taxRate.id : '';
                document.getElementById('productUnit').value = p.measurementUnit ? p.measurementUnit.id : '';
                document.getElementById('productActive').checked = p.active;
                document.getElementById('productExistingImageUrl').value = p.imageUrl || '';
                
                if (p.imageUrl) {
                    const img = document.getElementById('imagePreview');
                    if (img) {
                        img.src = p.imageUrl;
                        img.style.display = 'block';
                    }
                }
            });
    }
    productModal.show();
}

let isSavingProduct = false;

function saveProduct() {
    if (isSavingProduct) return;
    const id = document.getElementById('productId').value;
    const formData = new FormData();
    
    // Append fields individually (flat structures are easier to handle with @ModelAttribute)
    formData.append('name', document.getElementById('productName').value);
    formData.append('description', document.getElementById('productDescription').value);
    
    const price = parseFloat(document.getElementById('productPrice').value) || 0;
    if (price < 0) {
        showToast('El precio no puede ser negativo', 'error');
        return;
    }
    formData.append('price', price);
    formData.append('stock', document.getElementById('productStock').value || "0");
    formData.append('active', document.getElementById('productActive').checked);
    
    const catId = document.getElementById('productCategory').value;
    if (catId) formData.append('categoryId', catId);
    
    const taxId = document.getElementById('productTaxRate').value;
    if (taxId) formData.append('taxRateId', taxId);
    
    const unitId = document.getElementById('productUnit').value;
    if (unitId) formData.append('measurementUnitId', unitId);

    const fileInput = document.getElementById('productImage');
    if (fileInput && fileInput.files && fileInput.files.length > 0) {
        formData.append('imageFile', fileInput.files[0]);
    } else if (id) {
        // No new file selected — preserve existing imageUrl
        const existingImageUrl = document.getElementById('productExistingImageUrl')?.value;
        if (existingImageUrl) formData.append('imageUrl', existingImageUrl);
    }

    isSavingProduct = true;
    fetch('/api/products' + (id ? '/' + id : ''), {
        method: id ? 'PUT' : 'POST',
        body: formData
    }).then(async res => {
        isSavingProduct = false;
        if (res.ok) {
            productModal.hide();
            showToast(getAdminI18n('successSave'));
            setTimeout(() => location.reload(), 1000);
        } else {
            const data = await res.json().catch(() => ({}));
            showToast(data.error || getAdminI18n('errorSave'), 'error');
        }
    }).catch(() => {
        isSavingProduct = false;
        showToast(getAdminI18n('errorSave'), 'error');
    });
}

function toggleProductStatus(id) {
    fetch('/api/products/' + id, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                showToast(getAdminI18n('successSave'));
                setTimeout(() => location.reload(), 1000);
            }
        });
}

function deleteProduct(id, name) {
    if (!confirm('¿Seguro que quieres ELIMINAR permanentemente el producto "' + name + '"? Esta acción no se puede deshacer.')) return;
    fetch('/api/products/' + id + '/hard', { method: 'DELETE' })
        .then(async res => {
            if (res.ok) {
                showToast('Producto eliminado correctamente');
                setTimeout(() => location.reload(), 1000);
            } else {
                const data = await res.json();
                showToast(data.error || 'No se pudo eliminar el producto', 'error');
            }
        }).catch(err => {
            showToast('Error de conexión al eliminar', 'error');
        });
}

function uploadCsvFile(input) {
    if (!input.files || input.files.length === 0) return;
    const formData = new FormData();
    formData.append('file', input.files[0]);

    showToast(getAdminI18n('loading'), 'info');
    fetch('/api/admin/products/import-csv', {
        method: 'POST',
        body: formData
    }).then(res => {
        if (res.ok) {
            showToast(getAdminI18n('successSave'));
            location.reload();
        } else {
            showToast(getAdminI18n('errorSave'), 'error');
        }
    });
}

// --- Global Exports ---
window.openProductModal = openProductModal;
window.saveProduct = saveProduct;
window.toggleProductStatus = toggleProductStatus;
window.deleteProduct = deleteProduct;
window.uploadCsvFile = uploadCsvFile;

// Allow saving with Enter key
document.addEventListener('DOMContentLoaded', function() {
    const productForm = document.getElementById('productForm');
    if (productForm) {
        productForm.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                saveProduct();
            }
        });
    }
});
