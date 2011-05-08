package com.bomot113.langleo.algorithms;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.os.Bundle;

import com.atteo.langleo_trial.LearningAlgorithm;
import com.atteo.langleo_trial.models.Question;
import com.atteo.langleo_trial.models.Word;
import com.atteo.langleo_trial.util.BetterAsyncTask;
import com.atteo.silo.StorableCollection;

public class CramAlgorithm implements LearningAlgorithm {
	private Random random;
	private ArrayList<Question> questions;
	private int list_id;
	private int questionsAnswered;
	private int questionTotal;
	private Question currentQuestion;
	private boolean isStarted = false;
	@Override
	public void start(Bundle bundle) {
		list_id = bundle.getInt("list_id", -1);
		random = new Random();
		random.setSeed(new Date().getTime());
		questions = new ArrayList<Question>();
		questionsAnswered = 0;
		questionTotal = 0;		
		scrambleQuestions();
		isStarted = true;
	}

	private void scrambleQuestions() {
		StorableCollection storableCollection = new StorableCollection(
				Word.class);
		storableCollection.whereInPlace("list_id = " + list_id);
		ArrayList<Word> loadedWords = storableCollection.toArrayList();
		questionTotal = loadedWords.size();
		for(int i=0; i<questionTotal; i++){
			Word w = loadedWords.get(i);
			w.setStudied(true);
			Question q = new Question();
			q.setDate(new Date());
			q.setWord(w);
			
			questions.add(random.nextInt(questions.size()+1), q);
		}
	}

	@Override
	public void stop() {
		isStarted = false;
		
	}

	@Override
	public Bundle getInstanceState() {
		if (!isStarted)
			return null;
		Bundle b = new Bundle();
		b.putInt("list_id", list_id);
		return b;
	}

	@Override
	public int isQuestionWaiting() {
		return NO_QUESTIONS;
	}

	@Override
	public void increaseLimit(int increase) {
		return;
	}

	@Override
	public void answer(Question question, int answer) {
		new AnswerTask(question, answer).execute((Void) null);
	}

	@Override
	public Question getQuestion() {
		if (questions.size() != 0){
			currentQuestion = questions.get(0);
			questions.remove(0);
			return currentQuestion;
		}
		return null;
	}

	@Override
	public int questionsAnswered() {
		return questionsAnswered;
	}

	@Override
	public int allQuestions() {
		return questionTotal;
	}

	@Override
	public void deletedQuestion(Question question) {
		questionTotal--;
	}
	

	private class AnswerTask extends BetterAsyncTask<Void, Void, Void> {
		private Question question;
		private int answer;

		public AnswerTask(Question question, int answer) {
			this.question = question;
			this.answer = answer;

		}

		@Override
		protected void onPreExecute() {

			if (answer != LearningAlgorithm.ANSWER_INCORRECT)
				questionsAnswered++;

			if (answer == LearningAlgorithm.ANSWER_CORRECT &&
					question.getWord().isStudied()) {
				// challenge the users next time by scramble 
				// the word and the translation
				Word w = question.getWord();
				if (w.getReversible()){
					boolean lastRepe_isReversed = w.getLastRepe_isReversed();
					w.setLastRepe_isReversed(!lastRepe_isReversed);
					w.save();
				}
			}
			
			if (answer != LearningAlgorithm.ANSWER_CORRECT) {

				int oneThird = questions.size() / 3;
				questions.add(
						random.nextInt(questions.size() + 1
								- oneThird)
								+ oneThird, question);
				return;
			}

		}

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
	}

}
