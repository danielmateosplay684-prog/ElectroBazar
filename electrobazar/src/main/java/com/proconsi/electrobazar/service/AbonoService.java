package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.dto.AbonoRequest;
import com.proconsi.electrobazar.model.Abono;

import java.util.List;

public interface AbonoService {
    Abono createAbono(AbonoRequest request);
    List<Abono> getAbonosByCliente(String clienteIdOrDoc);
    void anularAbono(Long id);
}
