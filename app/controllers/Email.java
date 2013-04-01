package controllers;

import models.Answer;
import models.Question;
import play.mvc.Controller;

public class Email extends Controller {
	
	public static void newQuestion(Question question, Answer answer) {
		render (question, answer);
	}

}
