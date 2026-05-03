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

    public void sendPasswordResetEmail(String email, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Password Reset Request - ShopEase");
        
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>" +
                "<h2 style='color: #2ecc71;'>ShopEase</h2>" +
                "<p>Hello,</p>" +
                "<p>We received a request to reset your password. If you didn't make this request, you can safely ignore this email.</p>" +
                "<p>To reset your password, click the button below:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetLink + "' style='background-color: #2ecc71; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Reset Password</a>" +
                "</div>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>Best regards,<br>The ShopEase Team</p>" +
                "</div>";
                
        helper.setText(content, true);
        mailSender.send(message);
    }

    public void sendMembershipConfirmationEmail(in.ds.ShopEase.model.User user, String planName, java.time.LocalDateTime expiryDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject("Welcome to ShopEase Elite!");
        
        String expiryStr = expiryDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
        
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px; background-color: #fcfcfc;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'><h1 style='color: #ffc107; margin: 0;'>⭐ ShopEase Elite</h1></div>" +
                "<p>Dear " + user.getFirstName() + ",</p>" +
                "<p>Congratulations! You are now an official member of <strong>ShopEase Elite</strong>.</p>" +
                "<div style='background-color: #fff8e1; padding: 15px; border-radius: 8px; border-left: 5px solid #ffc107; margin: 20px 0;'>" +
                "<p style='margin: 5px 0;'><strong>Plan:</strong> Elite " + planName + "</p>" +
                "<p style='margin: 5px 0;'><strong>Valid Until:</strong> " + expiryStr + "</p>" +
                "</div>" +
                "<h3>Your Elite Benefits:</h3>" +
                "<ul>" +
                "<li><strong>10% Extra Discount</strong> on every purchase</li>" +
                "<li><strong>Free Express Delivery</strong> on all orders</li>" +
                "<li><strong>Priority Support</strong> and exclusive access</li>" +
                "</ul>" +
                "<p>Start enjoying your benefits today!</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://localhost:8087/shop' style='background-color: #1a1a1a; color: #ffc107; padding: 12px 25px; text-decoration: none; border-radius: 50px; font-weight: bold; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>Go to Shop</a>" +
                "</div>" +
                "<p>Best regards,<br>The ShopEase Elite Team</p>" +
                "</div>";
                
        helper.setText(content, true);
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
