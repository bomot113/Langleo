package com.atteo.langleo_trial.activities;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.atteo.langleo_trial.R;
import com.atteo.langleo_trial.models.Word;
import com.bomot113.langleo.DictSearch.FTSData;
import com.bomot113.langleo.models.SearchList;



public class SearchableActivity extends ListActivity {

	private SimpleCursorAdapter adapter;
	private SearchList list;
	private final int REQUEST_EDIT_WORD = 2;
	private String query="";
	private Cursor cursor;
	private FTSData SearchableData;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.words_list);
		
		this.SearchableData = new FTSData(getApplicationContext());
		
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doSearchWords(query);
	    }

	}

	private void doSearchWords(String query) {
		// TODO Auto-generated method stub
		list = new SearchList(1, null);
		ListView list = getListView();
		this.query = query;
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				editWord((int) id);
			}

		});

		//registerForContextMenu(list);
	}
	
	public void onResume() {
		super.onResume();
		refreshList();
	}
	
	public void refreshList() {
		ListView list = getListView();
		this.cursor = search(this.query);
		adapter = new SimpleCursorAdapter(this, R.layout.word_item,
				this.cursor, new String[] { FTSData.KEY_WORD, FTSData.KEY_TRANSLATION}, new int[] {
						R.id.word_word, R.id.word_translation });
		list.setAdapter(adapter);
	}
	
    private Cursor search(String query) {
        query = query.toLowerCase();
        String[] columns = new String[] {
        	BaseColumns._ID,
//            FTSData.KEY_ID,
//            FTSData.KEY_PATH,
            FTSData.KEY_WORD,
            FTSData.KEY_TRANSLATION};

        return this.SearchableData.getWordMatches(query, columns);
      }
    
	public void editWord(int id) {
		Intent intent = new Intent(getApplicationContext(), EditWord.class);
		intent.putExtra("word", new Word(id).toBundle());
		startActivityForResult(intent, REQUEST_EDIT_WORD);

	}
}
