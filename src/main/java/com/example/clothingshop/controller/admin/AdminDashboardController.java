package com.example.clothingshop.controller.admin;

import com.example.clothingshop.service.admin.AdminDashboardService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.clothingshop.entity.Order;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/admin/reports";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        BigDecimal totalRevenue = dashboardService.getTotalRevenue();
        long totalOrders = dashboardService.getTotalOrders();
        long totalUsers = dashboardService.getTotalUsers();
        double growth = dashboardService.getGrowthRate();
        List<BigDecimal> monthly = dashboardService.getMonthlyRevenue(java.time.LocalDate.now().getYear());

        Page<Order> latest = dashboardService.latestOrders(page, 5);

        int currentYear = java.time.LocalDate.now().getYear();
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("growth", growth);
        model.addAttribute("monthly", monthly);
        model.addAttribute("latestOrders", latest);
        model.addAttribute("currentYear", currentYear);
        return "admin/dashboard";
    }
}
