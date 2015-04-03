/**
 * Copyright (c) 2009-2013 by Benjamin Bahrenburg. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.light;

import java.io.IOException;
import java.util.List;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;


@Kroll.module(name="Tilight", id="ti.light")
public class TilightModule extends KrollModule {

	// Standard Debugging variables
	private static final String TAG = "TilightModule";
	
	//flag to detect flash is on or off
	// private boolean isLightOn = false;
	public boolean isLightOn = false;
	
	//Is supported Results
	private static boolean isSupported = false;
	private static Camera camera;
	private static Parameters params;
	

	public TilightModule() {
		super();
	}

	
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		
		PackageManager pm = app.getApplicationContext().getPackageManager();

		// if device support camera?
		isSupported = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

		if (isSupported) {
            
			camera = Camera.open();
		
            List<String> supportedFlashModes = camera.getParameters().getSupportedFlashModes();
            
            if (supportedFlashModes == null
                    || (supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals("off"))) {
                
            	isSupported = false;
            }
            
            camera.release();
            camera = null;
        }
	}
	
	
	@Override
    public void onStop(Activity activity) {
        
		super.onStop(activity);
        turnOff();
    }

    @Override
    public void onPause(Activity activity) {
        
    	super.onPause(activity);
        turnOff();
    }
	

	@Kroll.method
	public boolean isSupported() {
		
		return isSupported;
	}
	

	@Kroll.method
	public boolean isOn() {
		
		return isLightOn;
	}

	
	@Kroll.method
	public void turnOn() {
		
		if (!isLightOn && isSupported) {
			
			camera = Camera.open();
			params = camera.getParameters();
			
			
			if (Build.VERSION.SDK_INT >= 11) {

	        	try {

					camera.setPreviewTexture(new SurfaceTexture(0));
				}
				catch (IOException e) {

					Log.e(TAG, e.toString());
				}
	        }
			
			
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);

			camera.setParameters(params);
			camera.startPreview();

			isLightOn = true;
		}
	}
	

	@Kroll.method
	public void turnOff() {
		
		if (isLightOn && isSupported) {
			
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);

			camera.setParameters(params);
			camera.stopPreview();
			// camera.setPreviewCallback(null);
			
			
			if (Build.VERSION.SDK_INT >= 11) {
				
				try {
					
					camera.setPreviewTexture(null);
				}
				catch (IOException exception) {
					
					Log.e(TAG, exception.toString());
				}
			}
			
			camera.release();
			camera = null;

			isLightOn = false;
		}
	}
	

	@Kroll.method
	public void toggle() {
		
		if (isSupported) {
			
			if (isLightOn) {
				
				turnOff();
			}
			else {
				
				turnOn();
			}
		}
	}
}
