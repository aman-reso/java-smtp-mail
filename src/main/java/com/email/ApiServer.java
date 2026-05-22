package com.email;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class ApiServer {

    private static final String SENDER_EMAIL = "admin@cockroachjantapartyjoin.in";
    private static final String APP_PASSWORD = "izlzibvcwqlsmejz";

    private static final String HTML_TEMPLATE = "<div style=\"background-color: #050505; color: #f3f4f6; font-family: Arial, sans-serif; padding: 40px; text-align: center; border: 2px solid #222; border-radius: 12px; max-width: 600px; margin: auto;\">\n" +
            "    \n" +
            "    <div style=\"font-size: 50px; margin-bottom: 10px;\">🪳</div>\n" +
            "    \n" +
            "    <h1 style=\"color: #ff6b35; font-family: Impact, sans-serif; text-transform: uppercase; letter-spacing: 2px;\">\n" +
            "        WELCOME TO THE SWARM, {{name}}!\n" +
            "    </h1>\n" +
            "    \n" +
            "    <p style=\"font-size: 16px; color: #cccccc; line-height: 1.6; margin-bottom: 30px;\">\n" +
            "        You have officially joined the <strong>Cockroach Janta Party</strong>. <br>\n" +
            "        They tried to step on us, but we came back. You are now the voice of the lazy, the unemployed, and the chronically correct.\n" +
            "    </p>\n" +
            "    \n" +
            "    <div style=\"background-color: #111111; padding: 20px; border-radius: 8px; border: 1px solid #333333; margin: 30px 0;\">\n" +
            "        <h3 style=\"color: #4ade80; font-family: monospace; margin-top: 0; font-size: 18px;\">YOUR SURVIVAL KIT:</h3>\n" +
            "        <ul style=\"text-align: left; color: #dddddd; font-size: 15px; line-height: 1.8;\">\n" +
            "            <li>1. Stay hidden when the lights are on.</li>\n" +
            "            <li>2. Only emerge at 2 AM to demand your rights.</li>\n" +
            "            <li>3. Never apologize for being lazy.</li>\n" +
            "            <li>4. Multiply! Share the party link: <br><a href=\"https://cockroachjantapartyjoin.in\" style=\"color: #4ade80; font-weight: bold; text-decoration: none;\">cockroachjantapartyjoin.in</a></li>\n" +
            "        </ul>\n" +
            "    </div>\n" +
            "    \n" +
            "    <p style=\"font-size: 12px; color: #666666; margin-top: 40px;\">\n" +
            "        If you received this by mistake, just hide under the fridge. <br>\n" +
            "        © 2026 Cockroach Janta Party. All rights survived.\n" +
            "    </p>\n" +
            "\n" +
            "</div>";

    public static void main(String[] args) throws IOException {
        String envPort = System.getenv("PORT");
        int port = (envPort != null && !envPort.isEmpty()) ? Integer.parseInt(envPort) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/send-email", new SendEmailHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("✅ API Server is running on http://localhost:" + port);
        System.out.println("👉 Endpoint: POST http://localhost:" + port + "/send-email");
    }

    static class SendEmailHandler implements HttpHandler {
        private final Gson gson = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle CORS for Postman or Web clients
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                
                EmailRequest request;
                try {
                    request = gson.fromJson(body, EmailRequest.class);
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
                    return;
                }

                if (request == null || request.to == null || request.to.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\": \"'to' field is required\"}");
                    return;
                }

                String toAddress = request.to;
                String subject = (request.subject != null) ? request.subject : "Welcome to the Cockroach Janta Party!";
                String name = (request.name != null) ? request.name : "Comrade";
                String messageBody = HTML_TEMPLATE.replace("{{name}}", name);

                // Send Email Logic in Background
                new Thread(() -> {
                    sendEmail(toAddress, subject, messageBody);
                }).start();

                sendResponse(exchange, 200, "{\"message\": \"Email request received. It is being sent in the background!\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Only POST method is allowed\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class EmailRequest {
        String to;
        String subject;
        String name;
        String body;
    }

    private static boolean sendEmail(String toAddress, String subject, String messageBody) {
        String host = "smtp.gmail.com"; 
        String port = "587"; 

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); 

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            message.setContent(messageBody, "text/html; charset=utf-8");

            System.out.println("🚀 API Request: Sending email to " + toAddress + "...");
            Transport.send(message);
            System.out.println("✅ Email successfully sent!");
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email:");
            e.printStackTrace();
            return false;
        }
    }
}
