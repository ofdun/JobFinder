const runSelector = document.getElementById('run-selector');
const sizeSelector = document.getElementById('size-selector');
const limitSelector = document.getElementById('limit-selector');

function selectReport() {
    document.querySelectorAll('[data-run]').forEach(panel => {
        panel.hidden = panel.dataset.run !== runSelector.value;
    });
    document.querySelectorAll('[data-size]').forEach(panel => {
        panel.hidden = panel.dataset.size !== sizeSelector.value;
    });
    if (limitSelector) {
        document.querySelectorAll('[data-limit]').forEach(panel => {
            panel.hidden = panel.dataset.limit !== limitSelector.value;
        });
    }
}

document.querySelectorAll('.percentile-group').forEach(group => {
    const buttons = group.querySelectorAll('[data-chart]');
    const panels = group.querySelectorAll('[data-chart-panel]');
    function selectPercentile(value) {
        buttons.forEach(button => {
            button.setAttribute('aria-pressed', String(button.dataset.chart === value));
        });
        panels.forEach(panel => {
            panel.hidden = panel.dataset.chartPanel !== value;
        });
    }
    buttons.forEach(button => {
        button.addEventListener('click', () => selectPercentile(button.dataset.chart));
    });
    selectPercentile('p50');
});

runSelector.addEventListener('change', selectReport);
sizeSelector.addEventListener('change', selectReport);
if (limitSelector) limitSelector.addEventListener('change', selectReport);
selectReport();
