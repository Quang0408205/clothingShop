package com.example.clothingshop.service.admin;

import com.example.clothingshop.entity.Order;
import com.example.clothingshop.repository.OrderRepository;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.totalRevenue();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public double getGrowthRate() {
        // Simple growth: compute revenue last 30 days vs previous 30 days via DB fetch
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime thirty = now.minusDays(30);
        ZonedDateTime sixty = now.minusDays(60);

        BigDecimal recent = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(thirty.toLocalDateTime()))
            .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prev = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(sixty.toLocalDateTime()) && o.getCreatedAt().isBefore(thirty.toLocalDateTime()))
            .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (prev.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return recent.subtract(prev).doubleValue() / prev.doubleValue() * 100.0;
    }

    public List<BigDecimal> getMonthlyRevenue(int year) {
        List<BigDecimal> months = new ArrayList<>();
        for (int i = 0; i < 12; i++) months.add(BigDecimal.ZERO);
        List<Object[]> rows = orderRepository.monthlyRevenue(year);
        for (Object[] r : rows) {
            Integer m = ((Number) r[0]).intValue();
            BigDecimal s = r[1] == null ? BigDecimal.ZERO : new BigDecimal(r[1].toString());
            months.set(m - 1, s);
        }
        return months;
    }

    public Page<Order> latestOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size));
    }
}
