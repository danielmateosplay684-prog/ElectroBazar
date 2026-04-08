document.addEventListener('DOMContentLoaded', function () {
    const theoryAmountElem = document.getElementById('theoryAmount');
    if (!theoryAmountElem) return;

    // Leemos el valor teórico inyectado por Thymeleaf
    let theoryAmount = parseFloat(theoryAmountElem.getAttribute('data-amount')) || 0;

    const closingBalanceInput = document.getElementById('closingBalance');
    const realAmountDisplay = document.getElementById('realAmount');
    const diffAmountSpan = document.getElementById('diffAmount');
    const retainToggle = document.getElementById('retainToggle');
    const retainInput = document.getElementById('retainInput');
    const hiddenRetained = document.getElementById('retainedAmount');

    if (retainInput) retainInput._userEdited = false;

    function updateDifference() {
        // Obtenemos lo que el usuario escribe en el input
        const val = closingBalanceInput.value.replace(',', '.');
        const realAmount = parseFloat(val) || 0;

        // Calculamos la diferencia contra la teoría
        const difference = realAmount - theoryAmount;

        // Actualizamos el campo "Real (Contado)" del resumen
        if (realAmountDisplay) {
            realAmountDisplay.textContent = realAmount.toLocaleString('es-ES', { minimumFractionDigits: 2 }) + '€';
        }

        // Actualizamos el campo "Diferencia"
        if (diffAmountSpan) {
            diffAmountSpan.textContent = (difference > 0 ? '+' : '') + difference.toLocaleString('es-ES', { minimumFractionDigits: 2 }) + '€';

            // Color verde si cuadra o sobra, rojo si falta
            if (Math.abs(difference) < 0.01) {
                diffAmountSpan.className = 'fw-bold text-success';
                diffAmountSpan.textContent = '0,00€';
            } else if (difference > 0) {
                diffAmountSpan.className = 'fw-bold text-success';
            } else {
                diffAmountSpan.className = 'fw-bold text-danger';
            }
        }

        // Auto-rellenar fondo para mañana si el usuario no lo ha tocado
        if (retainToggle && retainToggle.checked && retainInput && !retainInput._userEdited) {
            retainInput.value = realAmount > 0 ? realAmount.toFixed(2) : '';
        }
    }

    function toggleRetainVisibility() {
        if (!retainToggle || !retainInput) return;
        const col = retainInput.closest('.col-md-3');
        if (retainToggle.checked) {
            col.style.display = 'block';
            updateDifference();
        } else {
            col.style.display = 'none';
            retainInput.value = '';
        }
    }

    closingBalanceInput.addEventListener('input', updateDifference);
    retainToggle.addEventListener('change', toggleRetainVisibility);
    retainInput.addEventListener('input', () => retainInput._userEdited = true);

    document.getElementById('cashCloseForm').addEventListener('submit', function () {
        if (retainToggle.checked && retainInput.value) {
            hiddenRetained.value = retainInput.value.replace(',', '.');
        } else {
            hiddenRetained.value = '0';
        }
    });

    toggleRetainVisibility();
    updateDifference();
});