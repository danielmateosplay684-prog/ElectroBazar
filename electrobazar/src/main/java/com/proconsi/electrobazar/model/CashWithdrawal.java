package com.proconsi.electrobazar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_withdrawals", indexes = {
        @Index(name = "idx_withdrawals_register_id", columnList = "cash_register_id"),
        @Index(name = "idx_withdrawals_session_id", columnList = "cash_session_id"),
        @Index(name = "idx_withdrawals_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashWithdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ESTO ES LO QUE BUSCA HIBERNATE: La relación mappedBy="cashSession"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_session_id", nullable = false)
    @JsonIgnore
    private CashRegister cashSession;

    // Columna fantasma
    @Column(name = "cash_register_id", nullable = false)
    private Long cashRegisterId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MovementType type = MovementType.WITHDRAWAL;

    public enum MovementType {
        WITHDRAWAL,
        ENTRY
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // Rellenamos el ID fantasma automáticamente
        if (this.cashSession != null && this.cashRegisterId == null) {
            this.cashRegisterId = this.cashSession.getId();
        }
    }
}