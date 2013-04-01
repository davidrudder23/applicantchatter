package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import play.db.jpa.Model;
import utils.StringUtils;

@Entity
public class Employer extends Model {

	public String name;
	
	@Lob
	public String description;

	public String seoName;
	
	public Date created = new Date();
	
	public boolean publicCanRead = true;
	
	public boolean publicCanAsk = false;

	@PrePersist
	public void prePersistHandler() {
		setupSeoName();
	}
	
	@PostLoad
	public void postLoadHandler() {
		if (setupSeoName()) {
			save();
		}
	}
	
	public boolean setupSeoName() {
		if (StringUtils.isEmpty(seoName)) {
			seoName = name.trim().toLowerCase().replaceAll(" ", "_").replaceAll("[^0-9a-zA-Z_]", "");
			return true;
		}
		
		return false;
	}
}
