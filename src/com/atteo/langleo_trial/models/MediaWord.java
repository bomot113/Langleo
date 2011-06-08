package com.atteo.langleo_trial.models;

import com.bomot113.Lipo.ImageStorage;
import com.bomot113.Lipo.associations.ImageField;

public class MediaWord extends ImageStorage {
	private Word textWord;
	
	@ImageField
	private byte[] imageWord;
	
	public MediaWord(Word word){
		super();
		textWord = word;
	}
	
	public Word getWord(){
		return this.textWord;
	}
	
	public boolean save(){
		return super.save(textWord);
	}
	
	public void setImage(byte[] image) {
		this.setImageChanged(true);
		this.imageWord = image;
	}
	
	public byte[] getImage() {
		return imageWord;
	}
	
	public boolean load(){
		return super.load(textWord);
	}
	
	public boolean delete(){
		this.textWord.setImage(null);
		return super.save(textWord);
		
	}
	public void setID(int id){
		this.id = id;
	}
}
