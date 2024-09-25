package com.Sharfuddin.Reg;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uemail = request.getParameter("username");
		String upwd = request.getParameter("password");
		String hashedPassword = null;
		try {
			hashedPassword = hashPassword(upwd);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSession session = request.getSession();
		RequestDispatcher dispatcher = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/registration?useSSL=false", "root", "Sharfoddin@28");
			PreparedStatement ps = con.prepareStatement("select * from users where uemail = ? and upwd = ?");
			ps.setString(1,uemail);
			ps.setString(2, hashedPassword);



			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				session.setAttribute("name", rs.getString("uname"));
				session.setAttribute("user", rs.getString("uemail"));
			dispatcher = request.getRequestDispatcher("index.jsp");
			}
			else {
				request.setAttribute("status", "failed");
				dispatcher = request.getRequestDispatcher("login.jsp");
			}
			dispatcher.forward(request,response);
		} catch (Exception e) {
			// TODO: handle exception
			e.setStackTrace(null);
		}
		}
	 private String hashPassword(String password) throws NoSuchAlgorithmException {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hashedBytes = md.digest(password.getBytes());
	        return Base64.getEncoder().encodeToString(hashedBytes); // Convert to Base64 encoded string
	    }

}