package com.bomot113.Lipo;

import java.lang.reflect.Field;

import android.content.ContentValues;
import android.database.Cursor;

import com.atteo.langleo_trial.models.Word;
import com.atteo.silo.Silo;

public class ImageStorage {
	protected int id = -1;
	private boolean isStub = true;
	private boolean isImageChanged = false;
	
	public ImageStorage() {
	}

	public ImageStorage(int id) {
		this.id = id;
	}

	public byte[] retrieveImageValue() {
		Field field = getImageFields();
		byte[] result = null;
		try {
			result = (byte[]) field.get(this);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private Field getImageFields() {
		return imageStorageInfo.getImageFieldForClass(this.getClass());
	}

	public String getImageFieldName() {
		Field field = getImageFields();
		return field.getName();
	}

	public boolean save(Word textWord) {
		if (!isImageChanged) return true;
		String iFieldName = getTableName();
		String imageTable = iFieldName;
		String imageId = iFieldName + "_id";
		byte[] image = this.retrieveImageValue();
		ContentValues values = new ContentValues();
		if (image != null && this.id != -1) {
			// update image value
			values.put(iFieldName, image);
			Silo.update(imageTable, values, " id = " + this.id, null);
		} else if (image == null && this.id != -1) {
			// delete image
			Silo.delete(imageTable, " id = " + this.id, null);
			values.put(imageId, -1);
			Silo.update(textWord.getTableName(), values,
					" id = " + textWord.getId(), null);
		} else if (image != null && this.id == -1) {
			// insert new image to image table
			values.put(iFieldName, image);
			this.id = Silo.insert(imageTable, values);

			values = new ContentValues();
			values.put(imageId, this.id);
			Silo.update(textWord.getTableName(), values, " id = " + textWord.getId(), null);
		} else {
			// nothing is changed
			return true;
		}
		return false;
	}

	public boolean load(Word textWord) {
		if (!isStub) {
			return true;
		}
		isStub = false;
		String fieldName = getTableName();
		String[] queriedFields; 
		
		Cursor cursor;
		queriedFields = new String[1];
		queriedFields[0] = fieldName+"_id";
		cursor = Silo.select(textWord.getTableName(), queriedFields, 
		"id = "+ textWord.getId(), null, null, null, null, null);
		
		if (cursor.moveToFirst()){
			int imageWord_id  = cursor.getInt(0); 
			this.id = imageWord_id;
			queriedFields = new String[1];
			queriedFields[0] = fieldName;
			cursor = Silo.select(this.getTableName(), queriedFields, 
					"id = "+ imageWord_id, null, null, null, null, null);
			if (cursor.moveToFirst()){
				Field field = getImageFields();
				byte[] image = cursor.getBlob(0);
				try {
					field.set(this, image);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	private String getTableName() {
		return this.getImageFieldName().toLowerCase();
	}

	public void setImageChanged(boolean isImageChanged) {
		this.isImageChanged = isImageChanged;
	}

	public boolean isImageChanged() {
		return isImageChanged;
	}
	
	public int getID(){
		return this.id;
	}
	
}
