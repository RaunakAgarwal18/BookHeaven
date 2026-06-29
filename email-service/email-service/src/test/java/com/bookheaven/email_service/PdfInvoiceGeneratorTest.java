package com.bookheaven.email_service;

import com.bookheaven.common.dto.event.OrderShippedEvent;
import com.bookheaven.email_service.util.PdfInvoiceGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

@SpringBootTest
public class PdfInvoiceGeneratorTest {

    @Autowired
    private PdfInvoiceGenerator pdfInvoiceGenerator;

    @Test
    public void testGenerateInvoicePdf() throws Exception {
        OrderShippedEvent event = new OrderShippedEvent();
        event.setUsername("John Doe");
        event.setShippingAddress("123 Book Lane, Library City, BK 12345");
        event.setOrderId("ABC123XYZ");
        event.setPaymentMethod("Credit Card (ending in 1234)");
        event.setSubtotal(45.00);
        event.setTaxAmount(4.50);
        event.setShippingAmount(5.00);
        event.setDiscountAmount(2.00);
        event.setTotalAmount(52.50);
        event.setCurrency("₹");

        OrderShippedEvent.ShippedItem item1 = new OrderShippedEvent.ShippedItem("The Great Gatsby", 1, 20.00);
        OrderShippedEvent.ShippedItem item2 = new OrderShippedEvent.ShippedItem("1984 by George Orwell", 2, 12.50);
        event.setItems(List.of(item1, item2));

        String base64Pdf = pdfInvoiceGenerator.generateInvoicePdfBase64(event);
        if (base64Pdf != null) {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            try (FileOutputStream fos = new FileOutputStream("test-invoice.pdf")) {
                fos.write(pdfBytes);
            }
            System.out.println("SUCCESS: PDF generated at test-invoice.pdf");
        } else {
            System.err.println("FAILED: Base64 PDF is null");
        }
    }
}
