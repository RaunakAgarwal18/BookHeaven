package com.bookheaven.email_service.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.bookheaven.common.dto.event.OrderShippedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfInvoiceGenerator {

    private final TemplateEngine templateEngine;

    public String generateInvoicePdfBase64(OrderShippedEvent event) {
        try {
            Context context = new Context();
            context.setVariable("username", event.getUsername());
            context.setVariable("shippingAddress", event.getShippingAddress());
            context.setVariable("orderId", event.getOrderId());
            context.setVariable("paymentMethod", event.getPaymentMethod() != null ? event.getPaymentMethod() : "N/A");
            context.setVariable("items", event.getItems());
            context.setVariable("subtotal", event.getSubtotal() != null ? event.getSubtotal() : 0.0);
            context.setVariable("taxAmount", event.getTaxAmount() != null ? event.getTaxAmount() : 0.0);
            context.setVariable("shippingAmount", event.getShippingAmount() != null ? event.getShippingAmount() : 0.0);
            context.setVariable("discountAmount", event.getDiscountAmount() != null ? event.getDiscountAmount() : 0.0);
            context.setVariable("totalAmount", event.getTotalAmount() != null ? event.getTotalAmount() : 0.0);
            context.setVariable("currency", event.getCurrency() != null ? event.getCurrency() : "$");

            String htmlContent = templateEngine.process("invoice-template", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlContent, null);
                builder.toStream(outputStream);
                builder.run();

                byte[] pdfBytes = outputStream.toByteArray();
                return Base64.getEncoder().encodeToString(pdfBytes);
            }
        } catch (Exception e) {
            log.error("Failed to generate PDF invoice for order {}", event.getOrderId(), e);
            return null; // Return null gracefully, email sender will ignore missing attachment
        }
    }
}
