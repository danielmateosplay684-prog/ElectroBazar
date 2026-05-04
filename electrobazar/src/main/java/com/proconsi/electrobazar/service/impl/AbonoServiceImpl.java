package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.dto.AbonoRequest;
import com.proconsi.electrobazar.model.*;
import com.proconsi.electrobazar.repository.AbonoRepository;
import com.proconsi.electrobazar.repository.CustomerRepository;
import com.proconsi.electrobazar.repository.SaleRepository;
import com.proconsi.electrobazar.service.AbonoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class AbonoServiceImpl implements AbonoService {

    private final AbonoRepository abonoRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    @Override
    @Transactional
    public Abono createAbono(AbonoRequest request) {
        String cliInput = request.getClienteId();
        if (cliInput == null || cliInput.trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        cliInput = cliInput.trim();

        Customer cliente = null;
        try {
            Long id = Long.parseLong(cliInput);
            cliente = customerRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Not a number, try Document/TaxId
        }

        if (cliente == null) {
            cliente = customerRepository.findByIdDocumentNumber(cliInput).orElse(null);
        }
        if (cliente == null) {
            cliente = customerRepository.findByTaxId(cliInput).orElse(null);
        }

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado con ese identificador o documento");
        }

        Sale venta = null;
        if (request.getVentaOriginalId() != null) {
            venta = saleRepository.findById(request.getVentaOriginalId())
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        }

        // Si el tipo es DEVOLUCION, valide que el importe no supere el total de la venta original
        if (request.getTipoAbono() == TipoAbono.DEVOLUCION) {
            if (venta == null) {
                throw new IllegalArgumentException("El tipo DEVOLUCION requiere una venta original");
            }
            if (request.getImporte().compareTo(venta.getTotalAmount()) > 0) {
                throw new IllegalArgumentException("El importe del abono no puede superar el total de la venta original");
            }
        }

        // Guardamos el importe como valor positivo absoluto para facilitar los cálculos de resta en el TPV
        BigDecimal importePositivo = request.getImporte().abs();

        // Generar código secuencial: AB-YYYY-N (ej: AB-2026-1, AB-2026-2...)
        int currentYear = java.time.Year.now().getValue();
        String prefix = "AB-" + currentYear + "-";
        
        long nextNum = 1;
        java.util.Optional<Abono> lastAbono = abonoRepository.findTopByCodeStartingWithOrderByIdDesc(prefix);
        if (lastAbono.isPresent()) {
            try {
                String lastCode = lastAbono.get().getCode();
                String numPart = lastCode.substring(prefix.length());
                nextNum = Long.parseLong(numPart) + 1;
            } catch (Exception e) {
                // Si el formato no coincide, buscamos el siguiente ID disponible como fallback
                nextNum = abonoRepository.count() + 1;
            }
        }
        String code = prefix + nextNum;

        Abono abono = Abono.builder()
                .code(code)
                .ventaOriginal(venta)
                .cliente(cliente)
                .importe(importePositivo)
                .fecha(LocalDateTime.now())
                .metodoPago(request.getMetodoPago())
                .tipoAbono(request.getTipoAbono())
                .requiresFullUse(request.getRequiresFullUse() != null ? request.getRequiresFullUse() : true)
                .motivo(request.getMotivo())
                .estado(EstadoAbono.PENDIENTE)
                .fechaLimite(request.getFechaLimite() != null && !request.getFechaLimite().isBlank() ? LocalDateTime.parse(request.getFechaLimite()) : null)
                .build();
                
        // Si el tipo es CREDITO_FAVOR, no descuente caja sino que quede como saldo pendiente del cliente
        // Esta lógica de caja se maneja al confirmar (APLICADO), por ahora se queda PENDIENTE y guardado sin afectar la caja.
        
        return abonoRepository.save(abono);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Abono> getAbonosByCliente(String clienteIdOrDoc) {
        if (clienteIdOrDoc == null || clienteIdOrDoc.trim().isEmpty()) {
            return List.of();
        }
        String doc = clienteIdOrDoc.trim();
        Customer cliente = null;
        try {
            Long id = Long.parseLong(doc);
            cliente = customerRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Ignore
        }
        if (cliente == null) {
            cliente = customerRepository.findByIdDocumentNumber(doc).orElse(null);
        }
        if (cliente == null) {
            cliente = customerRepository.findByTaxId(doc).orElse(null);
        }
        if (cliente == null) {
            return List.of();
        }
        return abonoRepository.findByClienteId(cliente.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Abono> getAbonosPaged(String clienteIdOrDoc, Pageable pageable) {
        if (clienteIdOrDoc == null || clienteIdOrDoc.trim().isEmpty()) {
            return abonoRepository.findAll(pageable);
        }
        String doc = clienteIdOrDoc.trim();
        Customer cliente = null;
        try {
            Long id = Long.parseLong(doc);
            cliente = customerRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // not a number
        }
        if (cliente == null) cliente = customerRepository.findByIdDocumentNumber(doc).orElse(null);
        if (cliente == null) cliente = customerRepository.findByTaxId(doc).orElse(null);
        if (cliente == null) return Page.empty(pageable);
        return abonoRepository.findByClienteId(cliente.getId(), pageable);
    }


    @Override
    @Transactional
    public void anularAbono(Long id) {
        Abono abono = abonoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado"));

        if (abono.getEstado() != EstadoAbono.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden anular abonos en estado PENDIENTE");
        }

        abono.setEstado(EstadoAbono.ANULADO);
        abonoRepository.save(abono);
    }

    @Override
    @Transactional
    public void deleteAbono(Long id) {
        Abono abono = abonoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado"));

        if (abono.getEstado() != EstadoAbono.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden eliminar abonos en estado PENDIENTE");
        }

        abonoRepository.delete(abono);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Abono> findByCode(String code) {
        if (code == null || code.isBlank()) return java.util.Optional.empty();
        return abonoRepository.findByCode(code.trim().toUpperCase())
                .filter(a -> a.getEstado() == EstadoAbono.PENDIENTE);
    }
}
