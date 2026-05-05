/**
 * admin-categories.js
 * Category management functions.
 */

function openCategoryModal(id) {
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryId').value = id || '';
    document.getElementById('categoryModalLabel').textContent = id ? 'Editar Categoría' : 'Nueva Categoría';

    if (id) {
        fetch('/api/categories/' + id)
            .then(function (r) { return r.json(); })
            .then(function (c) {
                document.getElementById('categoryName').value = c.nameEs || '';
                document.getElementById('categoryDescription').value = c.descriptionEs || '';
                document.getElementById('categoryActive').checked = c.active !== false;
            });
    }
    categoryModal.show();
}

function saveCategory() {
    var id = document.getElementById('categoryId').value;
    var name = document.getElementById('categoryName').value;
    if (!name || name.trim() === '') {
        showToast('El nombre de la categoría es obligatorio', 'error');
        return;
    }

    var category = {
        id: id ? parseInt(id) : null,
        nameEs: name.trim(),
        descriptionEs: document.getElementById('categoryDescription').value,
        active: document.getElementById('categoryActive').checked
    };

    fetch('/api/categories' + (id ? '/' + id : ''), {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(category)
    }).then(async res => {
        if (res.ok) {
            categoryModal.hide();
            showToast(getAdminI18n('successSave'));
            setTimeout(() => location.reload(), 1000);
        } else {
            const data = await res.json().catch(() => ({}));
            showToast(data.error || getAdminI18n('errorSave'), 'error');
        }
    }).catch(() => {
        showToast(getAdminI18n('errorNetwork'), 'error');
    });
}

function toggleCategoryStatus(id) {
    fetch('/api/categories/' + id, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                showToast(getAdminI18n('successSave'));
                setTimeout(() => location.reload(), 1000);
            }
        });
}

function deleteCategory(id, name) {
    if (!confirm('¿Seguro que quieres ELIMINAR permanentemente la categoría "' + (name || '') + '"? Esta acción no se puede deshacer.')) return;
    fetch('/api/categories/' + id + '/hard', { method: 'DELETE' })
        .then(async res => {
            if (res.ok) {
                showToast('Categoría eliminada correctamente');
                setTimeout(() => location.reload(), 1000);
            } else {
                const data = await res.json();
                showToast(data.error || 'No se pudo eliminar la categoría', 'error');
            }
        }).catch(err => {
            showToast('Error de conexión al eliminar', 'error');
        });
}

function resetCategoryFilters() {
    const srch = document.getElementById('categoryFilterSearch');
    if (srch) srch.value = '';
    const globalSearch = document.getElementById('sharedFilterSearch');
    if (globalSearch) globalSearch.value = '';

    const sortByEl = document.getElementById('categoryFilterSortBy');
    const sortDirEl = document.getElementById('categoryFilterSortDir');
    if (sortByEl) sortByEl.value = 'id';
    if (sortDirEl) sortDirEl.value = 'asc';

    if (typeof runSharedBackendCategoryFilter === 'function') {
        runSharedBackendCategoryFilter();
    }
}

// Global Exports
window.openCategoryModal = openCategoryModal;
window.saveCategory = saveCategory;
window.toggleCategoryStatus = toggleCategoryStatus;
window.deleteCategory = deleteCategory;
window.resetCategoryFilters = resetCategoryFilters;
