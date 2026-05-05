/**
 * admin-analytics.js
 * Analytics and dashboard charts logic.
 */

let salesChart = null;
let categoryChart = null;
let hourlyChartInstance = null;
let topProductsChartInstance = null;

function onAnalyticsPeriodChange() {
    const val = document.getElementById('analyticsPeriod').value;
    const dateInput = document.getElementById('analyticsDate');
    if (dateInput) {
        dateInput.style.display = (val === 'custom') ? 'block' : 'none';
        if (val === 'custom' && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }
    }
    updateAnalytics();
}

function updateAnalytics() {
    console.log("updateAnalytics called");
    const periodSelect = document.getElementById('analyticsPeriod');
    const period = periodSelect ? periodSelect.value : '7days';
    const periodText = periodSelect ? periodSelect.options[periodSelect.selectedIndex].text : '';
    const now = new Date();
    let fromDate = new Date();
    let toDate = new Date();
    let chartTitle = '';

    const translationEl = document.getElementById('analytics-js-translations') || document.getElementById('admin-js-translations');
    const labels = {
        trend: translationEl ? (translationEl.getAttribute('data-chart-trend') || 'Tendencia') : 'Tendencia',
        analysis: translationEl ? (translationEl.getAttribute('data-chart-analysis') || 'Análisis') : 'Análisis',
        error: translationEl ? (translationEl.getAttribute('data-error-loading') || 'Error al cargar') : 'Error al cargar'
    };

    const toLocalISO = (d) => {
        try {
            if (!d || isNaN(d.getTime())) return new Date().toISOString().slice(0, 19);
            const off = d.getTimezoneOffset() * 60000;
            return new Date(d.getTime() - off).toISOString().slice(0, 19);
        } catch (e) {
            console.error("ISO conversion error", e);
            return new Date().toISOString().slice(0, 19);
        }
    };

    toDate.setHours(23, 59, 59, 999);

    if (period === 'today') {
        fromDate.setHours(0, 0, 0, 0);
    } else if (period === '7days') {
        fromDate.setDate(now.getDate() - 6);
        fromDate.setHours(0, 0, 0, 0);
    } else if (period === '1month') {
        fromDate.setMonth(now.getMonth() - 1);
        fromDate.setHours(0, 0, 0, 0);
    } else if (period === '6months') {
        fromDate.setMonth(now.getMonth() - 6);
        fromDate.setHours(0, 0, 0, 0);
    } else if (period === '1year') {
        fromDate.setFullYear(now.getFullYear() - 1);
        fromDate.setHours(0, 0, 0, 0);
    } else if (period === 'all') {
        fromDate = new Date(0);
    }

    if (period === 'today' || period === '7days' || period === '1month' || period === '6months' || period === '1year' || period === 'all') {
        chartTitle = labels.trend + ' (' + periodText + ')';
    } else if (period === 'custom') {
        const dVal = document.getElementById('analyticsDate').value;
        if (dVal) {
            fromDate = new Date(dVal);
            fromDate.setHours(0, 0, 0, 0);
            toDate = new Date(dVal);
            toDate.setHours(23, 59, 59, 999);
            chartTitle = labels.analysis + ' ' + fromDate.toLocaleDateString();
        }
    }

    // Update title in UI
    const titleEl = document.getElementById('salesChartTitle');
    if (titleEl) {
        const span = titleEl.querySelector('span');
        if (span) span.textContent = chartTitle;
        else titleEl.textContent = chartTitle;
    }

    const url = `/api/sales/analytics?from=${toLocalISO(fromDate)}&to=${toLocalISO(toDate)}&_=${Date.now()}`;
    console.log("Fetching analytics from:", url);

    fetch(url)
        .then(r => { 
            if (!r.ok) throw new Error('Status: ' + r.status); 
            return r.json(); 
        })
        .then(analytics => {
            console.log("Analytics data received:", analytics);
            initCharts(analytics, period, chartTitle);
        })
        .catch(err => {
            console.error('Error updating analytics:', err);
            if (typeof showToast === 'function') showToast(labels.error, 'error');
        });
}

