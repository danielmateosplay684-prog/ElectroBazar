/**
 * Spanish NIF/NIE/CIF Validator module.
 */
function isNifCifNieValid(taxId) {
    if (!taxId || taxId.trim() === '') return { valid: true, msg: '' }; // Allow empty

    taxId = taxId.trim().toUpperCase();

    const nifPattern = /^[0-9]{8}[A-Z]$/;
    const niePattern = /^[XYZ][0-9]{7}[A-Z]$/;
    const cifPattern = /^[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-Z]$/;

    if (nifPattern.test(taxId)) return validateNIF(taxId);
    if (niePattern.test(taxId)) return validateNIE(taxId);
    if (cifPattern.test(taxId)) return validateCIF(taxId);

    return { valid: false, msg: 'Formato no reconocido' };
}

function validateNIF(nif) {
    const letters = "TRWAGMYFPDXBNJZSQVHLCKE";
    const letter = nif.charAt(8);
    const number = parseInt(nif.substring(0, 8), 10);
    const expectedLetter = letters.charAt(number % 23);

    if (expectedLetter === letter) {
        return { valid: true, msg: '' };
    } else {
        return { valid: false, msg: 'NIF inválido: la letra no corresponde al número' };
    }
}

function validateNIE(nie) {
    const letters = "TRWAGMYFPDXBNJZSQVHLCKE";
    const prefix = nie.charAt(0);
    let prefixNum = '';
    if (prefix === 'X') prefixNum = '0';
    else if (prefix === 'Y') prefixNum = '1';
    else if (prefix === 'Z') prefixNum = '2';

    const number = parseInt(prefixNum + nie.substring(1, 8), 10);
    const letter = nie.charAt(8);
    const expectedLetter = letters.charAt(number % 23);

    if (expectedLetter === letter) {
        return { valid: true, msg: '' };
    } else {
        return { valid: false, msg: 'NIE inválido: la letra de control es incorrecta' };
    }
}

function validateCIF(cif) {
    const numbers = cif.substring(1, 8);
    const control = cif.charAt(8);

    let evenSum = 0;
    let oddSum = 0;

    for (let i = 0; i < numbers.length; i++) {
        let n = parseInt(numbers.charAt(i), 10);
        if (i % 2 === 0) { // odd positions (1st, 3rd, 5th, 7th...) -> index 0, 2, 4, 6
            let doubleOdd = n * 2;
            oddSum += (doubleOdd >= 10) ? (doubleOdd - 9) : doubleOdd;
        } else {
            evenSum += n;
        }
    }

    const totalSum = evenSum + oddSum;
    const controlDigit = (10 - (totalSum % 10)) % 10;

    const controlLetters = "JABCDEFGHI";
    const expectedLetter = controlLetters.charAt(controlDigit);
    const expectedNumber = controlDigit.toString();

    if (control === expectedNumber || control === expectedLetter) {
        return { valid: true, msg: '' };
    } else {
        return { valid: false, msg: 'CIF inválido: dígito de control incorrecto' };
    }
}

function attachNifCifValidator(inputId, formId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    // Create feedback element if it doesn't exist
    let feedback = document.getElementById(inputId + '-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.id = inputId + '-feedback';
        feedback.className = 'mt-1';
        feedback.style.fontSize = '0.85rem';
        input.parentNode.appendChild(feedback);
    }

    input.addEventListener('input', function () {
        const val = this.value;
        const result = isNifCifNieValid(val);

        if (val.trim() === '') {
            this.classList.remove('is-valid', 'is-invalid');
            feedback.innerHTML = '';
            input.dataset.invalidNif = 'false';
            return;
        }

        if (result.valid) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
            feedback.innerHTML = '<span class="text-success"><i class="bi bi-check-circle-fill"></i> Válido</span>';
            input.dataset.invalidNif = 'false';
        } else {
            this.classList.remove('is-valid');
            this.classList.add('is-invalid');
            feedback.innerHTML = '<span class="text-danger"><i class="bi bi-x-circle-fill"></i> ' + result.msg + '</span>';
            input.dataset.invalidNif = 'true';
        }
    });
}
