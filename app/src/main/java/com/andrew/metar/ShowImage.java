package com.andrew.metar;


import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ShowImage extends Activity {
	
	ImageView webCamImageView;
	ImageView imageCloseButton;
	Bitmap bitmap;

	private DefaultHttpClient createHttpClient() {
    	
		HttpParams my_httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(my_httpParams, 3000);
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    ThreadSafeClientConnManager multiThreadedConnectionManager = new ThreadSafeClientConnManager(my_httpParams, registry);
	    DefaultHttpClient httpclient = new DefaultHttpClient(multiThreadedConnectionManager, my_httpParams);
		return httpclient;
	}
	
    private class DownloadWebcamImage extends AsyncTask<Void, Void, Void> {

    	byte[] mResultString;
    	Integer mStatusCode;
    	Exception mConnectionException;
    	
		@Override
		protected Void doInBackground(Void... args) {
			
			String fetchUrl = "http://88.159.160.46:60080/cam_1.jpg";

			DefaultHttpClient httpclient = createHttpClient();
			HttpGet httpget = new HttpGet(fetchUrl);
		    
		    try {
				HttpResponse response = httpclient.execute(httpget);
				StatusLine statusLine = response.getStatusLine();
				mStatusCode  = statusLine.getStatusCode();

				if (mStatusCode == 200){
					mResultString = EntityUtils.toByteArray(response.getEntity());
					bitmap = BitmapFactory.decodeByteArray(mResultString, 0, mResultString.length);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				mConnectionException = e;
			} catch (IOException e) {
				e.printStackTrace();
				mConnectionException = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			try {
				
				if (mStatusCode  == 200){
					webCamImageView.setImageBitmap(bitmap);
				}
				else if (mStatusCode  == 404){
					Toast.makeText(ShowImage.this, "Page not found - 404", Toast.LENGTH_LONG).show();
				}
				else if (mStatusCode > 0){
					Toast.makeText(ShowImage.this, "No connection: " + mStatusCode, Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(ShowImage.this, "General error (" +mConnectionException.toString() + ")" , Toast.LENGTH_LONG).show();
				}
			} catch (NumberFormatException e) {
				Toast.makeText(ShowImage.this, "Something went wrong..." , Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
    }

	
	
	
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showimage);
        
        webCamImageView = (ImageView) findViewById(R.id.webCamImageView);
        imageCloseButton = (ImageView) findViewById(R.id.imageCloseButton);

        View root = webCamImageView.getRootView();
        root.setBackgroundColor(0xFF222222);
        
        new DownloadWebcamImage().execute();
	}

	public void close(View view){
    	if (view == imageCloseButton){
    		finish();
    	}
    }

	

	
}