function initCharts(analytics, period, chartLabel) {
    console.log("initCharts called with period:", period, "analytics:", analytics);
    if (!analytics) {
        console.warn("No analytics data to render");
        return;
    }

    try {
        // KPI Counters
        console.log("Updating KPI counters...");
        if (document.getElementById('statTodayRevenue')) {
            document.getElementById('statTodayRevenue').textContent =
                (analytics.totalRevenue || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 }) + ' €';
        }
        if (document.getElementById('statTodaySales')) {
            document.getElementById('statTodaySales').textContent = analytics.totalSales || 0;
        }
        if (document.getElementById('statTopProduct')) {
            const topP = analytics.topProductName || '—';
            document.getElementById('statTopProduct').textContent =
                topP.length > 20 ? topP.substring(0, 20) + '...' : topP;
        }
        if (document.getElementById('statLowStock')) {
            document.getElementById('statLowStock').textContent = analytics.lowStockCount || 0;
        }
        if (document.getElementById('statAvgTicket')) {
            document.getElementById('statAvgTicket').textContent =
                (analytics.averageTicket || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 }) + ' €';
        }
        if (document.getElementById('statCancellationRate')) {
            document.getElementById('statCancellationRate').textContent =
                (analytics.cancellationRate || 0).toFixed(1) + '%';
        }

        // Translation element safe lookup
        const transEl = document.getElementById('analytics-js-translations') || document.getElementById('admin-js-translations');
        const salesLabel = transEl ? (transEl.getAttribute('data-label-sales') || 'Ventas') : 'Ventas';

        // Trend Chart
        console.log("Initializing Trend Chart...");
        const trendData = (period === 'today') ? (analytics.hourlyTrend || {}) : (analytics.revenueTrend || {});
        let labels = [];
        let datasetsData = [];

        if (period === 'today') {
            for (let i = 0; i < 24; i++) {
                labels.push(i + ':00');
                datasetsData.push(trendData[i] || 0);
            }
        } else {
            Object.keys(trendData).sort().forEach(dateStr => {
                try {
                    const d = new Date(dateStr);
                    if (isNaN(d.getTime())) {
                        labels.push(dateStr);
                    } else {
                        labels.push(d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }));
                    }
                    datasetsData.push(trendData[dateStr]);
                } catch (e) {
                    console.error("Error parsing date:", dateStr, e);
                }
            });
        }

        const ctxSales = document.getElementById('salesChart');
        if (ctxSales) {
            console.log('canvas dimensions (salesChart):', ctxSales.offsetWidth, ctxSales.offsetHeight);
            console.log("Rendering Sales Chart...");
            if (salesChart) salesChart.destroy();
            salesChart = new Chart(ctxSales.getContext('2d'), {
                type: (labels.length <= 1) ? 'bar' : 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: salesLabel,
                        data: datasetsData,
                        borderColor: '#f5a623',
                        backgroundColor: 'rgba(245, 166, 35, 0.1)',
                        fill: true,
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { 
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return (context.raw || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 }) + ' €';
                                }
                            }
                        }
                    },
                    scales: {
                        y: { 
                            beginAtZero: true,
                            grid: { color: 'rgba(255,255,255,0.05)' },
                            ticks: {
                                callback: function(value) { return value + ' €'; }
                            }
                        },
                        x: { grid: { display: false } }
                    }
                }
            });
        }

        // Category Distribution
        const catSummary = analytics.categoryDistribution || {};
        const ctxCat = document.getElementById('categoryChart');
        if (ctxCat && Object.keys(catSummary).length > 0) {
            console.log('canvas dimensions (categoryChart):', ctxCat.offsetWidth, ctxCat.offsetHeight);
            console.log("Rendering Category Chart...");
            if (categoryChart) categoryChart.destroy();
            categoryChart = new Chart(ctxCat.getContext('2d'), {
                type: 'doughnut',
                data: {
                    labels: Object.keys(catSummary),
                    datasets: [{
                        data: Object.values(catSummary),
                        backgroundColor: ['#f5a623', '#3b82f6', '#22c55e', '#ef4444', '#a855f7', '#06b6d4'],
                        borderWidth: 0
                    }]
                },
                options: { cutout: '75%', plugins: { legend: { position: 'bottom' } } }
            });
        }

        // Hourly Trend
        const hourlyTrend = analytics.hourlyTrend || {};
        const ctxHourly = document.getElementById('hourlyChart');
        if (ctxHourly) {
            console.log('canvas dimensions (hourlyChart):', ctxHourly.offsetWidth, ctxHourly.offsetHeight);
            console.log("Rendering Hourly Chart...");
            if (hourlyChartInstance) hourlyChartInstance.destroy();

            let hourlyLabels = [];
            let hourlyData = [];
            for (let i = 0; i < 24; i++) {
                hourlyLabels.push(i + ':00');
                hourlyData.push(hourlyTrend[i] || 0);
            }

            hourlyChartInstance = new Chart(ctxHourly.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: hourlyLabels,
                    datasets: [{
                        label: 'Ventas (€)',
                        data: hourlyData,
                        backgroundColor: '#10b981'
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { display: false } },
                        y: { grid: { color: 'rgba(255,255,255,0.05)' } }
                    }
                }
            });
        }

        // Top Products Chart
        const topProds = analytics.topProducts || {};
        const ctxTop = document.getElementById('topProductsChart');
        if (ctxTop && Object.keys(topProds).length > 0) {
            console.log('canvas dimensions (topProductsChart):', ctxTop.offsetWidth, ctxTop.offsetHeight);
            console.log("Rendering Top Products Chart...");
            if (topProductsChartInstance) topProductsChartInstance.destroy();
            topProductsChartInstance = new Chart(ctxTop.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: Object.keys(topProds),
                    datasets: [{
                        label: 'Ventas (€)',
                        data: Object.values(topProds),
                        backgroundColor: '#3b82f6'
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    plugins: { legend: { display: false } }
                }
            });
        }
        console.log("initCharts completed successfully");

    } catch (err) {
        console.error("Critical error in initCharts:", err);
    }
}

