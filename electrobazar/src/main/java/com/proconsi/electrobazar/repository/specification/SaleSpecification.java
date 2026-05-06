package com.proconsi.electrobazar.repository.specification;

import com.proconsi.electrobazar.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleSpecification {

    private static final Logger log = LoggerFactory.getLogger(SaleSpecification.class);

    public static Specification<Sale> filterSales(String search, String type, String method, LocalDate date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            log.info("[SPEC] filterSales called: type='{}', method='{}', search='{}', date='{}'", type, method, search, date);

            // Joins — only created when search is active (avoids unnecessary JOINs and duplicate rows)
            if (search != null && !search.trim().isEmpty()) {
                String cleanSearch = "%" + search.trim().toLowerCase() + "%";
                Join<Sale, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                Join<Sale, Invoice>  invoiceJoin  = root.join("invoice",  JoinType.LEFT);
                Join<Sale, Ticket>   ticketJoin   = root.join("ticket",   JoinType.LEFT);
                query.distinct(true);
                Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(invoiceJoin.get("invoiceNumber")), cleanSearch),
                    cb.like(cb.lower(ticketJoin.get("ticketNumber")),   cleanSearch),
                    cb.like(cb.lower(customerJoin.get("name")),         cleanSearch),
                    cb.like(cb.lower(customerJoin.get("taxId")),        cleanSearch),
                    cb.like(cb.concat("#", root.get("id").as(String.class)), cleanSearch)
                );
                predicates.add(searchPredicate);
            }

            // 2. TYPE FILTER — direct tipoDocumento column comparison, no JOIN needed
            if (type != null && !type.isBlank()) {
                log.info("[SPEC] Applying type filter: '{}'", type);
                if ("factura".equalsIgnoreCase(type)) {
                    predicates.add(cb.equal(root.get("tipoDocumento"), TipoDocumento.FACTURA_COMPLETA));
                } else if ("ticket".equalsIgnoreCase(type)) {
                    predicates.add(cb.equal(root.get("tipoDocumento"), TipoDocumento.FACTURA_SIMPLIFICADA));
                }
            }

            // 3. PAYMENT METHOD FILTER
            if (method != null && !method.isBlank()) {
                try {
                    PaymentMethod pm = PaymentMethod.valueOf(method.toUpperCase());
                    predicates.add(cb.equal(root.get("paymentMethod"), pm));
                    log.info("[SPEC] Applying method filter: '{}'", pm);
                } catch (IllegalArgumentException e) {
                    log.warn("[SPEC] Unknown payment method value: '{}'", method);
                }
            }

            // 4. DATE FILTER
            if (date != null) {
                predicates.add(cb.between(root.get("createdAt"), date.atStartOfDay(), date.atTime(23, 59, 59)));
            }

            // 5. DEFAULT ORDER
            query.orderBy(cb.desc(root.get("createdAt")));

            log.info("[SPEC] Total predicates: {}", predicates.size());
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}