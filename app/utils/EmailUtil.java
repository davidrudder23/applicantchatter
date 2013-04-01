package utils;

import java.util.HashMap;

import org.apache.commons.mail.HtmlEmail;

import play.Logger;
import play.templates.Template;
import play.templates.TemplateLoader;

public class EmailUtil {
	
	public static void sendEmail(String to, String subject, String routeToMessage, HashMap<String, Object> args) {
		try {
	    	HtmlEmail email = new HtmlEmail();
	    	email.setHostName("applicantchatter.com");
	    	email.setSSL(true);
	    	
	    	email.addTo(to);
	    	email.setFrom("askemployee@reliableresponse.net", "ApplicantChatter");
	    	email.setSubject(subject);
	    	
			Template template = TemplateLoader.load("/Email/"+routeToMessage+".html");
			String html = template.render(args); 
			
			Logger.debug ("HTML = %s", html);

			template = TemplateLoader.load("/Email/"+routeToMessage+".txt");
			String text = template.render(args); 
			
	    	email.setHtmlMsg(html);
	    	// set the alternative message
	    	email.setTextMsg(text);
	    	email.send();
		} catch (Exception anyExc) {
			Logger.warn(anyExc, "trying to send mail");
		}

	}

}
