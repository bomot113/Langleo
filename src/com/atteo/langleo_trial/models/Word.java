package com.atteo.langleo_trial.models;

import android.os.Bundle;

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
	@DatabaseField
	private Boolean lastRepe_isReversed = false;
	
	@BelongsTo
	private List list;
	@HasOne(foreignField = "word", dependent = true)
	private Question question;

	private MediaWord mediaWord;
	
	public Word() {
		super();
		mediaWord = new MediaWord(this);
	}


	public Word(int id) {
		super(id);
		mediaWord = new MediaWord(this);
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
	
	public boolean save(){
		if (super.save()){
			return mediaWord.save();
		} else {
			return false;
		}
	}

	public Bundle toBundle(){
		Bundle result = super.toBundle();
		result.putByteArray("imageWord", mediaWord.getImage());
		result.putInt("image_id", mediaWord.getID());
		return result;
		
	}
	
	public boolean loadBundle(Bundle bundle){
		if (super.loadBundle(bundle)){
			this.mediaWord = new MediaWord(this);
			this.setImage(bundle.getByteArray("imageWord"));
			this.mediaWord.setID(bundle.getInt("image_id"));
			return true;
		}
		return false;
	}
	
	public boolean load(){
		if (super.load()){
			this.mediaWord = new MediaWord(this);
			return this.mediaWord.load();
		}
		return false;
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

	public boolean getLastRepe_isReversed() {
		return lastRepe_isReversed;
	}
	
	public void setImage(byte[] image) {
		this.mediaWord.setImage(image);
	}
	
	public byte[] getImage() {
		return this.mediaWord.getImage();
	}
	public boolean delete(){
		return this.mediaWord.delete(); 
	}
}
