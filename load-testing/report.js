(() => {
  const charts = [...document.querySelectorAll('.interactive-chart')];
  const select = document.getElementById('component');
  const config = {
    responsive: true,
    displaylogo: false,
    showSendToCloud: false,
    displayModeBar: true,
    scrollZoom: false,
    modeBarButtonsToRemove: ['select2d', 'lasso2d', 'sendChartToCloud'],
    toImageButtonOptions: {format: 'png', filename: 'jobfinder-load', scale: 2},
  };

  function draw(chart) {
    if (!window.Plotly) {
      chart.textContent = 'Plotly не загрузился. Проверьте подключение к cdn.plot.ly.';
      return;
    }
    if (chart.data) {
      Plotly.Plots.resize(chart);
      return;
    }
    const spec = JSON.parse(chart.nextElementSibling.textContent);
    const percentile = spec.mode === 'percentiles';
    const colors = ['#2563eb', '#d97706', '#0d9488', '#9333ea', '#e11d48'];
    const names = [...new Set(spec.data.map(trace => trace.name))];
    spec.data.forEach(trace => {
      const color = colors[names.indexOf(trace.name) % colors.length];
      if (trace.type === 'scatter') trace.line = {...trace.line, color};
      trace.marker = {...trace.marker, color};
    });
    const layout = {
      autosize: true,
      margin: {l: 64, r: 20, t: 80, b: 58},
      font: {family: 'Inter, Arial, sans-serif', size: 12, color: '#17263c'},
      colorway: colors,
      paper_bgcolor: '#fff',
      plot_bgcolor: '#fff',
      hovermode: 'x unified',
      dragmode: 'zoom',
      legend: {orientation: 'h', x: 0, y: 1.02, yanchor: 'bottom'},
      showlegend: spec.data.filter(trace => trace.showlegend !== false).length > 1,
      barmode: 'group',
      bargap: 0.06,
      xaxis: {
        title: {text: spec.xlabel}, type: 'linear', gridcolor: '#edf1f5', automargin: true,
        ...(percentile ? {range: [0, 100], tickvals: [0, 25, 50, 75, 90, 95, 99], tickangle: -45} : {}),
      },
      yaxis: {title: {text: spec.ylabel}, type: 'linear', rangemode: 'tozero', gridcolor: '#edf1f5', automargin: true},
    };
    Plotly.newPlot(chart, spec.data, layout, config).catch(() => {
      chart.textContent = 'Не удалось построить график.';
    });
  }

  function showComponent() {
    document.querySelectorAll('[data-component]').forEach(panel => {
      panel.hidden = panel.dataset.component !== select.value;
    });
    charts.filter(chart => !chart.closest('[hidden]')).forEach(draw);
  }

  if (select) {
    select.addEventListener('change', showComponent);
    showComponent();
  } else {
    charts.forEach(draw);
  }
})();
