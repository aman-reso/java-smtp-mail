package com.email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpEmailSender {

    public static void main(String[] args) {
        
        // Google SMTP Server Details
        String host = "smtp.gmail.com"; 
        String port = "587"; 
        
        // Sender details
        final String username = System.getenv("SMTP_USERNAME"); 
        final String password = System.getenv("SMTP_PASSWORD"); 
        
        // Configurable target email: Uses command line argument if passed, else defaults to amanktor@gmail.com
        String toAddress = (args.length > 0) ? args[0] : "amanktor@gmail.com";

        // SMTP Connection properties
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); 
        
        System.out.println("Connecting to Google SMTP...");
        System.out.println("Sending email from: " + username);
        System.out.println("Sending email to: " + toAddress);

        // Authenticate session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            
            message.setSubject("Google Workspace SMTP Test - Success");
            message.setText("Hello,\n\nYe aapke apne Java code se bheja gaya test mail hai!");

            System.out.println("Email bhej rahe hain...");
            Transport.send(message);
            System.out.println("Email successfully bhej diya gaya! 🚀");

        } catch (MessagingException e) {
            System.out.println("❌ Email bhejne me error aayi:");
            e.printStackTrace();
        }
    }
}
