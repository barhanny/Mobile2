package com.hanny.sensorsproject;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class SensorsService extends Service {

	SensorManager sensorManager;
	List<Sensor> sensors;
	Sensor magneticSensor;
	Sensor accelerometerSensors;
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
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
		
		if(sensors.size() > 0) {
			sensorManager.registerListener(magListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(accListener, accelerometerSensors, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
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
	

}