function initDashboardCharts() {
    updateAnalytics();
}

// Returns Detail Logic
let currentReturnsData = [];

function showReturnDetailsModal() {
    const periodSelect = document.getElementById('analyticsPeriod');
    const period = periodSelect ? periodSelect.value : '7days';
    const now = new Date();
    let fromDate = new Date();
    let toDate = new Date();

    // Consolidation of toLocalISO to ensure consistency with main analytics
    const formatToISO = (d) => {
        const off = d.getTimezoneOffset() * 60000;
        return new Date(d.getTime() - off).toISOString().slice(0, 19);
    };

    toDate.setHours(23, 59, 59, 999);

    if (period === 'today') { fromDate.setHours(0, 0, 0, 0); }
    else if (period === '7days') { fromDate.setDate(now.getDate() - 6); fromDate.setHours(0, 0, 0, 0); }
    else if (period === '1month') { fromDate.setMonth(now.getMonth() - 1); fromDate.setHours(0, 0, 0, 0); }
    else if (period === '6months') { fromDate.setMonth(now.getMonth() - 6); fromDate.setHours(0, 0, 0, 0); }
    else if (period === '1year') { fromDate.setFullYear(now.getFullYear() - 1); fromDate.setHours(0, 0, 0, 0); }
    else if (period === 'all') { fromDate = new Date(0); }
    else if (period === 'custom') {
        const dVal = document.getElementById('analyticsDate').value;
        if (dVal) {
            fromDate = new Date(dVal); fromDate.setHours(0, 0, 0, 0);
            toDate = new Date(dVal); toDate.setHours(23, 59, 59, 999);
        }
    }

    const url = `/api/returns?from=${formatToISO(fromDate)}&to=${formatToISO(toDate)}`;
    const tbody = document.getElementById('analyticsReturnsTableBody');
    if (tbody) tbody.innerHTML = '<tr><td colspan="6" class="text-center py-5"><div class="spinner-border spinner-border-sm text-warning me-2"></div> Analizando devoluciones...</td></tr>';

    const modalEl = document.getElementById('returnsDetailModal');
    if (modalEl) {
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    }

    fetch(url)
        .then(r => {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        })
        .then(data => {
            currentReturnsData = data;
            renderAnalyticsReturnsTable(data);
        })
        .catch(err => {
            console.error('Error fetching returns:', err);
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4"><i class="bi bi-exclamation-octagon me-2"></i> Error: ${err.message}</td></tr>`;
        });
}

