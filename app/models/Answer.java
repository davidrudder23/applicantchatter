package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import controllers.Security;

import play.db.jpa.Model;

@Entity
public class Answer extends Model {

	@ManyToOne
	public Question question;
	
	public String name;
	
	public String department;
	
	public String position;
	
	public int monthsAtJob;
	
	public Date created = new Date();
	
	@Lob
	public String comments;
	
	public boolean canVote() {
		Employee employee = Security.getLoggedInEmployee();
		if (employee == null) return false;
		
		return (employee.employer == question.employer);
	}
	
	public boolean hasVoted() {
		Employee employee = Security.getLoggedInEmployee();
		if (employee == null) return false;
		return Vote.count("byEmployeeAndAnswer", employee, this)>0;
	}

	public boolean hasVotedFor() {
		Employee employee = Security.getLoggedInEmployee();
		if (employee == null) return false;
		return Vote.count("byEmployeeAndAnswerAndForOrAgainst", employee, this, true)>0;
	}

	public boolean hasVotedAgainst() {
		Employee employee = Security.getLoggedInEmployee();
		if (employee == null) return false;
		return Vote.count("byEmployeeAndAnswerAndForOrAgainst", employee, this, false)>0;
	}
	
	public int getVoteTally() {
		int tally = 0;
		List<Vote> votes = Vote.find("byAnswer", this).fetch();
		for (Vote vote: votes) {
			if (vote.forOrAgainst) {
				tally++;
			} else {
				tally--;
			}
		}
		
		return tally;
	}


}
