package com.example.clothingshop.controller;

import com.example.clothingshop.dto.CartLine;
import com.example.clothingshop.entity.*;
import com.example.clothingshop.repository.CartItemRepository;
import com.example.clothingshop.repository.OrderRepository;
import com.example.clothingshop.repository.ProductRepository;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Value("${shop.bank.id}")   private String bankId;
    @Value("${shop.bank.account}") private String bankAccount;
    @Value("${shop.bank.name}") private String bankName;

    private final CartItemRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public CartController(CartItemRepository cartRepository, ProductRepository productRepository,
                          OrderRepository orderRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // ----- Them san pham vao gio -----
    @PostMapping("/add")
    @Transactional
    public String add(@RequestParam Integer productId,
                      @RequestParam(required = false) String size,
                      @RequestParam(defaultValue = "1") Integer quantity,
                      @RequestParam(defaultValue = "add") String action,
                      RedirectAttributes ra,
                      Authentication auth) {
        if (size == null || size.isBlank()) {
            ra.addFlashAttribute("err", "Vui lòng chọn kích cỡ.");
            return "redirect:/products/" + productId;
        }
        User u = currentUser(auth);
        int qty = Math.max(1, quantity);
        CartItem item = cartRepository.findByUserIdAndProductIdAndSize(u.getId(), productId, size)
                .orElseGet(() -> {
                    CartItem c = new CartItem();
                    c.setUserId(u.getId());
                    c.setProductId(productId);
                    c.setSize(size);
                    c.setQuantity(0);
                    return c;
                });
        item.setQuantity(item.getQuantity() + qty);
        cartRepository.save(item);

        // MUA NGAY -> sang gio hang; THEM VAO GIO -> o lai trang sp + thong bao
        if ("buy".equals(action)) {
            return "redirect:/cart";
        }
        ra.addFlashAttribute("added", true);
        return "redirect:/products/" + productId;
    }

    // ----- Xem gio hang -----
    @GetMapping
    @Transactional(readOnly = true)
    public String viewCart(Authentication auth, Model model) {
        User u = currentUser(auth);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartRepository.findByUserId(u.getId())) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            lines.add(new CartLine(p, ci.getSize(), ci.getQuantity(), sub));
            total = total.add(sub);
        }

        model.addAttribute("lines", lines);
        model.addAttribute("total", total);
        model.addAttribute("user", u);
        model.addAttribute("bankId", bankId);
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("bankName", bankName);
        return "cart";
    }

    // ----- Doi so luong -----
    @PostMapping("/update")
    @Transactional
    public String update(@RequestParam Integer productId, @RequestParam(required = false) String size,
                         @RequestParam Integer quantity, Authentication auth) {
        User u = currentUser(auth);
        cartRepository.findByUserIdAndProductIdAndSize(u.getId(), productId, size).ifPresent(ci -> {
            if (quantity <= 0) cartRepository.delete(ci);
            else { ci.setQuantity(quantity); cartRepository.save(ci); }
        });
        return "redirect:/cart";
    }

    // ----- Xoa 1 san pham khoi gio -----
    @PostMapping("/remove")
    @Transactional
    public String remove(@RequestParam Integer productId, @RequestParam(required = false) String size,
                         Authentication auth) {
        User u = currentUser(auth);
        cartRepository.findByUserIdAndProductIdAndSize(u.getId(), productId, size).ifPresent(cartRepository::delete);
        return "redirect:/cart";
    }

    // ----- Dat hang: tao Order tu gio, xoa gio -----
    @PostMapping("/checkout")
    @Transactional
    public String checkout(@RequestParam String recipientName,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam(required = false) String note,
                           @RequestParam(defaultValue = "CASH") String paymentMethod,
                           Authentication auth, RedirectAttributes ra) {
        User u = currentUser(auth);
        List<CartItem> items = cartRepository.findByUserId(u.getId());
        if (items.isEmpty()) {
            ra.addFlashAttribute("err", "Giỏ hàng đang trống.");
            return "redirect:/cart";
        }

        // Kiem tra du ton kho truoc khi dat
        for (CartItem ci : items) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            int stock = p.getStock() == null ? 0 : p.getStock();
            if (stock < ci.getQuantity()) {
                ra.addFlashAttribute("err", "Sản phẩm \"" + p.getName() + "\" không đủ hàng (còn " + stock + ").");
                return "redirect:/cart";
            }
        }

        Order order = new Order();
        order.setUserId(u.getId());
        order.setRecipientName(recipientName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        boolean isBank = "BANK".equals(paymentMethod);
        order.setPaymentMethod(isBank ? "BANK" : "CASH");
        order.setStatus(isBank ? "PENDING_PAYMENT" : "PROCESSING");

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());   // snapshot ten + gia luc dat
            oi.setSize(ci.getSize());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.addItem(oi);

            p.setStock(p.getStock() - ci.getQuantity());   // tru ton kho ngay khi dat
            productRepository.save(p);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);

        orderRepository.save(order);          // cascade luu luon order_items
        cartRepository.deleteByUserId(u.getId());
        return "redirect:/orders/" + order.getId();
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Khong tim thay tai khoan dang nhap"));
    }
}