function renderAnalyticsReturnsTable(returns) {
    const tbody = document.getElementById('analyticsReturnsTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!returns || returns.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-5"><div class="mb-2"><i class="bi bi-inbox fs-2"></i></div> No se encontraron devoluciones en este periodo</td></tr>';
        return;
    }

    returns.forEach(ret => {
        const date = new Date(ret.createdAt).toLocaleString('es-ES', {
            day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        // Document Identification (Invoice, Ticket or #ID)
        let docLabel = '#' + (ret.originalSale ? ret.originalSale.id : (ret.saleId || '—'));
        if (ret.originalSale) {
            if (ret.originalSale.invoice) docLabel = ret.originalSale.invoice.invoiceNumber;
            else if (ret.originalSale.ticket) docLabel = ret.originalSale.ticket.ticketNumber;
        }

        if (ret.lines && ret.lines.length > 0) {
            ret.lines.forEach(line => {
                const productName = line.saleLine ? line.saleLine.productName : 'Producto Desconocido';
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="small" style="white-space:nowrap; color: var(--text-muted);">${date}</td>
                    <td><span class="badge bg-secondary bg-opacity-25 text-light fw-normal">${docLabel}</span></td>
                    <td style="font-weight:500; color:var(--text-main);">${productName}</td>
                    <td class="text-muted small italic">${ret.reason || '—'}</td>
                    <td class="text-center fw-600">${line.quantity}</td>
                    <td class="text-end text-danger fw-bold" style="font-size: 0.95rem;">-${(line.subtotal || 0).toFixed(2)} €</td>
                `;
                tbody.appendChild(tr);
            });
        }
    });
}

function filterReturnsTable() {
    const query = document.getElementById('returnSearchInput').value.toLowerCase().trim();
    if (!query) {
        renderAnalyticsReturnsTable(currentReturnsData);
        return;
    }

    const filtered = currentReturnsData.filter(ret => {
        const saleId = ret.originalSale ? ret.originalSale.id.toString() : '';
        const ticketNum = (ret.originalSale && ret.originalSale.ticket) ? ret.originalSale.ticket.ticketNumber.toLowerCase() : '';
        const invoiceNum = (ret.originalSale && ret.originalSale.invoice) ? ret.originalSale.invoice.invoiceNumber.toLowerCase() : '';
        const reason = (ret.reason || '').toLowerCase();

        const matchMaster = reason.includes(query) ||
            saleId.includes(query) ||
            ticketNum.includes(query) ||
            invoiceNum.includes(query);

        const matchLines = ret.lines && ret.lines.some(l =>
            l.saleLine && l.saleLine.productName.toLowerCase().includes(query)
        );
        return matchMaster || matchLines;
    });
    renderAnalyticsReturnsTable(filtered);
}

// Global Exports
window.onAnalyticsPeriodChange = onAnalyticsPeriodChange;
window.updateAnalytics = updateAnalytics;
window.initCharts = initCharts;
window.initDashboardCharts = initDashboardCharts;
window.showReturnDetailsModal = showReturnDetailsModal;
window.filterReturnsTable = filterReturnsTable;
