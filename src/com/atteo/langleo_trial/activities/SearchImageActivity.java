package com.atteo.langleo_trial.activities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atteo.langleo_trial.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchImageActivity extends ListActivity
{
    class MyCustomAdapter extends ArrayAdapter<String>
    {


		public MyCustomAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}

		private void setImage(String s, ImageView imageview)
        {
            Drawable drawable = LoadImageFromWebOperations(s);
            imageview.setImageDrawable(drawable);
        }

        public View getView(int i, View view, ViewGroup viewgroup)
        {
            View view1 = getLayoutInflater().inflate(0x7f03000d, viewgroup, false);
            TextView textview = (TextView)view1.findViewById(0x7f0a002f);
            CharSequence charsequence = (CharSequence)titleLists.get(i);
            textview.setText(charsequence);
            ImageView imageview = (ImageView)view1.findViewById(0x7f0a002e);
            String s = thumbnailLists.get(i);
            setImage(s, imageview);
            return view1;
        }

    }


    private byte imageData[];
    private final int COUNT = 10;
    private int offset =0 ;
    private String query;
    private ArrayList<String> thumbnailLists;
    private ArrayList<String> titleLists;
    private int total;

    private static String BuildRequest(String queryString, int count, int offset)
    {
        String appID = "A918243742DB81D6F900894DA070B16F1087E263";
        String URLBuilder="";
		try {
			URLBuilder = "http://api.search.live.net/json.aspx?" 
					+ "AppId=" + appID
					+ "&Query=" + URLEncoder.encode(queryString, "UTF-8")
					+ "&Sources=Image"
			        // Image-specific request fields (optional)
			        + "&Image.Count=" + count
			        + "&Image.Offset=" + offset
			        + "&JsonType=callback"
			        + "&JsonCallback=SearchCompleted";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

        return URLBuilder;
    }

    private void GetResponse(String URLRequest)
        throws IOException, JSONException
    {

        URL url = new URL(URLRequest);
        HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
        httpurlconnection.connect();
        InputStream inputstream = httpurlconnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputStreamReader);     
        
        String queryResult = bufferedreader.readLine();
        bufferedreader.close();
        if (queryResult != null) {
            
            queryResult = handleString(queryResult);
            JSONObject jsonobject = new JSONObject(queryResult);
            if (!jsonobject.has("error")) {
            	JSONObject tranverse = jsonobject.getJSONObject("SearchResponse");
            	tranverse = tranverse.getJSONObject("Image");
            	this.total = tranverse.getInt("Total");
            	int image2DisplayCount = Math.min(total - offset, COUNT);
            	if (image2DisplayCount>0) {
            		JSONArray jsonarray = tranverse.getJSONArray("Results");
            		for (int i=0;i<image2DisplayCount;i++){
            			tranverse = jsonarray.getJSONObject(i);
            			String title = tranverse.getString("Title");
            			titleLists.add(title);
            			tranverse = tranverse.getJSONObject("Thumbnail");
            			String ImageURL = tranverse.getString("Url");
            			thumbnailLists.add(ImageURL);
            			
            		}
            	}
            	
            }
        }
    }

    private Drawable LoadImageFromWebOperations(String s)
    {
        Drawable drawable = null;
        try {
			drawable = Drawable.createFromStream((InputStream)(new URL(s)).getContent(), "src");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return drawable;
    }

    private void doSearchWords(String s)
    {
        ListView listview = getListView();
        query = s;


        listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {
				String s = parent.getItemAtPosition(position).toString();
	            returnImage(s);
			}
		});
    }

    private String handleString(String s)
    {
        return s.replaceAll("^[^{]*", "").replaceAll("[^}]*$", "");
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.image_list);
        String s = getIntent().getStringExtra("query");
        doSearchWords(s);
        ImageButton imagebutton = (ImageButton)findViewById(R.id.previous_button);
        imagebutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            offset = Math.max(offset - COUNT, 0);;
	            refreshList();
			}
		});
        ImageButton next_button = (ImageButton)findViewById(R.id.next_button);
        next_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            offset = Math.min(offset + COUNT, total);
	            refreshList();
			}
		});		
    }

    public void onResume()
    {
        super.onResume();
        refreshList();
    }

    public void refreshList()
    {
        thumbnailLists = new ArrayList<String>(); 
        titleLists = new ArrayList<String>();
        String URLRequest;
        URLRequest = BuildRequest(query, COUNT, offset);
        try {
			GetResponse(URLRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        MyCustomAdapter mycustomadapter = new MyCustomAdapter(this, R.layout.image_item, thumbnailLists);
        setListAdapter(mycustomadapter);
    }

    protected void returnImage(String s)
    {
        Bitmap bitmap = ((BitmapDrawable)LoadImageFromWebOperations(s)).getBitmap();
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        android.graphics.Bitmap.CompressFormat compressformat = android.graphics.Bitmap.CompressFormat.PNG;
        bitmap.compress(compressformat, 100, bytearrayoutputstream);
        imageData = bytearrayoutputstream.toByteArray();
        Intent intent = new Intent();
        intent.putExtra("image", imageData);
        setResult(RESULT_OK, intent);
        finish();
    }

}
