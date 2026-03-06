package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Product;
import com.proconsi.electrobazar.service.IneApiService;
import com.proconsi.electrobazar.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ipc")
@RequiredArgsConstructor
public class IneApiRestController {

    private final IneApiService ineApiService;
    private final ProductService productService;

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentIpc() {
        BigDecimal ipcValue = ineApiService.getLatestIpc();
        Map<String, Object> response = new HashMap<>();
        response.put("ipcValue", ipcValue);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preview")
    public ResponseEntity<List<Map<String, Object>>> getIpcPreview(@RequestParam BigDecimal percentage) {
        List<Product> topProducts = productService.getTopProducts(5);
        List<Map<String, Object>> preview = new ArrayList<>();

        for (Product p : topProducts) {
            BigDecimal currentPrice = p.getPrice();
            BigDecimal multiplier = BigDecimal.ONE
                    .add(percentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            BigDecimal newPrice = currentPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("productName", p.getName());
            item.put("currentPrice", currentPrice);
            item.put("newPrice", newPrice);
            preview.add(item);
        }

        return ResponseEntity.ok(preview);
    }
}
