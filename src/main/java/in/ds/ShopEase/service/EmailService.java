package in.ds.ShopEase.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import in.ds.ShopEase.model.Order;
import in.ds.ShopEase.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmationEmail(Order order) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(order.getUser().getEmail());
        helper.setSubject("Order Confirmation - ShopEase");
        helper.setText("Dear " + order.getUser().getFirstName() + ",\n\nThank you for shopping with ShopEase! Your order has been confirmed.\n\nPlease find the attached bill for your reference.\n\nRegards,\nShopEase Team");

        byte[] pdfContent = generateBillPdf(order);
        helper.addAttachment("Bill_" + order.getOrderId() + ".pdf", new ByteArrayResource(pdfContent));

        mailSender.send(message);
    }

    private byte[] generateBillPdf(Order order) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("INVOICE").setBold().setFontSize(20));
        document.add(new Paragraph("Order ID: " + order.getOrderId()));
        document.add(new Paragraph("Customer: " + order.getUser().getFirstName() + " " + order.getUser().getLastName()));
        document.add(new Paragraph("Date: " + order.getCreatedAt()));
        document.add(new Paragraph("\n"));

        Table table = new Table(3);
        table.addCell("Product");
        table.addCell("Quantity");
        table.addCell("Price");

        for (OrderItem item : order.getOrderItems()) {
            table.addCell(item.getProductName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.valueOf(item.getPrice()));
        }

        document.add(table);
        document.add(new Paragraph("\nTotal Amount: Rs. " + order.getAmount()).setBold());

        document.close();
        return baos.toByteArray();
    }
}
