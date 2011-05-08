package com.atteo.langleo_trial.activities;

import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.atteo.langleo_trial.Langleo;
import com.atteo.langleo_trial.LearningAlgorithm;
import com.atteo.langleo_trial.R;
import com.atteo.langleo_trial.models.Collection;
import com.atteo.langleo_trial.models.Language;
import com.atteo.langleo_trial.models.List;
import com.atteo.langleo_trial.models.Question;
import com.atteo.langleo_trial.models.Word;
import com.atteo.langleo_trial.util.BetterAsyncTask;
import com.atteo.langleo_trial.views.NumberPicker;
import com.atteo.langleo_trial.views.SelectLimitDialog;
import com.google.marvin.widget.TouchGestureControlOverlay;
import com.google.marvin.widget.TouchGestureControlOverlay.Gesture;
import com.google.marvin.widget.TouchGestureControlOverlay.GestureListener;

public class Cram extends Activity {
	private static Cram INSTANCE = null;

	private Question currentQuestion = null;
	private Language questionBaseLanguage;
	private Language questionTargetLanguage;
	private TextView tv_word, tv_translation, tv_new, tv_note, tv_progress,
			tv_time_estimation;
	private LinearLayout new_word_buttons, normal_buttons;
	private TouchGestureControlOverlay gestures;
	private ImageView baseLanguageImage, targetLanguageImage;
	private ProgressBar progressBar = null;

	private Chronometer chronometer;

	private long startTime = 0;
	private long elapsedTime = 0;

	private boolean isCreated = false;

	private boolean audioOn;
	private boolean readTranslation = false;

	private boolean noWordLanguageShown = false,
			noTranslationLanguageShown = false;

	private int limitIncrease = 0;

	private static final int REQUEST_EDIT_WORD = 0;

	private static final int DIALOG_SELECT_LIMIT = 0;
	private static final int DIALOG_PLEASE_WAIT = 1;

	private static int list_ID; 
	
	private TextToSpeech tts = null;

	private boolean preparing = false;

	private OnInitListener ttsInitListener = new OnInitListener() {

	
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
				Word w = currentQuestion.getWord().l();
				audioIsOn(true);
				read(w);
			} else
				audioFailed();
		}

	};

	private void startAudio() {
		tts = new TextToSpeech(this, ttsInitListener);
	}

	private void stopAudio() {
		tts.shutdown();
		tts = null;
		audioIsOn(false);
	}

	private void read(Word w) {
		if (!isAudioOn())
			return;
		if (readTranslation) {
			if (TextToSpeech.LANG_AVAILABLE == tts.
					isLanguageAvailable(new Locale(questionTargetLanguage
							.getShortName()))) {
				tts.setLanguage(new Locale(questionTargetLanguage
						.getShortName()));
				tts.speak(prepareToSpeak(w.getTranslation()), 1, null);
			} else if (!noTranslationLanguageShown) {
				Toast.makeText(this, getString(R.string.no_voice_data,
						questionTargetLanguage.getName()), Toast.LENGTH_LONG).show();
				noTranslationLanguageShown = true;
			}
		} else {
			if (TextToSpeech.LANG_AVAILABLE == tts
					.isLanguageAvailable(new Locale(questionBaseLanguage
							.getShortName()))) {
				tts
						.setLanguage(new Locale(questionBaseLanguage
								.getShortName()));
				tts.speak(prepareToSpeak(w.getWord()), 1, null);
			} else if (!noWordLanguageShown) {
				Toast.makeText(this, getString(R.string.no_voice_data,
						questionBaseLanguage.getName()), Toast.LENGTH_LONG).show();
				noWordLanguageShown = true;
			}
		}
		readTranslation = !readTranslation;
	}

	private void audioIsOn(boolean isOn) {
		audioOn = isOn;
		if (!isFinishing()) {
			SharedPreferences prefs = Langleo.getPreferences();
			Editor e = prefs.edit();
			e.putBoolean("audio_on", audioOn);
			e.commit();
		}

		ToggleButton tb = (ToggleButton) findViewById(R.id.study_audio_switch);
		tb.setChecked(audioOn);

		if (audioOn)
			Toast.makeText(Cram.this, R.string.audio_activated,
					Toast.LENGTH_LONG).show();
		else if (!isFinishing())
			Toast.makeText(Cram.this, R.string.audio_deactivated,
					Toast.LENGTH_LONG).show();

	}

	private void audioFailed() {
		Toast.makeText(this, R.string.audio_start_failed, Toast.LENGTH_LONG);
	}

	private boolean isAudioOn() {
		return audioOn;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		INSTANCE = this;

		if (savedInstanceState != null)
			preparing = savedInstanceState.getBoolean("preparing", false);

		setContentView(R.layout.study);

		isCreated = true;

		Intent intent = getIntent();
		limitIncrease = intent.getIntExtra("limit_increase", 0);
		
		List list = new List();
		list.loadBundle(intent.getBundleExtra("list"));
		list_ID = list.getId();
		
		baseLanguageImage = (ImageView) findViewById(R.id.study_base_language_image);
		targetLanguageImage = (ImageView) findViewById(R.id.study_target_language_image);

		progressBar = (ProgressBar) findViewById(R.id.study_progress_bar);

		tv_new = (TextView) findViewById(R.id.study_new_word);
		tv_word = (TextView) findViewById(R.id.study_word_content);
		tv_translation = (TextView) findViewById(R.id.study_translation_content);
		tv_progress = (TextView) findViewById(R.id.study_progress_info);
		tv_note = (TextView) findViewById(R.id.study_note);
		tv_time_estimation = (TextView) findViewById(R.id.study_progress_time_estimation);

		new_word_buttons = (LinearLayout) findViewById(R.id.study_first_time_buttons);
		normal_buttons = (LinearLayout) findViewById(R.id.study_normal_buttons);

		chronometer = (Chronometer) findViewById(R.id.study_chronometer);

		ImageButton imageButton = (ImageButton) findViewById(R.id.study_button_incorrect);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				answer(LearningAlgorithm.ANSWER_INCORRECT);
			}
		});

		imageButton = (ImageButton) findViewById(R.id.study_button_correct);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				answer(LearningAlgorithm.ANSWER_CORRECT);
			}
		});

		imageButton = (ImageButton) findViewById(R.id.study_button_continue);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				answer(LearningAlgorithm.ANSWER_CONTINUE);
			}
		});

