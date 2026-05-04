/**
 * admin-abonos.js
 * Modern Abonos (credits/refunds) management.
 */

// Initialize global functions
window.openAbonoModal = openAbonoModal;
window.saveAbono = saveAbono;
window.filterAbonos = filterAbonos;
window.anularAbono = anularAbono;
window.eliminarAbono = eliminarAbono;
window.imprimirAbono = imprimirAbono;

function openAbonoModal() {
    const form = document.getElementById('abonoForm');
    if (form) form.reset();
    if (window.abonoModal) window.abonoModal.show();
}

/**
 * Saves a new credit/abono
 */
function saveAbono() {
    const clienteId = document.getElementById('abonoFormClienteId').value;
    const ventaId = document.getElementById('abonoFormVentaId').value;
    const importe = document.getElementById('abonoFormImporte').value;
    const tipo = document.getElementById('abonoFormTipo').value;
    const pago = document.getElementById('abonoFormPago').value;
    const requiresFullUse = document.getElementById('abonoFormRequiresFullUse')?.checked;
    
    const motivo = document.getElementById('abonoFormMotivo')?.value || '';
    
    if (!clienteId || !importe || !tipo || !pago) {
        if (typeof showToast === 'function') showToast(getAdminI18n('errorSave') || 'Campos obligatorios', 'error');
        return;
    }

    const payload = {
        clienteId: clienteId.trim(),
        ventaOriginalId: ventaId ? parseInt(ventaId) : null,
        importe: parseFloat(importe),
        tipoAbono: tipo,
        metodoPago: pago,
        motivo: motivo,
        requiresFullUse: requiresFullUse,
        fechaLimite: document.getElementById('abonoFormFechaLimite')?.value || null
    };

    fetch('/api/abonos', {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (res.ok) {
            if (window.abonoModal) window.abonoModal.hide();
            if (typeof showToast === 'function') showToast(getAdminI18n('successSave') || 'Éxito', 'success');
            filterAbonos();
        } else {
            const err = await res.text();
            if (typeof showToast === 'function') showToast(err || 'Error', 'error');
        }
    })
    .catch(err => {
        console.error(err);
        if (typeof showToast === 'function') showToast(getAdminI18n('errorConnection') || 'Error de conexión', 'error');
    });
}

/**
 * Loads abonos paginated — all or filtered by client.
 * Called on view init and on every filter change.
 */
function filterAbonos() {
    const clienteVal = (document.getElementById('abonoClienteSearch')?.value || '').trim();
    const tbody = document.getElementById('abonosTableBody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4"><span class="spinner-border spinner-border-sm me-2"></span>Cargando...</td></tr>';

    const params = new URLSearchParams();
    if (clienteVal) params.append('cliente', clienteVal);
    params.append('sortBy', 'fecha');
    params.append('sortDir', 'desc');
    params.append('size', '50');

    fetch(`/api/abonos?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            const list = data.content || [];
            tbody.innerHTML = '';

            const labelEl = document.getElementById('abonoCountLabel');
            if (labelEl) {
                if (clienteVal) {
                    labelEl.textContent = `Mostrando ${data.totalElements ?? list.length} abonos del cliente "${clienteVal}".`;
                } else {
                    labelEl.textContent = `Mostrando ${data.totalElements ?? list.length} abonos en total.`;
                }
            }

            if (list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No se encontraron abonos.</td></tr>';
                return;
            }

            list.forEach(a => {
                const tr = document.createElement('tr');
                const statusClass = a.estado === 'PENDIENTE' ? 'badge-status-p status-active-p' : 'badge-status-p status-cancelled-p';
                const statusText = a.estado === 'PENDIENTE' ? 'ACT. PAGO' : (a.estado === 'APLICADO' ? 'APLICADO' : 'ANULADO');

                tr.innerHTML = `
                    <td><strong>${a.code || '#' + a.id}</strong></td>
                    <td>${new Date(a.fecha).toLocaleString()}</td>
                    <td>${a.cliente ? (a.cliente.idDocumentNumber || a.cliente.taxId || '#' + a.cliente.id) : '--'}</td>
                    <td><span class="text-accent fw-bold">${a.tipoAbono}</span></td>
                    <td>${a.metodoPago}</td>
                    <td class="text-end fw-bold">${parseFloat(a.importe).toFixed(2)} €</td>
                    <td><span class="${statusClass}">${statusText}</span></td>
                    <td class="text-end">
                        <div class="d-flex justify-content-end gap-2">
                            <button class="btn-icon accent" onclick='imprimirAbono(${JSON.stringify(a).replace(/'/g, "&apos;")})' title="Imprimir Ticket">
                                <i class="bi bi-printer"></i>
                            </button>
                             ${a.estado === 'PENDIENTE' ? `
                                <button class="btn-icon warning" onclick="anularAbono(${a.id})" title="Anular">
                                    <i class="bi bi-slash-circle"></i>
                                </button>
                                <button class="btn-icon danger" onclick="eliminarAbono(${a.id})" title="Eliminar Permanentemente">
                                    <i class="bi bi-trash"></i>
                                </button>
                             ` : ''}
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger py-4">Error al cargar datos: ${err.message}</td></tr>`;
        });
}

