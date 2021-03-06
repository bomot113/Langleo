package com.atteo.langleo_trial;

import android.os.Bundle;

import com.atteo.langleo_trial.models.Question;

public interface LearningAlgorithm {
	public static final int QUESTIONS_WAITING = 0;
	public static final int NO_QUESTIONS = 1;
	public static final int QUESTIONS_ANSWERED = 2;
	public static final int QUESTIONS_ANSWERED_FORCEABLE = 3;

	public static final int ANSWER_CONTINUE = 0;
	public static final int ANSWER_NOT_NEW = 1;
	public static final int ANSWER_INCORRECT = 2;
	public static final int ANSWER_CORRECT = 3;

	public void start(Bundle bundle);

	public void stop();

	public Bundle getInstanceState();

	public int isQuestionWaiting();

	public void increaseLimit(int increase);

	public void answer(Question question, int answerQuality);

	public Question getQuestion();

	public int questionsAnswered();

	public int allQuestions();

	public void deletedQuestion(Question question);
}
