package com.atteo.langleo_trial.activities;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.atteo.langleo_trial.R;
import com.atteo.langleo_trial.models.MediaWord;
import com.atteo.langleo_trial.models.Word;

public class EditWord extends Activity {
	private String selectedImagePath;
	private Word word;
	private final int SELECT_PICTURE = 1;
	private final int NEW_HEIGHT = 120;
	private ImageView wordViewImage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_word);
		Bundle bundle = getIntent().getBundleExtra("word");
		word = new Word();
		word.loadBundle(bundle);
		word.load();
		
		Button button = (Button) findViewById(R.id.edit_word_cancel);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				cancel();
			}
		});
		button = (Button) findViewById(R.id.edit_word_ok);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				OK();
			}
		});

		button = (Button) findViewById(R.id.edit_word_new);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				new_word();
			}
		});

		CheckBox cb_reversible = (CheckBox) findViewById(R.id.edit_word_reversible);
		cb_reversible.setChecked(word.getReversible());

		TextView tv_word = (TextView) findViewById(R.id.edit_word_word);
		tv_word.setText(word.getWord());

		TextView tv_translation = (TextView) findViewById(R.id.edit_word_translation);
		tv_translation.setText(word.getTranslation());

		TextView tv_note = (TextView) findViewById(R.id.edit_word_note);
		tv_note.setText(word.getNote());

		wordViewImage = (ImageView) findViewById(R.id.image_word_view_preview);
		registerForContextMenu(wordViewImage);
		loadImage(word);
	}


	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_image, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete_image:
			deleteImage(word);
			return true;
		case R.id.edit_image:
			openGallery();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	private void deleteImage(Word word) {
		// TODO Auto-generated method stub
		word.setImage(null);
		wordViewImage.setImageResource(R.drawable.no_photo_available);
	}


	private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
		
	}

	
	private void loadImage(Word word) {
		byte[] image = word.getImage();
		if (image != null && image.length != 0) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0,
					image.length);
			this.wordViewImage.setImageBitmap(bitmap);
			this.wordViewImage.setVisibility(View.VISIBLE);
		} else {
			this.wordViewImage.setImageResource(R.drawable.no_photo_available);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	        if (requestCode == SELECT_PICTURE) {
	            Uri selectedImageUri = data.getData();
	            selectedImagePath = getPath(selectedImageUri);
	            byte[] image = getImageData(selectedImagePath);
	            image = resizeImage(image);
	            word.setImage(image);
	            loadImage(word);
	        }
	    }
	}
	private byte[] resizeImage(byte[] image) {
		if (image != null){
			Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
			int orgHeight = bm.getHeight();
			int orgWidth = bm.getWidth();
			float scale =  ((float) NEW_HEIGHT)/orgHeight;
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0,
                    orgWidth, orgHeight, matrix, true); 
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			resizedBitmap.compress(CompressFormat.PNG, 100, output);
			return output.toByteArray();
		}
		return null;
	}

	private byte[] getImageData(String selectedImagePath){
        FileInputStream in;
        BufferedInputStream buf; 
        byte[] imageData = null;
        try {
			in = new FileInputStream(selectedImagePath);
			buf = new BufferedInputStream(in);
			imageData = new byte[buf.available()];
			buf.read(imageData);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imageData;
	}
	
	private String getPath(Uri uri) {
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    int column_index = cursor
	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_word, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.edit_word_help:
			showHelp();
			break;
		}
		return true;
	}

	private void showHelp() {
		Intent intent = new Intent(this, Help.class);
		intent.putExtra("part", "edit_word");
		startActivity(intent);
	}

	private void OK() {
		Intent intent = new Intent();
		TextView tv_word = (TextView) findViewById(R.id.edit_word_word);
		TextView tv_translation = (TextView) findViewById(R.id.edit_word_translation);
		TextView tv_note = (TextView) findViewById(R.id.edit_word_note);
		CheckBox cb_reversible = (CheckBox) findViewById(R.id.edit_word_reversible);
		String word_ = tv_word.getText().toString();
		String translation = tv_translation.getText().toString();
		String note = tv_note.getText().toString();
		word.setWord(word_);
		word.setTranslation(translation);
		word.setNote(note);
		word.setReversible(cb_reversible.isChecked());			
		intent.putExtra("word", word.toBundle());
		setResult(RESULT_OK, intent);
		finish();
	}

	private void cancel() {
		setResult(RESULT_CANCELED, null);
		finish();
	}

	private void new_word() {
		TextView tv_word = (TextView) findViewById(R.id.edit_word_word);
		TextView tv_translation = (TextView) findViewById(R.id.edit_word_translation);
		TextView tv_note = (TextView) findViewById(R.id.edit_word_note);
		String word_ = tv_word.getText().toString();
		String translation = tv_translation.getText().toString();
		String note = tv_note.getText().toString();
		word.setWord(word_);
		word.setTranslation(translation);
		word.setNote(note);
		word.save();
		Word nword = new Word();
		nword.setList(word.getList());
		word = nword;
		tv_word.setText(word.getWord());
		tv_translation.setText(word.getTranslation());
		tv_word.requestFocus();
	}

	
}
