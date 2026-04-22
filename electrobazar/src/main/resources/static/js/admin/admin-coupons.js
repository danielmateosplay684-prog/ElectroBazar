/**
 * admin-coupons.js
 * Coupon management functions.
 */

let selectedCouponProducts = new Set();
let selectedCouponCategories = new Set();
let couponModalInstance = null;

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function searchCouponProducts(query) {
    if (!query || query.length < 2) {
        document.getElementById('couponProductSearchResults').innerHTML = '';
        return;
    }

    // Using global admin products API for consistency
    fetch(`/api/admin/products?search=${encodeURIComponent(query)}&size=10`)
        .then(res => res.json())
        .then(data => {
            const products = data.content || [];
            const results = document.getElementById('couponProductSearchResults');
            results.innerHTML = products.map(p => `
                <div class="list-group-item list-group-item-action cursor-pointer py-2 px-3 border-0 bg-secondary-hover" 
                     onclick="addCouponProduct(${p.id}, '${escHtml(p.name)}')">
                    <div class="small fw-bold">${escHtml(p.name)}</div>
                    <div class="text-muted" style="font-size:0.7rem;">ID: #${p.id}</div>
                </div>
            `).join('');
        });
}

function searchCouponCategories(query) {
    if (!query || query.length < 2) {
        document.getElementById('couponCategorySearchResults').innerHTML = '';
        return;
    }

    fetch(`/api/admin/categories?search=${encodeURIComponent(query)}&size=10`)
        .then(res => res.json())
        .then(data => {
            const categories = data.content || [];
            const results = document.getElementById('couponCategorySearchResults');
            results.innerHTML = categories.map(c => `
                <div class="list-group-item list-group-item-action cursor-pointer py-2 px-3 border-0 bg-secondary-hover" 
                     onclick="addCouponCategory(${c.id}, '${escHtml(c.name)}')">
                    <div class="small fw-bold">${escHtml(c.name)}</div>
                </div>
            `).join('');
        });
}

function addCouponProduct(id, name) {
    // Check if already in set
    if ([...selectedCouponProducts].some(p => p.id === id)) return;

    selectedCouponProducts.add({ id, name });
    renderSelectedCouponProducts();
    document.getElementById('couponProductSearchResults').innerHTML = '';
    document.getElementById('couponProductSearch').value = '';
}

function addCouponCategory(id, name) {
    if ([...selectedCouponCategories].some(c => c.id === id)) return;

    selectedCouponCategories.add({ id, name });
    renderSelectedCouponCategories();
    document.getElementById('couponCategorySearchResults').innerHTML = '';
    document.getElementById('couponCategorySearch').value = '';
}

function removeCouponProduct(id) {
    selectedCouponProducts = new Set([...selectedCouponProducts].filter(p => p.id !== id));
    renderSelectedCouponProducts();
}

function removeCouponCategory(id) {
    selectedCouponCategories = new Set([...selectedCouponCategories].filter(c => c.id !== id));
    renderSelectedCouponCategories();
}

function renderSelectedCouponProducts() {
    const container = document.getElementById('selectedCouponProducts');
    if (!container) return;
    container.innerHTML = [...selectedCouponProducts].map(p => `
        <span class="badge bg-primary d-inline-flex align-items-center gap-1 p-2 me-1 mb-1" style="border-radius: 6px;">
            ${escHtml(p.name)} <i class="bi bi-x-circle cursor-pointer" onclick="removeCouponProduct(${p.id})"></i>
        </span>
    `).join('');
}

function renderSelectedCouponCategories() {
    const container = document.getElementById('selectedCouponCategories');
    if (!container) return;
    container.innerHTML = [...selectedCouponCategories].map(c => `
        <span class="badge bg-accent d-inline-flex align-items-center gap-1 p-2 me-1 mb-1" style="border-radius: 6px; color: #000;">
            ${escHtml(c.name)} <i class="bi bi-x-circle cursor-pointer" onclick="removeCouponCategory(${c.id})"></i>
        </span>
    `).join('');
}

function openCouponModal(id) {
    // Handle if 'id' is actually the button element (passed via onclick="openCouponModal(this)")
    let couponId = id;
    if (id && typeof id === 'object' && id.dataset) {
        couponId = id.dataset.id;
    }

    const form = document.getElementById('couponForm');
    if (form) form.reset();

    document.getElementById('couponId').value = couponId || '';
    selectedCouponProducts.clear();
    selectedCouponCategories.clear();

    // Clear list search results
    document.getElementById('couponProductSearchResults').innerHTML = '';
    document.getElementById('couponCategorySearchResults').innerHTML = '';

    if (couponId) {
        fetch(`/api/coupons/${couponId}`)
            .then(res => res.json())
            .then(c => {
                document.getElementById('couponCode').value = c.code || '';
                document.getElementById('couponDesc').value = c.description || '';
                document.getElementById('couponType').value = c.discountType || 'PERCENTAGE';
                document.getElementById('couponValue').value = c.discountValue || 0;
                document.getElementById('couponLimit').value = c.usageLimit || '';
                document.getElementById('couponActive').checked = (c.active !== false);
                document.getElementById('couponGeneric').checked = (c.generic !== false);
                document.getElementById('couponFrom').value = c.validFrom ? c.validFrom.substring(0, 16) : '';
                document.getElementById('couponUntil').value = c.validUntil ? c.validUntil.substring(0, 16) : '';

                if (c.restrictedProducts) {
                    c.restrictedProducts.forEach(p => selectedCouponProducts.add({ id: p.id, name: p.nameEs || p.name }));
                }
                if (c.restrictedCategories) {
                    c.restrictedCategories.forEach(cat => selectedCouponCategories.add({ id: cat.id, name: cat.nameEs || cat.name }));
                }
                renderSelectedCouponProducts();
                renderSelectedCouponCategories();
            });
    } else {
        renderSelectedCouponProducts();
        renderSelectedCouponCategories();
    }

    if (!couponModalInstance) {
        couponModalInstance = new bootstrap.Modal(document.getElementById('couponModal'));
    }
    couponModalInstance.show();
}