/**
 * Cancels an abono
 */
function anularAbono(id) {
    if (!confirm('¿Seguro que desea anular este abono?')) return;

    fetch(`/api/abonos/${id}/anular`, {
        method: 'PATCH',
        headers: { 
            [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
        }
    })
    .then(res => {
        if (res.ok) {
            if (typeof showToast === 'function') showToast('Abono anulado correctamente', 'success');
            filterAbonos();
        } else {
            if (typeof showToast === 'function') showToast('No se pudo anular el abono', 'error');
        }
    });
}

/**
 * Permanently deletes an abono (only if PENDING)
 */
function eliminarAbono(id) {
    if (!confirm('¿Seguro que desea ELIMINAR PERMANENTEMENTE este abono? Esta acción no se puede deshacer.')) return;

    fetch(`/api/abonos/${id}`, {
        method: 'DELETE',
        headers: { 
            [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
        }
    })
    .then(res => {
        if (res.ok) {
            if (typeof showToast === 'function') showToast('Abono eliminado correctamente', 'success');
            filterAbonos();
        } else {
            if (typeof showToast === 'function') showToast('No se pudo eliminar el abono (puede que ya haya sido usado)', 'error');
        }
    })
    .catch(err => {
        console.error(err);
        if (typeof showToast === 'function') showToast('Error de conexión', 'error');
    });
}

/**
 * Prints the abono in pocket size (80mm)
 */
function imprimirAbono(a) {
    const isWallet = a.requiresFullUse === false;
    const rulesText = isWallet 
        ? "MONEDERO: Puedes usarlo parcialmente." 
        : "VALE: Requiere compra por el importe total.";

    const printWin = window.open('', '', 'width=400,height=600');
    printWin.document.write(`
        <html>
        <head>
            <title>Imprimir Abono #${a.id}</title>
            <style>
                body { font-family: 'Courier New', Courier, monospace; width: 80mm; margin: 0; padding: 10px; font-size: 12px; line-height: 1.2; }
                .text-center { text-align: center; }
                .divider { border-top: 1px dashed #000; margin: 10px 0; }
                .header h2 { margin: 5px 0; font-size: 18px; text-transform: uppercase; }
                .id-box { border: 2px solid #000; padding: 10px; margin: 10px 0; font-size: 20px; font-weight: bold; background: #eee; }
                .amount { font-size: 24px; font-weight: bold; margin: 10px 0; }
                .footer { font-size: 10px; margin-top: 20px; }
            </style>
        </head>
        <body onload="window.print(); window.close();">
            <div class="header text-center">
                <h2>ELECTROBAZAR</h2>
                <div class="divider"></div>
                <div>VALE DE ABONO / CRÉDITO</div>
            </div>

            <div class="text-center">
                <div class="amount">${parseFloat(a.importe).toFixed(2)} €</div>
                <p>Fecha: ${new Date(a.fecha).toLocaleString()}</p>
                <p>Cliente: ${a.cliente ? a.cliente.name : 'Cliente Genérico'}</p>
            </div>

            <div class="divider"></div>

            <div class="text-center">
                <p>INTRODUZCA ESTE CÓDIGO EN EL TPV:</p>
                <div class="id-box">${a.code || '#' + a.id}</div>
                <p style="font-size: 9px; font-weight: bold;">${rulesText}</p>
            </div>

            <div class="divider"></div>

            <div class="footer text-center">
                <p>Gracias por su confianza.</p>
                <p>Conserve este ticket para futuras compras.</p>
            </div>
        </body>
        </html>
    `);
    printWin.document.close();
}
