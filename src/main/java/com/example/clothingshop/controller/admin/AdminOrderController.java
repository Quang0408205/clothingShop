package com.example.clothingshop.controller.admin;

import com.example.clothingshop.entity.Order;
import com.example.clothingshop.entity.OrderItem;
import com.example.clothingshop.repository.OrderRepository;
import com.example.clothingshop.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminOrderController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var orders = q.isBlank()
                ? orderRepository.findAllByOrderByIdDesc(PageRequest.of(page, 20))
                : orderRepository.findByRecipientNameContainingIgnoreCaseOrderByIdDesc(q.trim(), PageRequest.of(page, 20));
        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Integer id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        order.getItems().size();   // nap danh sach item trong transaction (tranh lazy)
        model.addAttribute("order", order);
        return "admin/order-detail";
    }

    // Admin: danh dau hoan thanh
    @PostMapping("/{id}/complete")
    @Transactional
    public String complete(@PathVariable Integer id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && "PROCESSING".equals(order.getStatus())) {
            order.setStatus("COMPLETED");
            orderRepository.save(order);
        }
        return "redirect:/admin/orders/" + id;
    }

    // Admin: huy don -> hoan kho; tien mat -> Da huy, chuyen khoan -> Cho hoan tien
    @PostMapping("/{id}/cancel")
    @Transactional
    public String cancel(@PathVariable Integer id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        String cur = order.getStatus();
        if ("PROCESSING".equals(cur) || "COMPLETED".equals(cur)) {
            for (OrderItem it : order.getItems()) {           // hoan lai ton kho 1 lan
                productRepository.findById(it.getProductId()).ifPresent(p -> {
                    int stock = p.getStock() == null ? 0 : p.getStock();
                    p.setStock(stock + it.getQuantity());
                    productRepository.save(p);
                });
            }
            order.setStatus("BANK".equals(order.getPaymentMethod()) ? "REFUNDING" : "CANCELLED");
            orderRepository.save(order);
        }
        return "redirect:/admin/orders/" + id;
    }

    // Admin: xac nhan da hoan tien -> Da huy (chi tu trang thai Cho hoan tien)
    @PostMapping("/{id}/confirm-payment")
    @Transactional
    public String confirmPayment(@PathVariable Integer id, RedirectAttributes ra) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && "PENDING_PAYMENT".equals(order.getStatus())) {
            order.setStatus("PROCESSING");
            orderRepository.save(order);
            ra.addFlashAttribute("success", "Đã xác nhận thanh toán cho đơn #" + id + ". Đơn chuyển sang trạng thái đang xử lý.");
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/refunded")
    @Transactional
    public String refunded(@PathVariable Integer id, RedirectAttributes ra) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && "REFUNDING".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            ra.addFlashAttribute("success", "Đã hoàn tiền thành công cho đơn #" + id);
        }
        return "redirect:/admin/orders/" + id;
    }
}
