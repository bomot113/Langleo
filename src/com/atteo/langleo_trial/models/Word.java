package com.atteo.langleo_trial.models;

import com.atteo.silo.Storable;
import com.atteo.silo.associations.BelongsTo;
import com.atteo.silo.associations.DatabaseField;
import com.atteo.silo.associations.HasOne;

public class Word extends Storable {
	@DatabaseField
	private String word;
	@DatabaseField
	private String translation;
	@DatabaseField
	private String note;
	@DatabaseField
	private Boolean studied = false;
	@DatabaseField
	private Boolean reversible = false;
	@BelongsTo
	private List list;
	@HasOne(foreignField = "word", dependent = true)
	private Question question;
	@DatabaseField
	private Boolean lastRepe_isReversed = false;
	
	public Word() {
		super();
	}

	public Word(int id) {
		super(id);
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public Boolean isStudied() {
		return studied;
	}

	public void setStudied(boolean studied) {
		this.studied = studied;
	}

	public Question getQuestion() {
		return question;
	}

	public void setReversible(Boolean reversible) {
		this.reversible = reversible;
	}

	public Boolean getReversible() {
		return reversible;
	}

	public void setLastRepe_isReversed(Boolean lastRepe_isReversed) {
		this.lastRepe_isReversed = lastRepe_isReversed;
	}

	public Boolean getLastRepe_isReversed() {
		return lastRepe_isReversed;
	}
	
}
