document.addEventListener('DOMContentLoaded', function () {

  // Mobile menu
  const mobileMenuBtn = document.getElementById('mobile-menu-button');
  const sidebar = document.querySelector('aside');
  if (mobileMenuBtn && sidebar) {
    mobileMenuBtn.addEventListener('click', () => sidebar.classList.toggle('hidden'));
  }

  // Dashboard revenue chart
  const ctx = document.getElementById('revenueChart');
  if (ctx && typeof ADMIN_MONTHLY !== 'undefined') {
    // ADMIN_MONTHLY is List<BigDecimal> serialised by Thymeleaf — may be numbers or strings
    const monthly = ADMIN_MONTHLY.map(v => {
      if (v === null || v === undefined) return 0;
      if (typeof v === 'number') return v;
      if (typeof v === 'string') return parseFloat(v) || 0;
      // BigDecimal serialised as object edge-case
      if (typeof v === 'object' && v.intVal !== undefined) return Number(v.intVal) / Math.pow(10, v.scale || 0);
      return Number(v) || 0;
    });

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12'],
        datasets: [{
          label: 'Doanh thu (đ)',
          data: monthly,
          borderColor: '#4F46E5',
          backgroundColor: 'rgba(79,70,229,0.08)',
          borderWidth: 2.5,
          pointRadius: 4,
          tension: 0.35,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: ctx => ctx.parsed.y.toLocaleString('vi-VN') + 'đ' } }
        },
        scales: {
          y: {
            ticks: { callback: v => (v / 1_000_000).toFixed(0) + 'tr' },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }
});