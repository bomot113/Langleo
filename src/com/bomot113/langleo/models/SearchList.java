package com.bomot113.langleo.models;

import android.app.SearchManager;
import android.database.Cursor;
import android.text.TextUtils;

import com.atteo.silo.Silo;
import com.atteo.silo.Storable;
import com.bomot113.langleo.DictSearch.FTSData;


public class SearchList {

	private int id;
	private String query;

	/*
	 * To return the result, we must know where our user
	 * stand in (all, collection, list) 
	 */
	public SearchList(int id, Class<? extends Storable> klass) {
		this.id = id;
	}
	
	public Cursor getCursor(String query){
//		this.query = 
//			"SELECT c.name as Collection_Name, "+
//              "l.name as List_Name, "+
//              "w.word as Word, "+
//              "w.translation as Translation, "+ 
//              "w.note as Note, "+
//              "w.id as _id "+
//			"FROM collection c "+
//			"INNER JOIN list l"+
//			"   on c.id = l.collection_id "+
//			"INNER JOIN word w "+
//			"   on l.id = w.list_id ";
//		if (!TextUtils.isEmpty(query)){
//			this.query +=	
//				"WHERE w.word LIKE \"%" + query + "%\" " +
//				"   OR w.translation LIKE \"%" + query + "%\" " +
//				"   OR w.note LIKE \"%" + query + "%\" "+
//				"ORDER BY w.word";
//		}
		Cursor cursor = Silo.query(this.query, null);
		return cursor;
	}

}
