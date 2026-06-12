package com.example.clothingshop.controller.admin;

import com.example.clothingshop.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final OrderRepository orderRepository;

    public AdminReportController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String reports(@RequestParam(defaultValue = "0") int year, Model model) {
        int currentYear = LocalDate.now().getYear();
        if (year == 0) year = currentYear;

        List<BigDecimal> monthly    = buildMonthlyRevenue(orderRepository.monthlyCompletedRevenue(year));
        List<Long>       counts     = buildMonthlyCounts(orderRepository.monthlyOrderCount(year));
        List<Object[]>   topProds   = orderRepository.topProductsByRevenue(year);
        List<Object[]>   statusBreakdown = orderRepository.orderStatusBreakdown(year);
        BigDecimal       yearTotal  = orderRepository.completedRevenueByYear(year);
        long             yearOrders = orderRepository.orderCountByYear(year);

        // Tính doanh thu trung bình / đơn hoàn thành
        long completedCount = statusBreakdown.stream()
                .filter(r -> "COMPLETED".equals(r[0]))
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();
        BigDecimal avgOrder = completedCount > 0
                ? yearTotal.divide(BigDecimal.valueOf(completedCount), 0, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        model.addAttribute("monthly",        monthly);
        model.addAttribute("counts",         counts);
        model.addAttribute("topProducts",    topProds);
        model.addAttribute("statusBreakdown",statusBreakdown);
        model.addAttribute("yearTotal",      yearTotal);
        model.addAttribute("yearOrders",     yearOrders);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("avgOrder",       avgOrder);
        model.addAttribute("year",           year);
        model.addAttribute("years",          List.of(currentYear, currentYear - 1, currentYear - 2));

        return "admin/reports";
    }

    private List<BigDecimal> buildMonthlyRevenue(List<Object[]> rows) {
        BigDecimal[] arr = new BigDecimal[12];
        Arrays.fill(arr, BigDecimal.ZERO);
        for (Object[] row : rows) {
            int m = ((Number) row[0]).intValue();
            arr[m - 1] = new BigDecimal(row[1].toString());
        }
        return Arrays.asList(arr);
    }

    private List<Long> buildMonthlyCounts(List<Object[]> rows) {
        Long[] arr = new Long[12];
        Arrays.fill(arr, 0L);
        for (Object[] row : rows) {
            int m = ((Number) row[0]).intValue();
            arr[m - 1] = ((Number) row[1]).longValue();
        }
        return Arrays.asList(arr);
    }
}