package com.proconsi.electrobazar.repository.specification;

import com.proconsi.electrobazar.model.Sale;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic {@link Sale} filtering in the admin panel.
 */
public class SaleSpecification {

    public static Specification<Sale> filterSales(String search, String type, String method, LocalDate date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Optimization: Fetch customer and worker only when not counting
            if (Long.class != query.getResultType()) {
                root.fetch("customer", JoinType.LEFT);
                root.fetch("worker", JoinType.LEFT);
                root.fetch("invoice", JoinType.LEFT);
                root.fetch("ticket", JoinType.LEFT);
            }

            // 1. Keyword search (ID, Customer Name, Customer TaxID)
            if (search != null && !search.trim().isEmpty()) {
                String searchParam = search.trim();
                String searchPattern = "%" + searchParam.toLowerCase() + "%";
                
                Predicate custName = cb.like(cb.lower(root.get("customer").get("name")), searchPattern);
                Predicate custTaxId = cb.like(cb.lower(root.get("customer").get("taxId")), searchPattern);
                
                try {
                    Long idSearch = Long.parseLong(searchParam.replace("#", ""));
                    Predicate idPredicate = cb.equal(root.get("id"), idSearch);
                    predicates.add(cb.or(custName, custTaxId, idPredicate));
                } catch (NumberFormatException e) {
                    predicates.add(cb.or(custName, custTaxId));
                }
            }

            // 2. Type filtering
            if (type != null && !type.trim().isEmpty()) {
                if ("factura".equalsIgnoreCase(type)) {
                    predicates.add(cb.isNotNull(root.get("invoice")));
                } else if ("ticket".equalsIgnoreCase(type)) {
                    predicates.add(cb.isNotNull(root.get("ticket")));
                }
            }

            // 3. Payment Method
            if (method != null && !method.trim().isEmpty()) {
                if ("Efectivo".equalsIgnoreCase(method)) {
                    predicates.add(cb.equal(root.get("paymentMethod"), com.proconsi.electrobazar.model.PaymentMethod.CASH));
                } else if ("Tarjeta".equalsIgnoreCase(method)) {
                    predicates.add(cb.equal(root.get("paymentMethod"), com.proconsi.electrobazar.model.PaymentMethod.CARD));
                }
            }

            // 4. Exact Date
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);
                predicates.add(cb.between(root.get("createdAt"), startOfDay, endOfDay));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
