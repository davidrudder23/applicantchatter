package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.hibernate.tool.hbm2x.StringUtils;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class Employee extends Model {

	@ManyToOne
	public Employer employer;
	
	public String name;
	
	public String email;
	
	public String hashedPassword;
	
	public Date created = new Date();
	
	public static String hashPassword(String email, String plainPassword) {
		
		if (StringUtils.isEmpty(email) || StringUtils.isEmpty(plainPassword)) {
			Logger.debug ("either email or password are empty");
			return "";
		}
		SHA512Digest digest = new SHA512Digest();
		digest.update(email.getBytes(), 0, email.length());
		digest.update(":".getBytes(), 0, ":".length());
		digest.update(plainPassword.getBytes(), 0, plainPassword.length());
		
		byte[] digestOutput = new byte[64];
		digest.doFinal(digestOutput, 0);
		
		String hashedPassword = new String(Hex.encodeHex(digestOutput));
		
		return hashedPassword;
	}
}
