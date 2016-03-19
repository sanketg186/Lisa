package com.example.hp.lisa;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;


public class Graph extends ActionBarActivity implements View.OnClickListener{

    private LinearLayout layout;
    private GraphicalView mChart;
    private ArrayList<Data>sensorData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        // Bundle sensorvalue =getIntent().getExtras();
        layout = (LinearLayout) findViewById(R.id.chart_container);
        sensorData=(ArrayList<Data>)getIntent().getSerializableExtra("datavalue");
        openChart();
        mChart.setOnClickListener(this);   //2
    }






    public void openChart(){
        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTime();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");

            for (Data data : sensorData) {
                xSeries.add(data.getTime() - t, data.getX());
                ySeries.add(data.getTime() - t, data.getY());
                zSeries.add(data.getTime() - t, data.getZ());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.YELLOW);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            //multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Time(in millisecond)");
            multiRenderer.setYTitle("Values of Acceleration(in m/sec^2)");
            multiRenderer.setZoomButtonsVisible(true);
            multiRenderer.setShowGrid(true);
            multiRenderer.setShowGridY(true);
            multiRenderer.setShowGridX(true);
            multiRenderer.setGridColor(Color.LTGRAY);
            multiRenderer.setAxesColor(Color.BLACK);
            multiRenderer.setBackgroundColor(Color.WHITE);
            multiRenderer.setShowAxes(true);
            multiRenderer.setMarginsColor(Color.LTGRAY);
            // multiRenderer.setShowLabels(true);
            multiRenderer.setShowCustomTextGrid(true);
            multiRenderer.setXLabelsColor(Color.BLACK);
            multiRenderer.setYLabelsColor(0, Color.BLACK);
            multiRenderer.setXLabelsAlign(Paint.Align.CENTER);
            multiRenderer.setYLabelsAlign(Paint.Align.CENTER,0);
            multiRenderer.setClickEnabled(true);
            multiRenderer.setSelectableBuffer(10);
            multiRenderer.setPanLimits(new double[]{0.0,Double.MAX_VALUE,-50,Double.MAX_VALUE});

          /*  for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1,""
                        + (sensorData.get(i).getTime() - t));
            }*/
            for (int i = -12; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, ""+i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);
            // Getting a reference to LinearLayout of the MainActivity Layout


            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

        SeriesSelection seriesSelection=mChart.getCurrentSeriesAndPoint();
        if(seriesSelection!=null){
            Toast.makeText(this,"x="+seriesSelection.getXValue()+" y="+seriesSelection.getValue(),Toast.LENGTH_SHORT).show();
        }
    }
}
