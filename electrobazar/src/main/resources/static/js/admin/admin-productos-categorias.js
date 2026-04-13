/**
 * admin-productos-categorias.js
 * Specific logic for the management of products and categories in the products-categories view.
 */

function getSharedInvLocale() {
    try {
        const prefs = JSON.parse(localStorage.getItem('tpv-prefs'));
        return (prefs && prefs.language) ? prefs.language : 'es';
    } catch (e) {
        return 'es';
    }
}

// Override the shared product table rendering to include new columns (IVA and Descripción)
if (typeof renderSharedProductsTable === 'function' || true) {
    window.renderSharedProductsTable = function (products) {
        const tbody = document.getElementById('productsTableBody');
        if (!tbody) return;
        tbody.innerHTML = '';
        const i18n = window.sharedInventoryI18n || {};
        
        if (!products || products.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center p-4 text-muted">${i18n.noItems || 'No items'}</td></tr>`;
            return;
        }

        const locale = getSharedInvLocale();
        const isEn = locale === 'en';

        products.forEach(p => {
            const name = isEn && p.nameEn ? p.nameEn : (p.nameEs || p.name);
            const description = isEn && p.descriptionEn ? p.descriptionEn : (p.descriptionEs || p.description);
            const decimals = (p.measurementUnit && p.measurementUnit.decimalPlaces !== undefined) ? p.measurementUnit.decimalPlaces : (p.measurementUnit && p.measurementUnit.decimal_places !== undefined ? p.measurementUnit.decimal_places : 0);
            const formattedPrice = typeof formatDecimal === 'function' ? formatDecimal(p.price) : (p.price || 0).toFixed(2) + ' €';
            const formattedStock = (p.stock === 0 || !p.stock) ? "0" : (typeof formatDecimal === 'function' ? formatDecimal(p.stock, decimals, decimals) : p.stock.toFixed(decimals));
            const stockStyle = p.stock < 5 ? 'fw-bold text-danger' : '';
            const badgeLowStock = p.stock < 5 ? `<span class="badge-stock-low" style="font-size: 0.7rem;">${i18n.lowStock || 'Low'}</span>` : '';

            let catName = '—';
            if (p.category) {
                catName = isEn && p.category.nameEn ? p.category.nameEn : (p.category.nameEs || p.category.name);
            }

            const imgHtml = p.imageUrl
                ? `<img src="${p.imageUrl}" class="thumb" alt="">`
                : `<div class="thumb-placeholder"><i class="bi bi-image"></i></div>`;
            const activeBadge = p.active
                ? `<span class="badge-active yes">${i18n.yes || 'Yes'}</span>`
                : `<span class="badge-active no">${i18n.no || 'No'}</span>`;
            
            const ivaDisplay = p.taxRate && p.taxRate.vatRate != null ? (p.taxRate.vatRate * 100).toFixed(0) + '%' : '—';
            const descTruncated = description ? (description.length > 60 ? description.substring(0, 57) + '...' : description) : '—';
            const escapedName = name ? name.replace(/'/g, "\\'").replace(/"/g, "&quot;") : '';

            let tr = document.createElement('tr');
            tr.className = 'product-row';
            tr.setAttribute('data-iva', p.taxRate ? p.taxRate.vatRate : null);
            tr.innerHTML = `
                <td class="id-val right">#${p.id}</td>
                <td class="center">${imgHtml}</td>
                <td>
                    <strong>${name}</strong>
                    <div class="product-desc-small">${descTruncated}</div>
                </td>
                <td class="product-iva-val center">${ivaDisplay}</td>
                <td class="price-val right">${formattedPrice}€</td>
                <td class="center"><div class="d-flex flex-column align-items-center" style="gap: 0.2rem;"><span class="${stockStyle}">${formattedStock}</span> ${badgeLowStock}</div></td>
                <td class="center"><span class="product-unit-val">${p.measurementUnit ? p.measurementUnit.symbol : '-'}</span></td>
                <td class="center"><span class="product-category-val">${catName}</span></td>
                <td class="center">${activeBadge}</td>
                <td class="right">
                    <div class="d-flex gap-1 justify-content-end">
                        <button class="btn-icon" title="${i18n.actions_edit || 'Edit'}" onclick="openProductModal(${p.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn-icon danger" title="${i18n.actions_delete || 'Delete'}" onclick="deleteProduct(${p.id}, '${escapedName}')"><i class="bi bi-trash"></i></button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
        if (typeof filterProducts === 'function') filterProducts();
    };
}

function filterProducts() {
    const ivaFilter = document.getElementById('sharedFilterIvaRate');
    if (!ivaFilter) return;
    const selectedIva = ivaFilter.value;
    const rows = document.querySelectorAll('.product-row');
    rows.forEach(row => {
        const rowIva = row.getAttribute('data-iva');
        let matchesIva = true;
        if (selectedIva !== "") {
            const val = rowIva ? parseFloat(rowIva).toFixed(2) : null;
            const target = parseFloat(selectedIva).toFixed(2);
            matchesIva = (val === target);
        }
        row.style.display = matchesIva ? '' : 'none';
    });
}

// Pagination Products
function getTotalPages() {
    const el = document.getElementById('totalPages');
    return el ? parseInt(el.value, 10) || 1 : 1;
}

function getPageSize() {
    const el = document.getElementById('pageSize');
    return el ? parseInt(el.value, 10) || 25 : 25;
}

function goToPage(page) {
    const totalPages = getTotalPages();
    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    url.searchParams.set('size', getPageSize());
    window.location.href = url.toString();
}

function jumpToPage(value) {
    let page = parseInt(value, 10);
    const totalPages = getTotalPages();
    if (isNaN(page)) return;
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;
    goToPage(page - 1);
}

// Pagination Categories
function getCategoriesTotalPages() {
    const el = document.getElementById('categoriesTotalPages');
    return el ? parseInt(el.value, 10) || 1 : 1;
}

function getCategoriesPageSize() {
    const el = document.getElementById('categoriesPageSize');
    return el ? parseInt(el.value, 10) || 25 : 25;
}

function goToCategoriesPage(page) {
    const totalPages = getCategoriesTotalPages();
    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    const url = new URL(window.location.href);
    url.searchParams.set('categoriesPage', page);
    url.searchParams.set('categoriesSize', getCategoriesPageSize());
    window.location.href = url.toString();
}

function jumpToCategoriesPage(value) {
    let page = parseInt(value, 10);
    const totalPages = getCategoriesTotalPages();
    if (isNaN(page)) return;
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;
    goToCategoriesPage(page - 1);
}

// Export functions to window if needed (already mostly global)
window.getSharedInvLocale = getSharedInvLocale;
window.filterProducts = filterProducts;
window.goToPage = goToPage;
window.jumpToPage = jumpToPage;
window.goToCategoriesPage = goToCategoriesPage;
window.jumpToCategoriesPage = jumpToCategoriesPage;

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
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
});
