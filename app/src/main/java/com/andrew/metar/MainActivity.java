package com.andrew.metar;

import java.io.IOException;
import java.lang.String;
import java.lang.Math;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;

	ProgressDialog mProgressDialog;
	ImageView logoView;
	TextView timeView;
	TextView windView;
	TextView crosswindView;
    TextView crosswindlabelView;
	TextView visibilityView;
	TextView cloudsView;
	TextView temperatureView;
	TextView dewpointView;
	TextView qnhView;
	TextView rawmetarView;
	TextView last_updateView;
	TextView press_altView;
	TextView sigwxView;
	TextView aerodrome_infoView;
	TextView transitionlevelView;
	TextView UDPView;
	TextView dens_altView;
	ImageView imageView2;
	ImageView imageConditions;

	//16/08/2016: version 1.2
    //- Converted to Gradle
    //- Fixed typo when crosswind is 1 knot
    //- Removed webcam button, refresh is now swipe down

    //17/08/2016 version 1.3
    //- Added EACm logo
    //- Added call eacm option
    //- Implemented save functionality
    //- Bugfix CAVOK without 9999 or cloud group

	//TODO: history graphs
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

		logoView = (ImageView) findViewById(R.id.imageLogo);
        timeView = (TextView) findViewById(R.id.timeView);
        windView = (TextView) findViewById(R.id.windView);
        crosswindView = (TextView) findViewById(R.id.crosswindView);
        crosswindlabelView = (TextView) findViewById(R.id.crosswindlabelView);
        visibilityView = (TextView) findViewById(R.id.visibilityView);
        cloudsView = (TextView) findViewById(R.id.cloudsView);
        temperatureView = (TextView) findViewById(R.id.temperatureView);
        dewpointView = (TextView) findViewById(R.id.dewpointView);
        qnhView = (TextView) findViewById(R.id.qnhView);
        rawmetarView = (TextView) findViewById(R.id.rawmetarView);
        last_updateView = (TextView) findViewById(R.id.last_updateView);
        press_altView = (TextView) findViewById(R.id.press_altView);
        sigwxView = (TextView) findViewById(R.id.sigwxView);
        dens_altView = (TextView) findViewById(R.id.dens_altView);
        aerodrome_infoView = (TextView) findViewById(R.id.aerodrome_infoView);
        transitionlevelView = (TextView) findViewById(R.id.transitionlevelView);
        UDPView = (TextView) findViewById(R.id.UDPView); 
        imageConditions = (ImageView) findViewById(R.id.imageConditions); 

        // Find the root view by getRootView on a random object in the view
        View root = timeView.getRootView();

        // Set the color
        root.setBackgroundColor(0xFF222222);

        // Load data at startup
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("Retrieving METAR data...");
		mProgressDialog.show();
		new DownloadUserInfoTask().execute();
	}

    @Override
    public void onRefresh() {
        Toast.makeText(this, "METAR loaded", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
        new DownloadUserInfoTask().execute();
    }


	public void call_eacm(View view){
		if (view == logoView) {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:0643853201"));
			startActivity(callIntent);
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	Intent ShowSettings = new Intent(MainActivity.this, ShowSettings.class);
        		startActivity(ShowSettings);
            	return true;
            case R.id.action_call_eacm:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:0643853201"));
                startActivity(callIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }    

    private DefaultHttpClient createHttpClient() {
    	
		HttpParams my_httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(my_httpParams, 3000);
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    ThreadSafeClientConnManager multiThreadedConnectionManager = new ThreadSafeClientConnManager(my_httpParams, registry);
	    DefaultHttpClient httpclient = new DefaultHttpClient(multiThreadedConnectionManager, my_httpParams);
		return httpclient;
	}

    private class DownloadUserInfoTask extends AsyncTask<Void, Void, Void> {
		String mResultString;
		Integer mStatusCode=0;
  		Exception mConnectionException;
    	
    	
		@Override
		protected Void doInBackground(Void... args) {
			String fetchUrl = "http://www.alphapapa.nl/metar/";

			DefaultHttpClient httpclient = createHttpClient();
			HttpGet httpget = new HttpGet(fetchUrl);
		    
		    try {
				HttpResponse response = httpclient.execute(httpget);
				StatusLine statusLine = response.getStatusLine();
				mStatusCode  = statusLine.getStatusCode();

				if (mStatusCode == 200){
					mResultString = EntityUtils.toString(response.getEntity());
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

		@SuppressWarnings("unused")
		@Override
		protected void onPostExecute(Void arg) {
			
			Integer iVelocity,iGusts,iCrossWindLimit=0,iCross =0;
	    	String mHours, mMinutes, mDay, mDirection;
	    	String mVelocity="", mCloudString="",mSigwx="",mGusts="",mCross="";
	    	String mVisibility=null;
            boolean mCAVOK=false;
			String mCloudType,mCloudTypeText = null, mWindView = null;
			String mTempDew;
			String mModifier = null,mType = null, mTypeString = null, mIndication = null, mIndicationString = null;
			String mConditions = "VMC";
	    	
			try {
				mProgressDialog.dismiss();
				if (mStatusCode  == 200){
					String[] message = mResultString.split(";");
					String[] metar = message[0].split(",");
					String[] index = message[1].split(",");
					
					Integer i=0, iCloudHeight, iVisibility=null, iTemperature = 0, iDewpoint, iQNH;

					long lRH,lPressureAltitude=0, lTimeDifference;
					
					boolean bCB=false, bEndOfMessage=false, bTemporary=false;


					Map<String, String> map = new HashMap<String, String>();
					map.put("DZ", "drizzle");
					map.put("SG", "snow grains");
					map.put("IC", "ice crystals");
					map.put("PL", "ice pellets");
					map.put("GR", "hail");
					map.put("GS", "small hail");
					map.put("SN", "snow");
					map.put("RA", "rain");
					map.put("FG", "fog");
					map.put("BR", "mist");
					map.put("FU", "smoke");
					map.put("VA", "volcanic ash");
					map.put("DU", "dust");
					map.put("SA", "sand");
					map.put("HZ", "haze");
					map.put("PO", "dust whirl");
					map.put("SQ", "squalls");
					map.put("FC", "funnel cloud");
					map.put("SS", "sandstorm");
					map.put("DS", "duststorm");
					
					map.put("MI", "shallow");
					map.put("BC", "banks of");
					map.put("PR", "partial");
					map.put("DR", "low drifting");
					map.put("BL", "blowing");
					map.put("SH", "showers");
					map.put("TS", "thunderstorms");
					map.put("FZ", "freezing");
					map.put("RE", "recent");
					

					crosswindView.setTextColor(Color.WHITE);
					visibilityView.setTextColor(Color.WHITE);
					cloudsView.setTextColor(Color.WHITE);
					
					//start stream scan
					for (String data : index){
						if(data.equals("time")){ 
							mDay = metar[i].substring(0,2);
							mHours = metar[i].substring(2,4);
							mMinutes = metar[i].substring(4,6);
							timeView.setText("Time: " + mHours + ":" + mMinutes + "UTC");
						
							Calendar MetarTime = Calendar.getInstance();
							Calendar CurrentTime = Calendar.getInstance();
	
							CurrentTime.setTimeZone(TimeZone.getTimeZone("UTC"));
							MetarTime.setTimeZone(TimeZone.getTimeZone("UTC"));
							MetarTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(mDay));
							MetarTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mHours));
							MetarTime.set(Calendar.MINUTE, Integer.valueOf(mMinutes));
							MetarTime.set(Calendar.SECOND, 0);
	
							lTimeDifference = CurrentTime.getTimeInMillis()-MetarTime.getTimeInMillis();
							lTimeDifference = lTimeDifference/60000;
							
							last_updateView.setText("(" + lTimeDifference + " minutes ago)");
						}
						
						if(data.equals("wind")){
							if(metar[i].length()==7){
								//no gusts
								mDirection = metar[i].substring(0,3);
								mVelocity = metar[i].substring(3,5);
								iVelocity = Integer.valueOf(mVelocity);
								iCross = 0;
								
								if(!bEndOfMessage) {
									if (mDirection.equals("VRB")){
										mWindView = "Variable at " + iVelocity + " knots";
										iCross = 0;
										mCross = "N/A";
									} else {
										iCross = (int) (Integer.valueOf(mVelocity)*Math.sin((Integer.valueOf(mDirection)-30)*Math.PI/180));
										if(iCross==1) {
											mCross = Math.abs(iCross) + " knot";
										}else{
											mCross = Math.abs(iCross) + " knots";
										}
										mWindView = mDirection +"° at " + iVelocity + " knots";
									}
									
								} else {
									if(!bTemporary){
										mWindView = mWindView + "\nbcmg ";
									} else {
										mWindView = mWindView + "\ntempo ";
									}
	
									if (mDirection.equals("VRB")){
										mWindView = mWindView +"variable at " + iVelocity + " knots";
									} else {
										mWindView = mWindView + mDirection +"° at " + iVelocity + " knots";
									}
								}
							} else {
								//gusts = true
								mDirection = metar[i].substring(0,3);
								mVelocity = metar[i].substring(3,5);
								iVelocity = Integer.valueOf(mVelocity);
								mGusts = metar[i].substring(6,8);
								iGusts = Integer.valueOf(mGusts);

								if(!bEndOfMessage) {
									if (mDirection.equals("VRB")){
										mWindView = "Variable at " + iVelocity + " knots max " + iGusts + " knots";
										iCross = 0;
										mCross = "N/A";

									} else {
										mWindView = mDirection +"° at " + iVelocity + " knots max " + iGusts + " knots";
										iCross = (int) ((int) iGusts*Math.sin((Integer.valueOf(mDirection)-35)*Math.PI/180));
										mCross = Math.abs(iCross) + " knots max";
									}
								} else {
									if(!bTemporary){
										mWindView = mWindView + "\nbcmg ";
									} else {
										mWindView = mWindView + "\ntempo ";
									}
	
									if (mDirection.equals("VRB")){
										mWindView = mWindView +"variable at " + iVelocity + " knots max " + iGusts + " knots";
									} else {
										mWindView = mWindView + mDirection +"° at " + iVelocity + " knots max " + iGusts + " knots";
									}
								}
							}
							windView.setText(mWindView);

							crosswindView.setText(mCross);

							if (mDirection.equals("VRB")){
								crosswindlabelView.setText("Crosswind:");
							} else {
								if (Integer.valueOf(mDirection) >= 120 && Integer.valueOf(mDirection) <= 300) {
									crosswindlabelView.setText("Crosswind 21:");
								} else {
									crosswindlabelView.setText("Crosswind 03:");
								}
							}
						}

						if(data.equals("sigwx")){
							if(metar[i].contains("+")){
								mModifier = "severe ";
							} else {
								if(metar[i].contains("-")){
									mModifier = "light ";
								} else {
									mModifier = "";
								}
							}

							if(metar[i].length()>3){
								mType = metar[i].substring(metar[i].length()-2,metar[i].length());
								mIndication = metar[i].substring(metar[i].length()-4,metar[i].length()-2);
								mTypeString = map.get(mType);
								mIndicationString = map.get(mIndication);
								mSigwx = mSigwx + mModifier + mIndicationString + " " + mTypeString+"\n";
							} else {
								mType = metar[i].substring(metar[i].length()-2,metar[i].length());
								mTypeString = map.get(mType);
								mSigwx = mSigwx + mModifier + mTypeString+"\n";
							}
						}

                        if(data.equals("cavok")){
                            mCAVOK = true;
                        }

						if(data.equals("vis")){
							iVisibility = Integer.valueOf(metar[i]);

                            if(mCAVOK) {
                                if (iVisibility == null) {
                                    iVisibility = 9999;
                                }
                            }

                            if(!bTemporary){
								if(iVisibility == 9999){
									mVisibility = "10 km or more";
								} else {
									mVisibility = iVisibility + " m";
									 
									if(iVisibility < 5000){
										mConditions="IMC";
										visibilityView.setTextColor(Color.RED);
									} else {
										visibilityView.setTextColor(Color.WHITE);
									}
								}
							}else {
                                if (iVisibility == 9999) {
                                    mVisibility = mVisibility + "\ntemporary 10 km or more";
                                } else {
                                    mVisibility = mVisibility + "\ntemporary " + iVisibility + " m";

                                    if (iVisibility < 5000) {
                                        mConditions = "IMC";
                                        visibilityView.setTextColor(Color.RED);
                                    } else {
                                        visibilityView.setTextColor(Color.WHITE);
                                    }
                                }
                            }
                            visibilityView.setText(mVisibility);
						}



                        if(data.equals("cloud")){
							mCloudType=metar[i].substring(0,3);
							
							if(mCloudType.equals("NCD")||mCloudType.equals("NSC")) {
								mCloudTypeText = "no clouds detected\n";
								if(!bEndOfMessage) mCloudString = mCloudString + mCloudTypeText;
							} else {
								if(metar[i].length()>6) bCB = true;
								iCloudHeight=Integer.valueOf(metar[i].substring(3,6));
								iCloudHeight=iCloudHeight*100;
								
								if(mCloudType.equals("FEW")) {mCloudTypeText = "few";}
								if(mCloudType.equals("SCT")) {mCloudTypeText = "scattered";}
								if(mCloudType.equals("BKN")) {
									mCloudTypeText = "broken"; 
									if(iCloudHeight<2000) {
										mConditions="IMC";
										cloudsView.setTextColor(Color.RED);
									} else {
										cloudsView.setTextColor(Color.WHITE);
									}
								}
								
								if(mCloudType.equals("OVC")) {
									mCloudTypeText = "overcast"; 
									if(iCloudHeight<2000) {
										mConditions="IMC";
										cloudsView.setTextColor(Color.RED);
									} else {
										cloudsView.setTextColor(Color.WHITE);
									}
								}
								
								if(!bEndOfMessage) {
									if(bCB){
										mCloudString = mCloudString + mCloudTypeText + " at " + iCloudHeight + " feet (CB)\n";	
									}else{
										mCloudString = mCloudString + mCloudTypeText + " at " + iCloudHeight + " feet\n";
									}
								} else {
									if(bCB){
										mCloudString = mCloudString + "bcmg " + mCloudTypeText + " at " + iCloudHeight + " feet (CB)\n";
									}else{
										mCloudString = mCloudString + "bcmg " + mCloudTypeText + " at " + iCloudHeight + " feet\n";
									}
								}
							}
							
						}
						
						if(data.equals("tempdew")){
							mTempDew = metar[i];
							if(mTempDew.length() == 5){            // 21/20
								iTemperature = Integer.valueOf(metar[i].substring(0,2));
								iDewpoint = Integer.valueOf(metar[i].substring(3,5));
							} else {                            
								if(mTempDew.length() == 7){        // M05/M04    
									iTemperature = Integer.valueOf(metar[i].substring(1,3))*-1;
									iDewpoint = Integer.valueOf(metar[i].substring(5,7))*-1;
								} else {
									if(metar[i].substring(0,1) == "M"){ // M05/05 > hardly possible but still
										iTemperature = Integer.valueOf(metar[i].substring(1,3))*-1;
										iDewpoint = Integer.valueOf(metar[i].substring(4,6));										
									} else {						// 05/M01
										iTemperature = Integer.valueOf(metar[i].substring(0,2));
										iDewpoint = Integer.valueOf(metar[i].substring(4,6))*-1;
									}
								}
							}

							lRH = Math.round(100 * (Math.exp((17.271*iDewpoint)/(237.7+iDewpoint))/Math.exp((17.271*iTemperature)/(237.7+iTemperature))));
							temperatureView.setText(iTemperature + "\u2103");
							dewpointView.setText(iDewpoint + "\u2103 (RH: " + lRH + "%)");
						}
						
						if(data.equals("udp")){
							String mUDP=null;
							String mSunriseHours=null, mSunriseMinutes=null;
							String mSunsetHours=null, mSunsetMinutes=null;

							Calendar MetarTime = Calendar.getInstance();
							Calendar CurrentTime = Calendar.getInstance();

							
							//format: 05:26-17:32
							mUDP = metar[i];
							String[] udp = mUDP.split("-");
							
							mSunriseHours = udp[0].substring(0,2);
							mSunriseMinutes = udp[0].substring(3,5);
					
							mSunsetHours = udp[1].substring(0,2);
							mSunsetMinutes = udp[1].substring(3,5);

							
							Calendar SunriseTime = Calendar.getInstance();
							Calendar SunsetTime = Calendar.getInstance();
	
							SunriseTime.setTimeZone(TimeZone.getTimeZone("UTC"));
							SunriseTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mSunriseHours));
							SunriseTime.set(Calendar.MINUTE, Integer.valueOf(mSunriseMinutes));
							SunriseTime.set(Calendar.SECOND, 0);
	
							SunsetTime.setTimeZone(TimeZone.getTimeZone("UTC"));
							SunsetTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mSunsetHours));
							SunsetTime.set(Calendar.MINUTE, Integer.valueOf(mSunsetMinutes));
							SunsetTime.set(Calendar.SECOND, 0);

							
							if(CurrentTime.getTimeInMillis()>SunriseTime.getTimeInMillis()
								&& CurrentTime.getTimeInMillis()<SunsetTime.getTimeInMillis()){
									//during UDP
							} else {
									//outside UDP
								if(mConditions.equals("IMC")){
									mConditions="IMC";
								} else {
									mConditions="NVFR";
								}
							}
							UDPView.setText(mUDP + " UTC");
						}
						
						if(data.equals("qnh")){
							String mTL=null;
							iQNH = Integer.valueOf(metar[i].substring(1));
							qnhView.setText(iQNH + " hPa");
							lPressureAltitude=Math.round(72 + 27*(1013 - iQNH));
							press_altView.setText(lPressureAltitude + " feet");
							
							//table based on 4000ft transition altitude
							if(iQNH<959) mTL = "FL060";
							if(iQNH<977 && iQNH>=959) mTL = "FL055";
							if(iQNH<995 && iQNH>=977) mTL = "FL050";
							if(iQNH<1013 && iQNH>=995) mTL = "FL045";
							if(iQNH<1032 && iQNH>=1013) mTL = "FL040";
							if(iQNH>=1032) mTL = "FL035";
							
							transitionlevelView.setText(mTL);
							
							bEndOfMessage = true;
						}
						if(data.equals("tempo")){
							bTemporary = true;
						}
						
					i++;	
					}
					// end of stream scan. Start post-processing
					
					long lDensityAltitude = Math.round(lPressureAltitude + (120*((lPressureAltitude/500)+iTemperature-15)));
					//long lDensityAltitude = Math.round(lPressureAltitude + 3.7);
					dens_altView.setText(lDensityAltitude + " feet");

                    if(mCAVOK = true && mCloudString.equals("")){
                        mCloudString ="CAVOK";
                    } else {
					    mCloudString = mCloudString.substring(0, mCloudString.length() - 1);
                    }
					cloudsView.setText(mCloudString);

					if(mSigwx.equals("")) {
						sigwxView.setText("no significant weather");
					} else {
						mSigwx = mSigwx.substring(0,mSigwx.length()-1);
						sigwxView.setText(mSigwx);
					}
					
					if(mConditions.equals("VMC")){
						imageConditions.setVisibility(View.VISIBLE);
						imageConditions.setImageResource(R.drawable.vfr);
					} else {
						if(mConditions.equals("IMC")){
							imageConditions.setVisibility(View.VISIBLE);
							imageConditions.setImageResource(R.drawable.ifr);
						} else {
							if(mConditions.equals("NVFR")){
								imageConditions.setVisibility(View.VISIBLE);
								imageConditions.setImageResource(R.drawable.nvfr);
							} else {
								imageConditions.setVisibility(View.INVISIBLE);
							}
						}
					}

                    //Retrieve crosswind limit setting
                    SharedPreferences prefs = getSharedPreferences("com.andrew.metar.settings", MODE_PRIVATE);
                    iCrossWindLimit = prefs.getInt("crosswind_limit", 0);

                    if(Math.abs(iCross) > iCrossWindLimit){
                        crosswindView.setTextColor(Color.RED);
                    } else {
                        crosswindView.setTextColor(Color.WHITE);
                    }

                    rawmetarView.setText("METAR: \n" + message[2]);
					bCB=false;				
				}
				else if (mStatusCode  == 404){
					Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_LONG).show();
				}
                else if (mStatusCode  == 500){
                    Toast.makeText(MainActivity.this, "Server error: script failed", Toast.LENGTH_LONG).show();
                }
				else if (mStatusCode > 0){
					Toast.makeText(MainActivity.this, "No connection", Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(MainActivity.this, "No connection" , Toast.LENGTH_LONG).show();
				}
			} catch (NumberFormatException e) {
				Toast.makeText(MainActivity.this, "Something went wrong..." , Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
    }
		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
