package controllers;

import play.*;
import play.libs.Codec;
import play.mvc.*;
import play.templates.Template;
import play.templates.TemplateLoader;
import utils.EmailUtil;
import utils.StringUtils;

import java.net.URL;
import java.util.*;

import org.apache.commons.mail.HtmlEmail;

import models.*;

public class Application extends Controller {

    public static void index() {
    	Employee employee = Security.getLoggedInEmployee();
    	if (employee != null) {
    		Account.account();
    	}
        render();
    }
    
    public static void ask(String employerName) {
    	
    	Employer employer = null;
    	List<Employer> employers = Employer.find("bySeoName", employerName).fetch();
    	if ((employers == null) || (employers.size()<=0)) {
    		employers = Employer.find("byPublicCanAsk", true).fetch();
    	}
    	
    	if (employers.size()==1) {
    		employer = employers.get(0);
    	}
    	if ((employers == null) || (employers.size()<=0)) {
    		flash.error ("ApplicantChatter is having some problems.  Please come back later");
    		Application.index();
    	}
    	
    	String captchaId = Codec.UUID();
    	render(employers, employer, captchaId);
    }
    
    public static void submitQuestion(Question question, String captchaId, String captchaCode) {
    	
    	if (!Security.confirmCaptcha(captchaId, captchaCode)) {
    		flash.error ("Captcha did not match");
    		ask(question.employer.seoName);
    	}
    	question.save();
    	
    	List<Employee> employees = Employee.find("byEmployer", question.employer).fetch();
		Logger.debug ("Emailing to %d employees", employees.size());

		if ((employees!=null) && (employees.size()>0)) {
    		for (Employee employee: employees) {
    			Logger.debug ("Emailing to %s", employee.email);
		    	HashMap<String, Object> args = new HashMap<String, Object>();
		    	args.put("question", question);
		    	args.put("employee", employee);
    			EmailUtil.sendEmail(employee.email, "New ApplicantChatter Question - "+question.subject, "newQuestion", args);
    		}
    	}
		
		if (!StringUtils.isEmpty(question.email)) {
	    	HashMap<String, Object> args = new HashMap<String, Object>();
	    	args.put("question", question);
			EmailUtil.sendEmail(question.email, "Your ApplicantChatter Question - "+question.subject, "newQuestionForApplicant", args);
		}
	    	
    	
    	HashMap<String, Object> params = new HashMap<String, Object>();
    	params.put("viewId", question.viewId);
    	String viewUrl = Router.getFullUrl("Application.question", params);
    	flash.success("Thank you for submitting your question.  You can find the answers at <a href=\"%s\">%s</a>", viewUrl, viewUrl);
    	ask(null);
    }
    
    public static void answer(String answerId) {
    	Question question = Question.find("byAnswerId", answerId).first();
    	if (question == null) {
    		flash.error("Could not find that question");
    		Application.index();
    	}
    	
    	render(question);
    }
    
    public static void submitAnswer(Answer answer) {
    	answer.save();
    	
    	HashMap<String, Object> params = new HashMap<String, Object>();
    	params.put("viewId", answer.question.viewId);
    	String viewUrl = Router.getFullUrl("Application.question", params);

		Question question = answer.question;
		if (!StringUtils.isEmpty(question.email)) {
	    	HashMap<String, Object> args = new HashMap<String, Object>();
	    	args.put("question", question);
	    	args.put("answer", answer);
			EmailUtil.sendEmail(question.email, "A New Answer on ApplicantChatter for your question \""+question.subject+"\"", 
					"newAnswerForApplicant", args);
		}

    	flash.success("Thank you for answering this question");
    	Application.question(answer.question.viewId);
    }
    
    public static void question(String viewId) {
    	Question question = Question.find("byViewId", viewId).first();
    	if (question == null) {
    		flash.error("Could not find that question");
    		Application.index();
    	}
    	render(question);
    }
    
    public static void submitVote(Long answerId, boolean forOrAgainst) {
    	Employee employee = Security.getLoggedInEmployee();
    	Answer answer = Answer.findById(answerId);
    	if (answer== null) {
    		flash.error("Could not vote on that answer");
    		index();
    	}

    	Question question = answer.question;
    	if (question == null) {
    		flash.error("Could not vote on that question");
    		question(question.viewId);
    	}
    	
    	if (!question.canAnswer()) {
    		flash.error("Could not vote on that question");
    		question(question.viewId);
    	}
    	
    	Vote vote = new Vote();
    	List<Vote> existingVotes = Vote.find("byEmployeeAndAnswer", employee, answer).fetch();
    	if (existingVotes.size()==0) {
    		vote = new Vote();
    		vote.employee = employee;
    		vote.answer = answer;
    		vote.forOrAgainst = forOrAgainst;
    		vote.save();
    		question(question.viewId);
    	}
    	
    	Vote existingVote = existingVotes.get(0);
    	if (existingVote.forOrAgainst == forOrAgainst) {
    		existingVote.delete();
    	} else {
    		existingVote.forOrAgainst = forOrAgainst;
    		existingVote.save();
    	}
    	
		question(question.viewId);
    }

}