function saveCoupon() {
    const id = document.getElementById('couponId').value;
    const coupon = {
        id: id ? parseInt(id) : null,
        code: document.getElementById('couponCode').value,
        description: document.getElementById('couponDesc').value,
        discountType: document.getElementById('couponType').value,
        discountValue: parseFloat(document.getElementById('couponValue').value),
        usageLimit: document.getElementById('couponLimit').value ? parseInt(document.getElementById('couponLimit').value) : null,
        active: document.getElementById('couponActive').checked,
        generic: document.getElementById('couponGeneric').checked,
        validFrom: document.getElementById('couponFrom').value ? document.getElementById('couponFrom').value : null,
        validUntil: document.getElementById('couponUntil').value ? document.getElementById('couponUntil').value : null,
        restrictedProducts: [...selectedCouponProducts].map(p => ({ id: p.id })),
        restrictedCategories: [...selectedCouponCategories].map(c => ({ id: c.id }))
    };

    fetch('/api/coupons', {
        method: 'POST', // The backend save method handles both creation and update
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(coupon)
    }).then(res => {
        if (res.ok) {
            if (couponModalInstance) couponModalInstance.hide();
            showToast('Cupón guardado correctamente');
            setTimeout(() => location.reload(), 1000);
        } else {
            res.json().then(data => showToast(data.message || 'Error al guardar el cupón', 'error'));
        }
    });
}

function deleteCoupon(id) {
    if (!confirm('¿Seguro que quieres eliminar este cupón?')) return;
    fetch(`/api/coupons/${id}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                showToast('Cupón eliminado');
                setTimeout(() => location.reload(), 1000);
            }
        });
}

function loadCoupons() {
    const tbody = document.getElementById('couponsTable')?.querySelector('tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';

    fetch('/api/coupons')
        .then(res => res.json())
        .then(data => {
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No hay cupones registrados.</td></tr>';
                return;
            }

            tbody.innerHTML = data.map(c => `
                <tr>
                    <td><strong class="text-accent">${escHtml(c.code)}</strong></td>
                    <td>${escHtml(c.description || '—')}</td>
                    <td>
                        <span class="badge ${c.discountType === 'PERCENTAGE' ? 'bg-primary' : 'bg-info'}">
                            ${c.discountType === 'PERCENTAGE' ? 'Porcentaje' : 'Importe Fijo'}
                        </span>
                    </td>
                    <td class="fw-bold">
                        ${c.discountType === 'PERCENTAGE' ? (c.discountValue + '%') : (c.discountValue.toFixed(2) + '€')}
                    </td>
                    <td>
                        <span class="badge-active ${c.active ? 'yes' : 'no'}">
                            ${c.active ? 'Si' : 'No'}
                        </span>
                    </td>
                    <td>
                        <span>${c.timesUsed || 0}</span> / <span>${c.usageLimit || '—'}</span>
                    </td>
                    <td>${c.validUntil ? c.validUntil.substring(0, 10) : '—'}</td>
                    <td style="text-align:right">
                        <div style="display:flex;gap:0.4rem;justify-content:flex-end">
                            <button class="btn-icon" title="Editar" 
                                onclick="openCouponModal(${c.id})">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn-icon danger" title="Eliminar"
                                onclick="deleteCoupon(${c.id})">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
        })
        .catch(err => {
            console.error("Error loading coupons:", err);
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-danger py-4">Error al conectar con el servidor.</td></tr>';
        });
}

// Global Exports
window.searchCouponProducts = searchCouponProducts;
window.searchCouponCategories = searchCouponCategories;
window.addCouponProduct = addCouponProduct;
window.addCouponCategory = addCouponCategory;
window.removeCouponProduct = removeCouponProduct;
window.removeCouponCategory = removeCouponCategory;
window.renderSelectedCouponProducts = renderSelectedCouponProducts;
window.renderSelectedCouponCategories = renderSelectedCouponCategories;
window.openCouponModal = openCouponModal;
window.saveCoupon = saveCoupon;
window.deleteCoupon = deleteCoupon;
window.loadCoupons = loadCoupons;
