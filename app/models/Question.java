package models;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import controllers.Security;

import play.db.jpa.Model;
import utils.StringUtils;

@Entity
public class Question extends Model {
	
	public String email;
	
	@ManyToOne
	public Employer employer;
	
	public String subject;
	
	@Lob
	public String question;
	
	public String viewId;
	
	public String answerId;
	
	public Date created = new Date();
	
	public List<Answer> getAnswers() {
		return Answer.find("byQuestion", this).fetch();
	}
	
	public boolean canAnswer() {
		Employee employee = Security.getLoggedInEmployee();
		if (employee == null) return false;
		
		return (employee.employer == employer);
	}
	
	@PrePersist
	public void setupIds() {
		boolean isFound = false;
		
		if (StringUtils.isEmpty(viewId)) {
			do {
				UUID uuid = UUID.randomUUID();
				viewId = uuid.toString();
				viewId = viewId.replaceAll("-", "");
				
				viewId = viewId.substring(0, 12);
				
				isFound = Question.count("byViewId", viewId) > 0;
			} while (isFound);
		}
		
		isFound = false;
		if (StringUtils.isEmpty(answerId)) {
			do {
				UUID uuid = UUID.randomUUID();
				answerId = uuid.toString();
				answerId = answerId.replaceAll("-", "");
				
				answerId = answerId.substring(0, 12);
	
				isFound = Question.count("byViewId", answerId) > 0;
			} while (isFound);
		}
	}
}
