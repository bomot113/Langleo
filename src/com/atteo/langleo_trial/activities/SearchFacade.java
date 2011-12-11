package com.atteo.langleo_trial.activities;

import com.atteo.langleo_trial.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

// Referenced classes of package com.atteo.langleo_trial.activities:
//            SearchImageActivity

public class SearchFacade extends Activity
{
    private static final int SEARCH_BING = 0;

    private void handleIntent(Intent intent)
    {
    	String action = intent.getAction();
        if ("android.intent.action.SEARCH".equals(action))
        {
            String queryString = intent.getStringExtra("query");
            android.content.Context context = getApplicationContext();
            Intent ImgSearch = new Intent(context, SearchImageActivity.class);
            ImgSearch.putExtra("query", queryString);
            startActivityForResult(ImgSearch, SEARCH_BING);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == SEARCH_BING)
        {
            byte[] imageData = data.getByteArrayExtra("image");
            Intent forwardIntent = new Intent();
            forwardIntent.putExtra("image", imageData);
            setResult(RESULT_OK, forwardIntent);
            finish();
        }
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.image_list);
        Intent intent = getIntent();
        handleIntent(intent);
        onSearchRequested();
    }

    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        handleIntent(intent);
    }

    protected void onResume()
    {
        super.onResume();
        onSearchRequested();
    }
}
