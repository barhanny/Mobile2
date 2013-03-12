package com.hanny.sensorsproject;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button scan, generate, acc_btn, azimuth_btn, light_btn, pressure_btn;
	private TextView results;
	
	private String brResult;
	private IntentIntegrator integrator;
	
	private Activity main;
	
	private SensorManager sensorManager;
	private List<Sensor> sensors;
	private Sensor magneticSensor;
	private Sensor accelerometerSensors;
	
	private static final int matrix_size = 9;
	float[] Rotation;
	float[] I;
	float azimuth;
	float[] accels = new float[3];
	float[] mag = new float[3];
	Sensor magnetometer;
	Sensor accelerometer;
    
	
	private Dialog acc_dialog;
	private Dialog light_dialog;
	private Dialog azimuth_dialog;
	private Dialog pressure_dialog;
	private Dialog QR_dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		main = this;
		
		integrator = new IntentIntegrator(this);
		
		scan = (Button)findViewById(R.id.scan_qr_btn);
		generate = (Button)findViewById(R.id.generate_qr_btn);
		
		acc_btn = (Button)findViewById(R.id.acc_btn);
		azimuth_btn = (Button)findViewById(R.id.azimuth_btn);
		light_btn = (Button)findViewById(R.id.light_btn);
		pressure_btn = (Button)findViewById(R.id.pressure_btn);
		
		//get sensor manager
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		//List sensors
		sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		
		//Get default sensor. return null if none
		magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometerSensors = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//check for Sensor minimum update interval
		magneticSensor.getMinDelay();
		accelerometerSensors.getMinDelay();
		
		
		//start sensors
		acc_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				acc_dialog = new Dialog(main);
				acc_dialog.setTitle("Acceleration");
				acc_dialog.setContentView(R.layout.acc_layout);
				acc_dialog.show();
				
				if(sensors.size() > 0)  
					sensorManager.registerListener(accListener, accelerometerSensors, SensorManager.SENSOR_DELAY_NORMAL);
				Button done = (Button)acc_dialog.findViewById(R.id.done_btn);
				done.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						sensorManager.unregisterListener(accListener);
						acc_dialog.cancel();
						
					}
				});
			}
		});
		
		//stop sensors
		azimuth_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				azimuth_dialog = new Dialog(main);
				azimuth_dialog.setTitle("Azimuth");
				azimuth_dialog.setContentView(R.layout.azimuth_layout);
				azimuth_dialog.show();				
				
				
				if(sensors.size() > 0)  {
					sensorManager.registerListener(azimuthListener, accelerometerSensors, SensorManager.SENSOR_DELAY_NORMAL);
					sensorManager.registerListener(azimuthListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
				Button done = (Button)azimuth_dialog.findViewById(R.id.done_btn);
				done.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						sensorManager.unregisterListener(azimuthListener);
						azimuth_dialog.cancel();
						
					}
				});
			}
		});
		
		scan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				integrator.initiateScan();
				
			}
		});
		
		generate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				QR_dialog = new Dialog(main);
				QR_dialog.setContentView(R.layout.generate_qr_layout);
				QR_dialog.setTitle("Generate QR");
				QR_dialog.show();
				
				
				Button genarate = (Button) QR_dialog.findViewById(R.id.done_generate);
				genarate.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						EditText stringtoQR = (EditText)QR_dialog.findViewById(R.id.edit_string);
						integrator.shareText(stringtoQR.getText().toString());
						QR_dialog.cancel();
						
					}
				});
				
			}
		});
		
		light_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	//handle result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			brResult = scanResult.getContents();
			//do something with result
		}
	}
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ² alpha ² 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	static final float ALPHA = 0.15f;

	protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;
	     
	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	SensorEventListener magListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = event.values;
			Log.i("magnometer",values[0]+" "+ values[1]+" "+ values[2]);
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};
	
	SensorEventListener accListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = event.values;
			Log.i("accelerometer",values[0]+" "+ values[1]+" "+ values[2]);
			
			TextView x = (TextView)acc_dialog.findViewById(R.id.x_result);
			TextView y = (TextView)acc_dialog.findViewById(R.id.y_result);
			TextView z = (TextView)acc_dialog.findViewById(R.id.z_result);
			
			x.setText(String.valueOf(values[0]));
			y.setText(String.valueOf(values[1]));
			z.setText(String.valueOf(values[2]));
			
			}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
		}
	};
	

	SensorEventListener azimuthListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			

			
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometer = event.sensor;
				accels = lowPass( event.values.clone(), accels );
			}
			if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magnetometer = event.sensor;
				mag = lowPass( event.values.clone(), mag );
			}
		    if ((accels != null) && (mag != (null))) {
				Rotation = new float[matrix_size];
				I = new float[matrix_size];

		        boolean success = SensorManager.getRotationMatrix(Rotation, I, accels, mag);
		        if (success) {
		          float orientation[] = new float[3];
		          SensorManager.getOrientation(Rotation, orientation);
		          azimuth = (float)Math.toDegrees(orientation[0]);
		          if(azimuth < 0)
		        	  azimuth = azimuth + 360; 
		          Log.i("tg",String.valueOf(azimuth));	
		          mag = new float[3];
		          accels = new float[3];
		          
				TextView azimith_result = (TextView)azimuth_dialog.findViewById(R.id.azimuth_result);		      
		          azimith_result.setText(String.valueOf(azimuth));
		        }
		    }
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	@Override
	public void onDestroy()
	{
		sensorManager.unregisterListener(magListener);
		sensorManager.unregisterListener(accListener);
		super.onDestroy();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
