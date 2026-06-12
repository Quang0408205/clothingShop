package com.example.clothingshop.controller.user;

import com.example.clothingshop.entity.Order;
import com.example.clothingshop.entity.OrderItem;
import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.OrderRepository;
import com.example.clothingshop.repository.ProductRepository;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Value("${shop.bank.id}")      private String bankId;
    @Value("${shop.bank.account}") private String bankAccount;
    @Value("${shop.bank.name}")    private String bankName;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Authentication auth, Model model) {
        User u = currentUser(auth);
        model.addAttribute("orders", orderRepository.findByUserIdOrderByIdDesc(u.getId()));
        return "user/orders";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Integer id, Authentication auth, Model model) {
        User u = currentUser(auth);
        Order order = orderRepository.findByIdAndUserId(id, u.getId()).orElse(null);
        if (order == null) {
            return "redirect:/orders";
        }
        order.getItems().size();
        model.addAttribute("order", order);
        model.addAttribute("bankId", bankId);
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("bankName", bankName);
        return "user/order-detail";
    }

    // Huy don hang -> hoan lai ton kho
    @PostMapping("/{id}/cancel")
    @Transactional
    public String cancel(@PathVariable Integer id,
                         @RequestParam(required = false) String refundBank,
                         @RequestParam(required = false) String refundAccount,
                         @RequestParam(required = false) String refundAccountName,
                         Authentication auth, RedirectAttributes ra) {
        User u = currentUser(auth);
        Order order = orderRepository.findByIdAndUserId(id, u.getId()).orElse(null);
        if (order == null) {
            return "redirect:/orders";
        }
        String status = order.getStatus();
        if (!"PROCESSING".equals(status) && !"PENDING_PAYMENT".equals(status)) {
            ra.addFlashAttribute("err", "Đơn hàng này không thể huỷ.");
            return "redirect:/orders";
        }

        boolean isBank = "BANK".equals(order.getPaymentMethod());
        // Don chuyen khoan da dat (PROCESSING): bat buoc nhap thong tin hoan tien
        // Don chua thanh toan (PENDING_PAYMENT): huy thang, khong can nhap STK
        if ("PROCESSING".equals(status) && isBank && (refundAccount == null || refundAccount.isBlank()
                || refundBank == null || refundBank.isBlank())) {
            ra.addFlashAttribute("err", "Đơn chuyển khoản: vui lòng nhập ngân hàng và số tài khoản để nhận hoàn tiền.");
            return "redirect:/orders/" + id;
        }

        // Hoan lai so luong vao kho
        for (OrderItem it : order.getItems()) {
            productRepository.findById(it.getProductId()).ifPresent(p -> {
                int stock = p.getStock() == null ? 0 : p.getStock();
                p.setStock(stock + it.getQuantity());
                productRepository.save(p);
            });
        }

        if ("PENDING_PAYMENT".equals(status)) {
            // Chua thanh toan -> huy thang, khong can hoan tien
            order.setStatus("CANCELLED");
            ra.addFlashAttribute("msg", "Đã huỷ đơn #" + id + ".");
        } else if (isBank) {
            order.setStatus("REFUNDING");
            order.setRefundBank(refundBank.trim());
            order.setRefundAccount(refundAccount.trim());
            order.setRefundAccountName(refundAccountName != null ? refundAccountName.trim() : null);
            ra.addFlashAttribute("msg", "Đã gửi yêu cầu hoàn tiền cho đơn #" + id + ".");
        } else {
            order.setStatus("CANCELLED");
            ra.addFlashAttribute("msg", "Đã huỷ đơn #" + id + ".");
        }
        orderRepository.save(order);
        return "redirect:/orders";
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Khong tim thay tai khoan dang nhap"));
    }
}
