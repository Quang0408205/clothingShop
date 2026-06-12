package com.example.clothingshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String note;                 // ghi chu (khong bat buoc)

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod = "CASH";   // CASH | BANK

    @Column(name = "refund_bank", length = 100)
    private String refundBank;               // ngan hang nhan hoan tien

    @Column(name = "refund_account", length = 50)
    private String refundAccount;            // so tai khoan nhan hoan tien

    @Column(name = "refund_account_name", length = 150)
    private String refundAccountName;        // chu tai khoan

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSING";   // PROCESSING | COMPLETED | CANCELLED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Tien ich: them 1 dong vao don, tu set quan he 2 chieu
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}
