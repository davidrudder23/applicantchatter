package controllers;

import models.Employee;
import models.Employer;
import play.Logger;
import play.cache.Cache;
import play.libs.Images;
import play.mvc.Controller;
import utils.StringUtils;

public class Security extends Controller {
	
	public static void login(String email, String password) {
		String hashedPassword = Employee.hashPassword(email, password);
		
		Employee employee = Employee.find("byEmailAndHashedPassword", email, hashedPassword).first();
		if (employee == null) {
			flash.error("login failed");
			Application.index();
		}
		
		session.put("employeeId", employee.id);
		
		Account.account();
	}
	
	public static void internalLogin(Long employeeId) {
		if (employeeId == null) {
			flash.error("login failed");
			Application.index();
		}
		
		Logger.debug ("Internal login with employee #%d", employeeId);
		
		session.put("employeeId", employeeId);
		
		Account.account();
	}

	
	public static void logout() {
		session.put ("employeeId", 0);
		Application.index();
	}
	
	public static Employee getLoggedInEmployee() {
		String employeeIdString = session.get("employeeId");
		if (StringUtils.isEmpty(employeeIdString)) {
			return null;
		}
		
		Long employeeId = new Long(0);
		try {
			employeeId = Long.parseLong(employeeIdString);
		} catch (NumberFormatException nfExc) {}
		
		return Employee.findById(employeeId);
	}
	
	public static void captcha(String id) {
	    Images.Captcha captcha = Images.captcha();
	    String code = captcha.getText("#E4EAFD");
	    Logger.debug ("storing captcha code at %s with %s", id, code);
	    Cache.set("captcha-"+id, code, "10mn");
	    renderBinary(captcha);
	}
	
	public static boolean confirmCaptcha(String id, String code) {
		String storedCode = Cache.get("captcha-"+id, String.class);
		
		Logger.debug ("captcha id=%s code=%s storedCode=%s", id, code, storedCode);
		if (StringUtils.isEmpty(storedCode)) {
			return false;
		}
		
		if (!storedCode.equalsIgnoreCase(code)) {
			return false;
		}
		return true;
	}

	public static void ajaxConfirmCaptcha(String id, String code) {
		String storedCode = Cache.get("captcha-"+id, String.class);
		
		Logger.debug ("captcha id=%s code=%s storedCode=%s", id, code, storedCode);
		if (StringUtils.isEmpty(storedCode)) {
			renderText("false");
		}
		
		if (!storedCode.equalsIgnoreCase(code)) {
			renderText("false");
		}
		renderText("true");
	}

}
