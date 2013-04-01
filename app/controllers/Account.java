package controllers;

import java.util.List;

import org.hibernate.tool.hbm2x.StringUtils;

import models.Answer;
import models.Employee;
import models.Employer;
import models.Question;
import play.Logger;
import play.libs.Codec;
import play.mvc.Controller;

public class Account extends Controller {
	
	public static void signup() {
    	String captchaId = Codec.UUID();

		render(captchaId);
	}
	
	public static void signupSubmit(Employer employer, Employee employee, String password, String passwordVerify,String captchaId, String captchaCode) {
		
		if ((employee == null) || (employer == null) || 
				(StringUtils.isEmpty(employer.name)) || 
				(StringUtils.isEmpty(employee.email)) || (StringUtils.isEmpty(password))){
			flash.error ("Please fill in all the fields");
			signup();
		}
		
		if (password.length()<5) {
			flash.error("Your password must be at least 5 characters long");
			signup();
		}
		
		if (!password.equals(passwordVerify)) {
			flash.error ("Password and verify do not match");
			signup();
		}
		
		if (Employer.count("lower(name)", employer.name.toLowerCase())>0) {
			flash.error ("That company name is already taken");
			signup();
		}
		
		if (Employee.count("lower(email)", employee.email.toLowerCase())>0) {
			flash.error ("That company name is already taken");
			signup();
		}
		
    	if (!Security.confirmCaptcha(captchaId, captchaCode)) {
    		flash.error ("Captcha did not match");
    		signup();
    	}

		
		employer = employer.save();
		
		employee.employer = employer;
		employee.hashedPassword = Employee.hashPassword(employee.email, password);
		
		employee = employee.save();
		employee = Employee.find("byEmail", employee.email).first();
		
		Logger.debug ("in signupsubmit, the employee id is %s", employee.id);
		Security.internalLogin(employee.id);
	}
	
	public static void account() {
		Employee loggedInEmployee = Security.getLoggedInEmployee();
		if (loggedInEmployee == null) {
			flash.error("Your session has timed out.  Please login again.");
			Application.index();
		}
		
		Employer employer = loggedInEmployee.employer;
		
		if (employer == null) {
			flash.error ("This account can not login for an employer");
			Application.index();
		}
		
		List<Employee> employees = Employee.find("byEmployer", employer).fetch();
		
		Logger.debug ("Found %d employees", employees.size());
		
		List<Question> questions = Question.find("byEmployer", employer).fetch();
		
		render(loggedInEmployee, employer, employees, questions);
	}

	public static void editEmployer(Employer employer) {
		employer.save();
		account();
	}
	
	
	public static void deleteQuestion(Long questionId) {
		Employee employee = Security.getLoggedInEmployee();
		Employer employer = employee.employer;
		
		Question question = Question.findById(questionId);
		
		if ((question != null) && (question.employer.equals(employer))) {
			
			List<Answer> answers = Answer.find("byQuestion", question).fetch();
			for (Answer answer: answers) {
				answer.delete();
			}
			
			question.delete();
		}
		
		account();
	}
	
	public static void addEmployee(Long employer_id, Employee employee, String password, String password_verify) {
		Employer employer = Employer.findById(employer_id);
		if (employer == null) {
			error("I'm sorry, that didn't work.  Please try again and contact us if you still have problems.");
			signup();
		}
		
		Logger.debug ("Employee=%s", employee);
		
		String hashedPassword = Employee.hashPassword(employee.email, password);
		
		employee.employer = employer;
		employee.hashedPassword = hashedPassword;
		employee.save();
		
		account();
		
	}
	
	public static void deleteEmployee(Long employee_id) {
		Employee.delete("byId",  employee_id);
		account();
	}
	
	public static void editEmployee(Employee employee, String password, String password_verify) {
		boolean valid = true;
		if (!StringUtils.isEmpty(password) && (password.equals(password_verify))) {
			employee.hashedPassword = Employee.hashPassword(employee.email, password);
		} else if (!password.equals(password_verify)) {
			valid = false;
			flash.error ("Passwords do not match");
		}
		
		List<Employee> checkEmails = Employee.find("byEmail", employee.email).fetch();
		for (Employee checkEmail: checkEmails) {
			if (checkEmail.id != employee.id) {
				flash.error ("Email already taken");
				valid = false;
			}
		}
		
		if (valid) {
			employee.save();
		}
		account();
	}

}
