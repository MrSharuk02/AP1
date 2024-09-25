package com.Sharfuddin.Reg;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/SendOtpServlet")
public class SendOtpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        HttpSession session = request.getSession();
        session.setAttribute("email", email);

        try  {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Registration", "root", "Sharfoddin@28");
            String query = "SELECT * FROM users where uemail=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Generate OTP and set expiration time
                        String otp = OTPUtil.generateOTP();
                        Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000); // 5 min expiry

                        // Store OTP in the database
                        String updateQuery = "UPDATE users SET otp=?, otp_expiration=? WHERE uemail=?";
                        try (PreparedStatement updatePs = con.prepareStatement(updateQuery)) {

							try {
								String hashotp = hashPassword(otp);
								updatePs.setString(1, hashotp);
	                            updatePs.setTimestamp(2, expirationTime);
	                            updatePs.setString(3, email);
	                            updatePs.executeUpdate();
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

                        }

                        // Send OTP via email
                        sendOtpEmail(email, otp);
                        response.sendRedirect("jsp/OtpVerification.jsp");
                    } else {
                        response.getWriter().println("Email not found!");
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException | MessagingException e) {
            e.printStackTrace();
            response.getWriter().println("Error occurred.");
        }
    }

    private void sendOtpEmail(String email, String otp) throws MessagingException, IOException {
    	final String username = "loveboy9160@gmail.com";  // your Gmail address
     	final String password = "fxehlgzwxobzqsoi";     // the 16-character App password generated

        // SMTP server settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a mail session with authentication
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(username, password);
            }
        });

        // Create the message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Your OTP for Password Reset");

        // HTML content for the email
        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 0; margin: 0; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #fff; border-radius: 10px; }"
                + ".header { text-align: center; background-color: #4CAF50; padding: 10px; color: #fff; }"
                + ".header img { max-width: 100%; height: auto; }"  // Adjusted this line
                + ".content { padding: 20px; text-align: center; }"
                + ".otp { font-size: 22px; font-weight: bold; margin: 20px 0; color: #333; }"
                + ".cta-button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }"
                + ".cta-button:hover { background-color: #45a049; }"
                + ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }"
                + ".social-icons img { width: 30px; margin: 0 5px; }"
                + "@media only screen and (max-width: 600px) {"
                + ".container { padding: 10px; }"
                + "}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<img src='cid:logoImage' alt='Company Logo'/>"
                + "<h2>Your OTP for Password Reset</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Hello,</p>"
                + "<p>Please use the following OTP to reset your password. This OTP is valid for 5 minutes:</p>"
                + "<div class='otp'>" + otp + "</div>"
                + "<a href='http://localhost:2002/ArilineProject/jsp/OtpVerification.jsp' class='cta-button'>Reset Your Password</a>"
                + "<p>If you didn’t request this, please ignore this email.</p>"
                + "<img src='cid:otpGif' alt='OTP GIF' style='width:100%; margin-top:20px; border-radius:10px;'/>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>Follow us on:</p>"
                + "<div class='social-icons'>"
                + "<a href='https://facebook.com'><img src='cid:facebookIcon' alt='Facebook'></a>"
                + "<a href='https://twitter.com'><img src='cid:twitterIcon' alt='Twitter'></a>"
                + "<a href='https://instagram.com'><img src='cid:instagramIcon' alt='Instagram'></a>"
                + "</div>"
                + "<p>&copy; 2024 AirLine Reservation. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";


        // Create a multipart message for HTML with embedded images
        MimeMultipart multipart = new MimeMultipart("related");

        // HTML part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html");
        multipart.addBodyPart(htmlPart);

        // Add embedded logo image
        MimeBodyPart logoPart = new MimeBodyPart();
        logoPart.attachFile(getServletContext().getRealPath("/images/qutar.jpg")); // Replace with the actual path to your logo
        logoPart.setContentID("<logoImage>");
        logoPart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(logoPart);

        // Add embedded OTP GIF
        MimeBodyPart gifPart = new MimeBodyPart();
        gifPart.attachFile(getServletContext().getRealPath("/images/otp.jpg")); // Replace with the actual path to your OTP GIF
        gifPart.setContentID("<otpGif>");
        gifPart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(gifPart);

        // Add embedded social media icons
        MimeBodyPart fbIconPart = new MimeBodyPart();
        fbIconPart.attachFile(getServletContext().getRealPath("/images/fb.png")); // Replace with the actual path to Facebook icon
        fbIconPart.setContentID("<facebookIcon>");
        fbIconPart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(fbIconPart);

        MimeBodyPart twitterIconPart = new MimeBodyPart();
        twitterIconPart.attachFile(getServletContext().getRealPath("/images/x.png")); // Replace with the actual path to Twitter icon
        twitterIconPart.setContentID("<twitterIcon>");
        twitterIconPart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(twitterIconPart);

        MimeBodyPart instaIconPart = new MimeBodyPart();
        instaIconPart.attachFile(getServletContext().getRealPath("/images/insta.png")); // Replace with the actual path to Instagram icon
        instaIconPart.setContentID("<instagramIcon>");
        instaIconPart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(instaIconPart);

        // Set the email content
        message.setContent(multipart);

        // Send the message
        Transport.send(message);
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }
}





