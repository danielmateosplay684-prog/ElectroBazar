package com.proconsi.electrobazar.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportServiceImpl implements PdfReportService {

    private final TemplateEngine templateEngine;
    private final com.proconsi.electrobazar.repository.SaleReturnRepository saleReturnRepository;

    @Override
    public byte[] generateCashCloseReport(CashRegister register) {
        log.info("Generating cash close PDF report for Register ID {}", register.getId());
        try {
            // 1. Prepare Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("register", register);

            // Fetch returns for this session
            java.time.LocalDateTime start = register.getOpeningTime() != null ? register.getOpeningTime()
                    : register.getRegisterDate().atStartOfDay();
            java.time.LocalDateTime end = register.getClosedAt() != null ? register.getClosedAt()
                    : java.time.LocalDateTime.now();
            List<com.proconsi.electrobazar.model.SaleReturn> returns = saleReturnRepository
                    .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
            context.setVariable("returns", returns);

            // 2. Process HTML template
            String htmlContent = templateEngine.process("reports/cash-close-report", context);

            // 3. Convert HTML to PDF using OpenHTMLToPDF in-memory
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(htmlContent, "classpath:/templates/");
                builder.toStream(os);
                builder.run();

                byte[] pdfBytes = os.toByteArray();
                log.info("Cash close PDF generated successfully (Size: {} bytes)", pdfBytes.length);
                return pdfBytes;
            }

        } catch (Exception e) {
            log.error("Error generating cash close PDF report for Register ID " + register.getId(), e);
            throw new RuntimeException("Error generating PDF report: " + e.getMessage(), e);
        }
    }
}
