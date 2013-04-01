package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import play.db.jpa.Model;

@Entity
public class Vote extends Model {

	@ManyToOne
	public Employee employee;
	
	@ManyToOne
	public Answer answer;
	
	public boolean forOrAgainst;

	@PrePersist
	public void prePersistHandler() throws Exception {
		checkAccess();
	}
	
	@PreUpdate
	public void preUpdateHandler() throws Exception {
		checkAccess();
	}
	
	public void checkAccess() throws Exception {
		if (employee == null) {
			throw new Exception ("Could not save vote because employee is null");
		}
		if (answer == null) {
			throw new Exception ("Could not save vote because question is null");
		}
		
		if (employee.employer != answer.question.employer) {
			throw new Exception ("Could not save vote because you don't work for the employer the question was asked of");
		}
	}
	
	
}
