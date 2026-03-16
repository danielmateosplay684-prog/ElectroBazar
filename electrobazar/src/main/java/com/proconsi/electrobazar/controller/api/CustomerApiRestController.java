package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Customer;
import com.proconsi.electrobazar.model.Tariff;
import com.proconsi.electrobazar.repository.TariffRepository;
import com.proconsi.electrobazar.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerApiRestController {

    private final CustomerService customerService;
    private final TariffRepository tariffRepository;

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.findAllActive());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(@RequestParam String query) {
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Map<String, Object> body) {
        Customer customer = buildCustomerFromBody(body, new Customer());

        // minimal server-side validation
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (customer.getType() == Customer.CustomerType.COMPANY &&
                (customer.getTaxId() == null || customer.getTaxId().trim().isEmpty())) {
            throw new IllegalArgumentException("El CIF es obligatorio para empresas");
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.save(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("Customer update request for id={}: {}", id, body);
        Customer existing = customerService.findById(id);
        Customer updated = buildCustomerFromBody(body, existing);
        updated.setId(id);
        return ResponseEntity.ok(customerService.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private Customer buildCustomerFromBody(Map<String, Object> body, Customer customer) {
        if (body.containsKey("name"))
            customer.setName((String) body.get("name"));
        if (body.containsKey("taxId"))
            customer.setTaxId((String) body.get("taxId"));
        if (body.containsKey("email"))
            customer.setEmail((String) body.get("email"));
        if (body.containsKey("phone"))
            customer.setPhone((String) body.get("phone"));
        if (body.containsKey("address"))
            customer.setAddress((String) body.get("address"));
        if (body.containsKey("city"))
            customer.setCity((String) body.get("city"));
        if (body.containsKey("postalCode"))
            customer.setPostalCode((String) body.get("postalCode"));
        if (body.containsKey("type")) {
            try {
                customer.setType(Customer.CustomerType.valueOf((String) body.get("type")));
            } catch (Exception ignored) {
                customer.setType(Customer.CustomerType.INDIVIDUAL);
            }
        }
        if (customer.getType() == null)
            customer.setType(Customer.CustomerType.INDIVIDUAL);

        if (body.containsKey("active"))
            customer.setActive(Boolean.TRUE.equals(body.get("active")));
        if (customer.getActive() == null)
            customer.setActive(true);

        if (body.containsKey("hasRecargoEquivalencia"))
            customer.setHasRecargoEquivalencia(Boolean.TRUE.equals(body.get("hasRecargoEquivalencia")));
        if (customer.getHasRecargoEquivalencia() == null)
            customer.setHasRecargoEquivalencia(false);

        // Resolve tariff
        Tariff tariff = null;
        if (body.containsKey("tariffId") && body.get("tariffId") != null) {
            Long tariffId = Long.parseLong(body.get("tariffId").toString());
            tariff = tariffRepository.findById(tariffId).orElse(null);
        } else if (body.containsKey("tariffName") && body.get("tariffName") != null) {
            tariff = tariffRepository.findByName((String) body.get("tariffName")).orElse(null);
        }
        if (tariff != null)
            customer.setTariff(tariff);

        return customer;
    }
}
