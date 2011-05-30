package com.bomot113.langleo.DictSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

import com.atteo.langleo_trial.Langleo;
import com.atteo.langleo_trial.models.Question;
import com.atteo.langleo_trial.models.Word;
import com.atteo.silo.Silo;
import com.atteo.silo.StorableCollection;


public class FTSData {
	 private static final String TAG = "DictionaryDatabase";

	    //The columns we'll include in the dictionary table
	    public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	    public static final String KEY_TRANSLATION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	    public static final String KEY_ID = "WordID";
	    
	    private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";

	    private static DictionaryOpenHelper mDatabaseOpenHelper;
	    private static final HashMap<String,String> mColumnMap = buildColumnMap();
	    private static SQLiteDatabase mDatabase;
        public static final String FTS_TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
            " USING fts3 (" +
            KEY_WORD + ", " +
            KEY_TRANSLATION + ", " +
            KEY_ID +");";	    
	    /**
	     * Constructor
	     * @param context The Context within which to work, used to create the DB
	     */
	    public static void activateFTSData(Context context, SQLiteDatabase db) {
	        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
	        mDatabaseOpenHelper.getReadableDatabase();
	        mDatabase = db;
	    }

	    public static void setDatabaseVersion(Context context, int newVersion){
			// TBM: update version
			FTSData.activateFTSData(context, Silo.getDatabase());
			new SQLiteOpenHelper(context, Langleo.DATABASE_NAME, null, newVersion) {
				
				@Override
				public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1,
						int paramInt2) {
					
				}
				
				@Override
				public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
					
				}
			};
	    }
	    public static void updateFTSData() throws IOException{
	    	mDatabaseOpenHelper.loadDictionary(mDatabase);
	    }
	    /**
	     * Builds a map for all columns that may be requested, which will be given to the 
	     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
	     * all columns, even if the value is the key. This allows the ContentProvider to request
	     * columns w/o the need to know real column names and create the alias itself.
	     */
	    private static HashMap<String,String> buildColumnMap() {
	        HashMap<String,String> map = new HashMap<String,String>();
	        map.put(KEY_WORD, KEY_WORD);
	        map.put(KEY_TRANSLATION, KEY_TRANSLATION);
	        map.put(BaseColumns._ID, KEY_ID + " AS " +
	                BaseColumns._ID);
	        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
	                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
	        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
	                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
	        return map;
	    }

	    /**
	     * Returns a Cursor positioned at the word specified by rowId
	     *
	     * @param rowId id of word to retrieve
	     * @param columns The columns to include, if null then all are included
	     * @return Cursor positioned to matching word, or null if not found.
	     */
	    public Cursor getWord(String rowId, String[] columns) {
	        String selection = KEY_ID + " = ?";
	        String[] selectionArgs = new String[] {rowId};

	        return query(selection, selectionArgs, columns);

	        /* This builds a query that looks like:
	         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
	         */
	    }

	    /**
	     * Returns a Cursor over all words that match the given query
	     *
	     * @param query The string to search for
	     * @param columns The columns to include, if null then all are included
	     * @return Cursor over all words that match, or null if none found.
	     */
	    public Cursor getWordMatches(String query, String[] columns) {
	        String selection = KEY_WORD + " MATCH ?";
	        String[] selectionArgs = new String[] {"*"+ query+"*"};

	        return query(selection, selectionArgs, columns);

	        /* This builds a query that looks like:
	         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*' OR
	         *     								 <KEY_DEFINITION> MATCH 'query*' 
	         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
	         *
	         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
	         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
	         *    HOWEVER, we use the WordID as _id to hook up the words in search result and real words
	         *    in database.
	         */
	    }

	    
	    /**
	     * Returns a Cursor over all words that have translation and words
	     * match the given query
	     * 
	     * @param query The string to search for
	     * @param columns The columns to be returned, if null then all are included
	     * @return Cursor over all words that match, or null if none found.
	     */
	    public static Cursor getWordMatchesTransNWords(String query, String[] columns) {
	        String selection = KEY_ID + " IN " +
				 	   "(SELECT "+ KEY_ID +" FROM " + FTS_VIRTUAL_TABLE + 
				 	   " WHERE " + KEY_WORD + " MATCH " + "\"*"+ query+"*\" "+
				 	   " UNION " +
				 	   " SELECT "+ KEY_ID +" FROM " + FTS_VIRTUAL_TABLE + 
				 	   " WHERE " + KEY_TRANSLATION + " MATCH "+ "\"*"+ query+"*\") ";
	        return query(selection, null, columns);

	        /* This builds a query that looks like:
	         *     SELECT <columns> FROM <table> Result
			 *	   WHERE Result.WordID IN
			 *	   (SELECT WordID FROM <table> WHERE <KEY_WORD> MATCH "*query*"
			 *		UNION
			 *		SELECT WordID FROM <table> WHERE <KEY_TRANSLATION> MATCH "*query*")
			 *
	         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
	         *
	         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
	         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
	         *    HOWEVER, we use the WordID as _id to hook up the words in search result and real words
	         *    in database.
	         */
	    }

	    
	    /**
	     * Performs a database query.
	     * @param selection The selection clause
	     * @param selectionArgs Selection arguments for "?" components in the selection
	     * @param columns The columns to return
	     * @return A Cursor over all rows matching the query
	     */
	    private static Cursor query(String selection, String[] selectionArgs, String[] columns) {
	        /* The SQLiteBuilder provides a map for all possible columns requested to
	         * actual columns in the database, creating a simple column alias mechanism
	         * by which the ContentProvider does not need to know the real column names
	         */
	        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	        builder.setTables(FTS_VIRTUAL_TABLE);
	        builder.setProjectionMap(mColumnMap);

	        // Execute SQL statement
	        long startTime = SystemClock.currentThreadTimeMillis();
	        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
	                columns, selection, selectionArgs, null, null, null);
			long endTime = SystemClock.currentThreadTimeMillis();
			String columnsString = "";
			for (int i = 0; i < columns.length; ++i) {
				if (i != 0)
					columnsString = columnsString + ",";
				columnsString = columnsString + columns[i];
			}
			Log.d(TAG, "Select query in table '" + FTS_VIRTUAL_TABLE + "' ("
					+ columnsString + ") with where '" + selection
					+ "' executed in " + (endTime - startTime) + "ms");

			if (cursor == null) {
	            return null;
	        } else if (!cursor.moveToFirst()) {
	            cursor.close();
	            return null;
	        }
	        return cursor;
	    }


	    /**
	     * This creates/opens the database.
	     */
	    private static class DictionaryOpenHelper extends SQLiteOpenHelper {

        
	        /* Note that FTS3 does not support column constraints and thus, you cannot
	         * declare a primary key. However, "rowid" is automatically used as a unique
	         * identifier, so when making requests, we will use "_id" as an alias for "rowid"
	         */

	        private static final String FTS_TABLE_CLEAR =
                "DELETE FROM " + FTS_VIRTUAL_TABLE + ";";
	        DictionaryOpenHelper(Context context) {
	        	super(context, Langleo.DATABASE_NAME, null, 
	        			(Langleo.MIGRATIONS != null) ? Langleo.MIGRATIONS.length : 0);
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) {
	        	loadDictionary(db);
	        }

			/**
	         * Starts a thread to load the database table with words
	         */
	        private void loadDictionary(SQLiteDatabase mDatabase) {
	        	final SQLiteDatabase db = mDatabase;
	            new Thread(new Runnable() {
	                public void run() {
	                    try {
	        	            Log.d(TAG, "Loading FTS data.");
	                    	db.execSQL(FTS_TABLE_CLEAR);
	                        loadWords(db);
	                        Log.d(TAG, "DONE loading FTS data.");		
	                    } catch (IOException e) {
	                        throw new RuntimeException(e);
	                    }
	                }
	            }).start();
	        }

	        private void loadWords(SQLiteDatabase mDatabase) throws IOException {
	            Log.d(TAG, "Loading words...");
	            StorableCollection storableCollection = new StorableCollection(
	    				Word.class);
	    		ArrayList<Word> loadedWords = storableCollection.toArrayList();
	    		int questionTotal = loadedWords.size();
	    		long id = -1;
	    		for(int i=0; i<questionTotal; i++){
	    			Word w = loadedWords.get(i);
	                id = addWord(mDatabase, w.getWord().trim(), w.getTranslation().trim(), w.getId());
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + w.getWord().trim());
                    }
	    		}
	            Log.d(TAG, "DONE loading words.");
	        }

	        /**
	         * Add a word to the dictionary.
	         * @return rowId or -1 if failed
	         */
	        public long addWord(SQLiteDatabase mDatabase, String word, String translation, long id) {
	            ContentValues initialValues = new ContentValues();
	            initialValues.put(KEY_WORD, word);
	            initialValues.put(KEY_TRANSLATION, translation);
	            initialValues.put(KEY_ID, id);
	            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                    + newVersion + ", which will destroy all old data");
	            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
	            db.execSQL(FTSData.FTS_TABLE_CREATE);
	            onCreate(db);
	        }
	    }

}
