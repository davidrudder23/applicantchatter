package jobs;

import models.Employee;
import models.Employer;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

	@Override
	public void doJob() throws Exception {
		Employer reliableResponse = Employer.find("byName", "Reliable Response").first();
		if (reliableResponse == null) {
			reliableResponse = new Employer();
			reliableResponse.name = "Reliable Response";
			reliableResponse.description = "The makers of ApplicantChatter";
			reliableResponse.save();
		}
		
		Employee dave = Employee.find("byEmail", "david@reliableresponse.net").first();
		if (dave == null) {
			dave = new Employee();
			dave.employer = reliableResponse;
			dave.name = "Dave Rudder";
			dave.email = "david@reliableresponse.net";
			dave.hashedPassword = "28c2b587c9187d7df222e2fa3180c5d92b03d944638af388525b5dbfd13219221db3c24ef08a3c02e159f8812ad9d5db15d9e576a60192673798f2ebdfe4d6eb";
			dave.save();
		}
		
		if (!dave.employer.equals(reliableResponse)) {
			dave.employer = reliableResponse;
			dave.save();
		}
	}

	
}
