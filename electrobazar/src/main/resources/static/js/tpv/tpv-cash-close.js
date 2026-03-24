document.addEventListener('DOMContentLoaded', function () {
    // ── Get theory amount from the hidden/visible span ──
    const theoryAmountElem = document.getElementById('theoryAmount');
    if (!theoryAmountElem) {
        console.error('theoryAmount element not found');
        return;
    }

    // Prefer data-amount for robustness; fallback to text if missing
    let theoryAmount = 0;
    const dataAmt = theoryAmountElem.getAttribute('data-amount');
    if (dataAmt !== null && dataAmt !== '') {
        theoryAmount = parseFloat(dataAmt) || 0;
    } else {
        const theoryAmountText = theoryAmountElem.textContent.replace('€', '').replace('\u20AC', '').trim();
        theoryAmount = parseFloat(theoryAmountText.replace(/\./g, '').replace(',', '.')) || 0;
    }

    const closingBalanceInput = document.getElementById('closingBalance');
    const realAmountDisplay = document.getElementById('realAmount');
    const diffAmountSpan = document.getElementById('diffAmount');

    // ── Retain logic elements ──
    const retainToggle = document.getElementById('retainToggle');
    const retainBody = document.getElementById('retainBody');
    const retainCard = document.getElementById('retainCard');
    const retainInput = document.getElementById('retainInput');
    const hiddenRetained = document.getElementById('retainedAmount');

    if (retainInput) retainInput._userEdited = false;

    function updateDifference() {
        if (!closingBalanceInput) return;

        // Correctly handle both comma and dot for manual keyboards
        const valueStr = closingBalanceInput.value.replace(',', '.');
        const realAmount = parseFloat(valueStr) || 0;
        const difference = realAmount - theoryAmount;

        // Update basic "Real" display
        if (realAmountDisplay) {
            realAmountDisplay.textContent = realAmount.toFixed(2).replace('.', ',') + '€';
        }

        // Update "Difference" display (showing localized decimals)
        if (diffAmountSpan) {
            const formattedDiff = (difference > 0 ? '+' : '') + difference.toFixed(2).replace('.', ',') + '€';
            diffAmountSpan.textContent = formattedDiff;

            if (Math.abs(difference) < 0.001) {
                diffAmountSpan.textContent = '0,00€';
                diffAmountSpan.style.color = 'var(--success)';
            } else if (difference > 0) {
                diffAmountSpan.style.color = 'var(--success)';
                diffAmountSpan.classList.add('text-success-custom');
            } else {
                diffAmountSpan.style.color = 'var(--danger)';
                diffAmountSpan.classList.add('text-danger-custom');
            }
        }

        // Mirror value into retain input only if toggle is on and user has not manually changed it
        if (retainToggle && retainToggle.checked && retainInput && !retainInput._userEdited) {
            retainInput.value = realAmount > 0 ? realAmount.toFixed(2) : '';
        }
    }

    if (closingBalanceInput) {
        ['input', 'change', 'keyup', 'paste'].forEach(evt => {
            closingBalanceInput.addEventListener(evt, updateDifference);
        });
    }

    // ── Retain toggle UI logic ──
    if (retainToggle) {
        retainToggle.addEventListener('change', function () {
            if (this.checked) {
                if (retainBody) retainBody.classList.add('open');
                if (retainCard) retainCard.classList.add('active');

                // Sync current closing balance to retain input on activation if not edited
                if (retainInput && !retainInput._userEdited) {
                    const currentClosing = parseFloat(closingBalanceInput.value.replace(',', '.')) || 0;
                    retainInput.value = currentClosing > 0 ? currentClosing.toFixed(2) : '';
                }
            } else {
                if (retainBody) retainBody.classList.remove('open');
                if (retainCard) retainCard.classList.remove('active');
            }
        });
    }

    if (retainInput) {
        ['input', 'change', 'blur'].forEach(evt => {
            retainInput.addEventListener(evt, () => {
                retainInput._userEdited = true;
            });
        });
    }

    // ── Form Guard: Merge retain value into hidden field ──
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function () {
            if (retainToggle && retainToggle.checked && retainInput && retainInput.value.trim() !== '') {
                if (hiddenRetained) hiddenRetained.value = retainInput.value.trim().replace(',', '.');
            } else {
                if (hiddenRetained) hiddenRetained.value = '';
            }
        });
    }

    // Initial state
    if (retainToggle && retainToggle.checked && retainBody) {
        retainBody.classList.add('open');
    }
    updateDifference();
});