//		button = (Button) findViewById(R.id.study_button_not_new);
//		button.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				answer(LearningAlgorithm.ANSWER_NOT_NEW);
//			}
//		});

		gestures = new TouchGestureControlOverlay(this);
		gestures.setGestureListener(new GestureListener() {

			@Override
			public void onGestureChange(Gesture gesture) {

			}

			@Override
			public void onGestureFinish(Gesture gesture) {
				if (gesture == Gesture.CENTER) {
					tv_translation.setVisibility(View.VISIBLE);
					tv_note.setVisibility(View.VISIBLE);

					if (!isAudioOn())
						return;

					Word w = currentQuestion.getWord();

					List list = w.getList();
					list.load();
					Collection c = list.getCollection();
					c.load();

					read(w);

				}

				if (gesture == Gesture.UP && isAudioOn()) {
					if (currentQuestion.getRepetitions() == -1)
						answer(LearningAlgorithm.ANSWER_CONTINUE);
					else
						answer(LearningAlgorithm.ANSWER_CORRECT);
				}

				if (gesture == Gesture.DOWN && isAudioOn()) {
					if (currentQuestion.getRepetitions() == -1)
						answer(LearningAlgorithm.ANSWER_CONTINUE); // NOT_NEW was here
					else
						answer(LearningAlgorithm.ANSWER_INCORRECT);
				}

			}

			@Override
			public void onGestureStart(Gesture gesture) {

			}

		});

		ToggleButton tb = (ToggleButton) findViewById(R.id.study_audio_switch);
		tb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isAudioOn())
					startAudio();
				else
					stopAudio();

			}
		});

		FrameLayout f = (FrameLayout) findViewById(R.id.study_layout);
		f.addView(gestures);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBundle("question") != null) {
				currentQuestion = new Question();
				currentQuestion.loadBundle(savedInstanceState
						.getBundle("question"));
				showQuestion();
				if (savedInstanceState.getBoolean("answer_shown")) {
					tv_translation.setText(currentQuestion.getWord()
							.getTranslation());
					tv_note.setText(currentQuestion.getWord().getNote());
					tv_translation.setVisibility(View.VISIBLE);
					tv_note.setVisibility(View.VISIBLE);
				}
				findViewById(R.id.study_main_layout)
						.setVisibility(View.VISIBLE);
			}
			elapsedTime = savedInstanceState.getLong("elapsedTime", 0);
			if (!preparing)
				new PrepareTask().execute(savedInstanceState
						.getBundle("alg_state"));
		} else {
			elapsedTime = 0;
			if (!preparing)
				new PrepareTask().execute();
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (!isCreated) {
			startTime = SystemClock.elapsedRealtime() - elapsedTime;
			chronometer.setBase(startTime);
			chronometer.start();
		} else
			isCreated = false;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		elapsedTime = SystemClock.elapsedRealtime() - startTime;
		chronometer.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isAudioOn())
			stopAudio();
		Langleo.getCrammingAlgorithm().stop();
		INSTANCE = null;
	}

	private String prepareToSpeak(String string) {
		return string.replaceAll("\\([^)]*\\)", "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.study, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int dialogId) {
		switch (dialogId) {
		case DIALOG_PLEASE_WAIT:
			ProgressDialog progressDialog;
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.please_wait));
			progressDialog.setCancelable(false);
			return progressDialog;

		case DIALOG_SELECT_LIMIT:
			final Dialog dialog = new SelectLimitDialog(this);
			Button b = (Button) dialog
					.findViewById(R.id.increase_limit_dialog_ok);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					NumberPicker np = (NumberPicker) dialog
							.findViewById(R.id.increase_limit_dialog_picker);
					LearningAlgorithm alg = Langleo.getCrammingAlgorithm();
					alg.increaseLimit(np.getCurrent());

					progressBar.setMax(alg.allQuestions());
					progressBar.setProgress(alg.questionsAnswered() + 1);

					tv_progress.setText((alg.questionsAnswered() + 1) + "/"
							+ alg.allQuestions());
				}
			});
			b = (Button) dialog.findViewById(R.id.increase_limit_dialog_cancel);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			return dialog;
		default:
			return null;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Intent intent;

		switch (menuItem.getItemId()) {
		case R.id.study_more_new_words:
			showDialog(DIALOG_SELECT_LIMIT);
			break;
		case R.id.study_edit:
			intent = new Intent(getApplicationContext(), EditWord.class);
			intent.putExtra("word", currentQuestion.getWord().toBundle());
			startActivityForResult(intent, REQUEST_EDIT_WORD);
			break;
		case R.id.study_delete:
			Langleo.getCrammingAlgorithm().deletedQuestion(currentQuestion);
			currentQuestion.getWord().delete();
			nextQuestion();
			break;
		case R.id.study_help:
			showHelp();
			break;
		}
		return true;
	}

	private void showHelp() {
		Intent intent = new Intent(this, Help.class);
		intent.putExtra("part", "study");
		startActivity(intent);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_CANCELED)
			return;
		Bundle b;
		switch (requestCode) {
		case REQUEST_EDIT_WORD:
			b = intent.getBundleExtra("word");
			Word word = new Word();
			word.loadBundle(b);
			word.save();
			currentQuestion.getWord().reload();
			showQuestion();
			break;

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle b) {
		b.putBoolean("preparing", preparing);
		b.putBoolean("answer_shown", !tv_translation.isShown());
		if (currentQuestion != null)
			b.putBundle("question", currentQuestion.toBundle());
		elapsedTime = SystemClock.elapsedRealtime() - startTime;
		b.putLong("elapsedTime", elapsedTime);
		b.putBundle("alg_state", Langleo.getCrammingAlgorithm()
				.getInstanceState());
	}

	private boolean nextQuestion() {
		currentQuestion = Langleo.getCrammingAlgorithm().getQuestion();
		readTranslation = false;
		if (currentQuestion == null) {
			finish();
			return false;
		}

		showQuestion();
		return true;
	}

	private void showQuestion() {
		currentQuestion.load();
		Word w = currentQuestion.getWord();
		w.load();
		List aList = w.getList();
		aList.load();
		Collection c = aList.getCollection();
		c.load();
		if (questionBaseLanguage == null
				|| questionBaseLanguage.getId() != c.getBaseLanguage().getId()) {
			questionBaseLanguage = c.getBaseLanguage();
			questionBaseLanguage.load();
		}
		if (questionTargetLanguage == null
				|| questionTargetLanguage.getId() != c.getTargetLanguage()
						.getId()) {
			questionTargetLanguage = c.getTargetLanguage();
			questionTargetLanguage.load();
		}

		tv_word.setText(w.getWord());
		tv_translation.setText(w.getTranslation());
		tv_note.setText(w.getNote());
		// TBM: switch translation's place and that of word		
		if (w.getReversible() && !w.getLastRepe_isReversed()) {
			tv_word.setText(w.getTranslation());
			tv_translation.setText(w.getWord());
		}

		baseLanguageImage.setImageDrawable(getResources().getDrawable(
				getResources().getIdentifier(
						"flag_" + questionBaseLanguage.getName().toLowerCase(),
						"drawable", Langleo.PACKAGE)));

		targetLanguageImage.setImageDrawable(getResources().getDrawable(
				getResources().getIdentifier(
						"flag_"
								+ questionTargetLanguage.getName()
										.toLowerCase(), "drawable",
						Langleo.PACKAGE)));

		LearningAlgorithm alg = Langleo.getCrammingAlgorithm();
		progressBar.setMax(alg.allQuestions());
		progressBar.setProgress(alg.questionsAnswered() + 1);

		tv_progress.setText((alg.questionsAnswered() + 1) + "/"
				+ alg.allQuestions());

		// config the view
		normal_buttons.setVisibility(View.VISIBLE);
		tv_translation.setVisibility(View.INVISIBLE);
		tv_note.setVisibility(View.INVISIBLE);
		new_word_buttons.setVisibility(View.GONE);
		tv_new.setVisibility(View.GONE);

		read(w);

	}

	private void updateTimeEstimation() {
		LearningAlgorithm alg = Langleo.getCrammingAlgorithm();
		if (alg.questionsAnswered() < 3) {
			tv_time_estimation.setText("--:--");
			return;
		}
		long estimation = SystemClock.elapsedRealtime() - startTime;
		estimation += estimation / alg.questionsAnswered()
				* (alg.allQuestions() - alg.questionsAnswered());
		int hours = (int) (estimation / (1000 * 60 * 60));
		int minutes = (int) (estimation / (1000 * 60)) % 60;
		int seconds = (int) (estimation / 1000) % 60;

		String minStr;
		String secStr;

		if (minutes < 10 && hours > 0)
			minStr = "0" + minutes;
		else
			minStr = String.valueOf(minutes);

		if (seconds < 10)
			secStr = "0" + seconds;
		else
			secStr = String.valueOf(seconds);

		if (hours > 0)
			tv_time_estimation.setText(hours + ":" + minStr + ":" + secStr);
		else
			tv_time_estimation.setText(minStr + ":" + secStr);
	}

	private void answer(int answerQuality) {
		Langleo.getCrammingAlgorithm().answer(currentQuestion, answerQuality);
		nextQuestion();
		updateTimeEstimation();
	}

	private class PrepareTask extends BetterAsyncTask<Bundle, Void, Void> {
		@Override
		protected void onPreExecute() {
			Cram.INSTANCE.preparing = true;
			Cram.INSTANCE.showDialog(DIALOG_PLEASE_WAIT);
		}

		@Override
		protected void onPostExecute(Void v) {
			SharedPreferences prefs = Langleo.getPreferences();
			if (Cram.INSTANCE.currentQuestion == null)
				if (!Cram.INSTANCE.nextQuestion())
					return;
			else
				Cram.INSTANCE.showQuestion();
			
			if (prefs.getBoolean("audio_on", false))
				Cram.INSTANCE.startAudio();

			Cram.INSTANCE.findViewById(R.id.study_main_layout).setVisibility(
					View.VISIBLE);
			Cram.INSTANCE.removeDialog(DIALOG_PLEASE_WAIT);
			Cram.INSTANCE.startTime = SystemClock.elapsedRealtime()
					- elapsedTime;
			Cram.INSTANCE.chronometer.setBase(startTime);
			Cram.INSTANCE.chronometer.start();
			Cram.INSTANCE.updateTimeEstimation();
			Cram.INSTANCE.preparing = false;
		}

		@Override
		protected Void doInBackground(Bundle... params) {
			Bundle bundle;
			if (params.length == 1)
				bundle = params[0];
			else
				bundle = new Bundle();
			bundle.putInt("list_id", list_ID);
			Langleo.getCrammingAlgorithm().start(bundle);
			Langleo.getCrammingAlgorithm().increaseLimit(limitIncrease);
			return null;
		}

	}

}
