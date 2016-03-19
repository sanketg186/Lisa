package com.example.hp.lisa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MapsActivity extends FragmentActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,SensorEventListener
{
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST=9000;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GoogleApiClient mgoogleapiclient;
    public static final String TAG=MapsActivity.class.getSimpleName();
    private LocationRequest mlocationrequest;


    private Button start,stop;
    private boolean started=false;
    private SensorManager sensormanager;
    private ArrayList<Data> sensordata;
    float x,y,z;
    long time;
    double currentlatitude,currentlongitude;
    LatLng l;
    //file
    FileOutputStream fos;
    File textfile,kmlfile;
    FileOutputStream fouttext,foutkml;
    OutputStreamWriter osw;
    String result="";
    //Array
    ArrayList<Double>lat;
    ArrayList<Double>lon;
    ArrayList<Double>latgreen;
    ArrayList<Double>longreen;
    ArrayList<Double>latyellow;
    ArrayList<Double>lonyellow;
    ArrayList<Double>latred;
    ArrayList<Double>lonred;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mgoogleapiclient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mlocationrequest=LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(500).setFastestInterval(16);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sensormanager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensordata=new ArrayList<Data>();

        start=(Button)findViewById(R.id.startbutton);
        stop=(Button)findViewById(R.id.stopbutton);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        start.setEnabled(true);
        stop.setEnabled(false);



        setUpMapIfNeeded();



        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);



        RadioGroup rgViews = (RadioGroup) findViewById(R.id.rg_views);

        rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (checkedId == R.id.rb_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.rb_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }

            }
        });

    }




    @Override
    public void onSensorChanged(SensorEvent event) {

        x=event.values[0];
        y=event.values[1];
        z=event.values[2];
        time=System.currentTimeMillis();
        Location location=LocationServices.FusedLocationApi.getLastLocation(mgoogleapiclient);
        handlenewlocation(location);

        String str=""+currentlatitude+"     "+currentlongitude+"     "+x+"     "+y+"     "+z+"     "+time+"\n";
        result+=str;
        lat.add(currentlatitude);
        lon.add(currentlongitude);
        if(Math.abs(z)>3.5&&Math.abs(z)<=5.5){
            latgreen.add(currentlatitude);
            longreen.add(currentlongitude);

        }
        else if(Math.abs(z)>5.5&&Math.abs(z)<=8.5){
            latyellow.add(currentlatitude);
            lonyellow.add(currentlongitude);

        }else if(Math.abs(z)>8.5) {
            latred.add(currentlatitude);
            lonred.add(currentlongitude);

        }


        if(Math.abs(z)>8.5) {
            MarkerOptions options = new MarkerOptions().position(l).title("Critical Condition of Road").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(options);
        }
        else if(Math.abs(z)>5.5&&Math.abs(z)<=8.5){
            MarkerOptions options = new MarkerOptions().position(l).title("Road Requires Maintenance").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            mMap.addMarker(options);
             options = new MarkerOptions().position(l).title("Road have some little problems").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(options);
        }
        else if(Math.abs(z)>3.5&&Math.abs(z)<=5.5){
        }


        Data data=new Data(x,y,z,time);
        sensordata.add(data);





    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.startbutton:
                start.setEnabled(false);
                stop.setEnabled(true);
                makefile();
                sensordata = new ArrayList<Data>();
                started = true;
                Sensor accel = sensormanager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sensormanager.registerListener(this, accel,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case R.id.stopbutton:
                start.setEnabled(true);
                stop.setEnabled(false);
                started = false;

                sensormanager.unregisterListener(this);
                txtfile();
                kmlfile();

                Intent i=new Intent(this,Graph.class);
                i.putExtra("datavalue",sensordata);
                startActivity(i);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    @Override
    protected void onResume() {



        super.onResume();

        lat=new ArrayList<Double>();
        lon=new ArrayList<Double>();
        latgreen=new ArrayList<Double>();
        longreen=new ArrayList<Double>();
        latyellow=new ArrayList<Double>();
        lonyellow=new ArrayList<Double>();
        latred=new ArrayList<Double>();
        lonred=new ArrayList<Double>();






        setUpMapIfNeeded();
        mgoogleapiclient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mgoogleapiclient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleapiclient,this);
            mgoogleapiclient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location location=LocationServices.FusedLocationApi.getLastLocation(mgoogleapiclient);
        Log.i(TAG, "Location services connected");
        if(location==null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient,mlocationrequest,this);

        }else {
            handlenewlocation(location);
        }

    }

    private void handlenewlocation(Location location){
         currentlatitude=location.getLatitude();
         currentlongitude=location.getLongitude();
         l=new LatLng(currentlatitude,currentlongitude);
       // MarkerOptions options=new MarkerOptions().position(l).title("i am here");
       // mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(l));
        Log.d(TAG, location.toString());

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Location services suspended.please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if(connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);

            }catch (IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        }else{
            Log.i(TAG,"Locatoon services connection failed with code"+connectionResult.getErrorCode());
        }



    }

    @Override
    public void onLocationChanged(Location location) {
        handlenewlocation(location);
    }



    public void txtfile(){
        osw=new OutputStreamWriter(fouttext);
        try {
            // osw.write(str);
            osw.append(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void kmlfile(){

        osw=new OutputStreamWriter(foutkml);
        try {

            osw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<kml xmlns=\"http://www.opengis.net/kml/2.2\">"+
                    "\n<Document>"+"\n"+"<Style id=\"thickBlackLine\">\n" +
                    "      <LineStyle>\n" +
                    "        <color>87000000</color>\n" +
                    "        <width>10</width>\n" +
                    "      </LineStyle>\n" +
                    "    </Style>\n"+"<Style id='icon-503-F4EB37-nodesc-normal'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff37EBF4</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>0.0</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<Style id='icon-503-F4EB37-nodesc-highlight'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff37EBF4</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<StyleMap id='icon-503-F4EB37-nodesc'>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>normal</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-F4EB37-nodesc-normal</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>highlight</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-F4EB37-nodesc-highlight</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t</StyleMap>\n"+

                    "\t\t<Style id='icon-503-62AF44-nodesc-normal'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff44AF62</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>0.0</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<Style id='icon-503-62AF44-nodesc-highlight'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff44AF62</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<StyleMap id='icon-503-62AF44-nodesc'>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>normal</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-62AF44-nodesc-normal</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>highlight</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-62AF44-nodesc-highlight</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t</StyleMap>"+
                    "<Style id='icon-503-DB4436-nodesc-normal'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff3644DB</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>0.0</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<Style id='icon-503-DB4436-nodesc-highlight'>\n" +
                    "\t\t\t<IconStyle>\n" +
                    "\t\t\t\t<color>ff3644DB</color>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t\t<Icon>\n" +
                    "\t\t\t\t\t<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>\n" +
                    "\t\t\t\t</Icon>\n" +
                    "\t\t\t\t<hotSpot x='16' y='31' xunits='pixels' yunits='insetPixels'>\n" +
                    "\t\t\t\t</hotSpot>\n" +
                    "\t\t\t</IconStyle>\n" +
                    "\t\t\t<LabelStyle>\n" +
                    "\t\t\t\t<scale>1.1</scale>\n" +
                    "\t\t\t</LabelStyle>\n" +
                    "\t\t\t<BalloonStyle>\n" +
                    "\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>\n" +
                    "\t\t\t</BalloonStyle>\n" +
                    "\t\t</Style>\n" +
                    "\t\t<StyleMap id='icon-503-DB4436-nodesc'>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>normal</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-DB4436-nodesc-normal</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t\t<Pair>\n" +
                    "\t\t\t\t<key>highlight</key>\n" +
                    "\t\t\t\t<styleUrl>#icon-503-DB4436-nodesc-highlight</styleUrl>\n" +
                    "\t\t\t</Pair>\n" +
                    "\t\t</StyleMap>\n" +
                    "\t\t");
            for(int i=0;i<latyellow.size();i++){
                osw.append("\n<Placemark >"+"\n<name>"+i+"</name>"+"<styleUrl>#icon-503-F4EB37-nodesc</styleUrl>"+"\n<Description></Description>"+
                        "\n<Point>\n" +
                        "          <coordinates>"+lonyellow.get(i)+","+latyellow.get(i)+","+"0"+"</coordinates>\n" +
                        "        </Point>\n" +
                        "      </Placemark>"+"\n");

            }
            for(int i=0;i<latgreen.size();i++){
                osw.append("\n<Placemark >"+"\n<name>"+i+"</name>"+"<styleUrl>#icon-503-62AF44-nodesc</styleUrl>"+"\n<Description></Description>"+
                        "\n<Point>\n" +
                        "          <coordinates>"+longreen.get(i)+","+latgreen.get(i)+","+"0"+"</coordinates>\n" +
                        "        </Point>\n" +
                        "      </Placemark>"+"\n");

            }
            for(int i=0;i<latred.size();i++){
                osw.append("\n<Placemark >"+"\n<name>"+i+"</name>"+"<styleUrl>#icon-503-DB4436-nodesc</styleUrl>"+"\n<Description></Description>"+
                        "\n<Point>\n" +
                        "          <coordinates>"+lonred.get(i)+","+latred.get(i)+","+"0"+"</coordinates>\n" +
                        "        </Point>\n" +
                        "      </Placemark>"+"\n");

            }
            osw.append(" \n<Placemark>\n" +
                    "        <name>Relative</name>\n" +
                    "        <visibility>0</visibility>\n" +
                    "        <description>Black line (10 pixels wide), height tracks terrain</description>\n" +
                    "        <styleUrl>#thickBlackLine</styleUrl>\n" +
                    "        <LineString>\n" +
                    "          <tessellate>1</tessellate>\n" +
                    "          <altitudeMode>relativeToGround</altitudeMode>\n" +
                    "          <coordinates>");
            for(int i=0;i<lat.size();i++){

                osw.append(""+lon.get(i)+","+lat.get(i)+","+"0"+"\n" );

            }
            osw.append("</coordinates>\n" +
                    "        </LineString>\n" +
                    "      </Placemark>\n");

            osw.append("</Document>"+"\n</kml>");

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void makefile(){

        String str=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());

        File dir=new File(Environment.getExternalStorageDirectory(),"Lisa");
        if(!dir.exists())
            dir.mkdirs();
        textfile=new File(dir,str+".txt");
        kmlfile=new File(dir,str+".kml");
        kmlfile.setReadable(true, false);
        textfile.setReadable(true, false);
        fouttext= null;
        try {
            fouttext = new FileOutputStream(textfile);
            foutkml =new FileOutputStream(kmlfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
