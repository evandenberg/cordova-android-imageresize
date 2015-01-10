/**
 * An Image Resizer Plugin for Cordova/PhoneGap.
 * 
 * More Information : https://github.com/raananw/
 * 
 * The android version of the file stores the images using the local storage.
 * 
 * The software is open source, MIT Licensed.
 * Copyright (C) 2012, webXells GmbH All Rights Reserved.
 * 
 * @author Raanan Weber, webXells GmbH, http://www.webxells.com
 */
package nl.creativeskills.cordova.imageresize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Base64;

public class ImageResize extends CordovaPlugin {
	private static final String TAG = "ImageResizePlugin";

	public static String IMAGE_DATA_TYPE_BASE64 = "base64Image";
	public static String IMAGE_DATA_TYPE_URL = "urlImage";
	public static String RESIZE_TYPE_FACTOR = "factorResize";
	public static String RESIZE_TYPE_PIXEL = "pixelResize";
	public static String RETURN_BASE64 = "returnBase64";
	public static String RETURN_URI = "returnUri";
	public static String FORMAT_JPG = "jpg";
	public static String FORMAT_PNG = "png";
	public static String FORMAT_PDF = "pdf";
	public static String DEFAULT_FORMAT = "jpg";
	public static String DEFAULT_IMAGE_DATA_TYPE = IMAGE_DATA_TYPE_BASE64;
	public static String DEFAULT_RESIZE_TYPE = RESIZE_TYPE_FACTOR;

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
		PluginResult result = null;
		LOG.d(TAG, action.toString());
		LOG.d(TAG, TAG+": "+Environment.getDataDirectory().toString());

		JSONObject params;
		String imageData;
		String imageDataType;
		String format;
		Bitmap bmp;
		try {
			//parameters (forst object of the json array)
			params = data.getJSONObject(0);
			//image data, either base64 or url
			imageData = params.getString("data");
			//which data type is that, defaults to base64
			imageDataType = params.has("imageDataType") ? params.getString("imageDataType") : DEFAULT_IMAGE_DATA_TYPE;
			LOG.d(TAG, action.toString()+".imageDataType: "+imageDataType+": "+params.getString("imageDataType"));
			//which format should be used, defaults to jpg
			format = params.has("format") ? params.getString("format")
					: DEFAULT_FORMAT;
			//create the Bitmap object, needed for all functions
			bmp = getBitmap(imageData, imageDataType);
			
		} catch (JSONException e) {
			LOG.d(TAG, action.toString()+": "+e.getMessage());
			callbackContext.sendPluginResult( new PluginResult(Status.JSON_EXCEPTION, e.getMessage()) );
			return false;
		} catch (IOException e) {
			LOG.d(TAG, action.toString()+": "+e.getMessage());
			callbackContext.sendPluginResult( new PluginResult(Status.ERROR, e.getMessage()) );
			return false;
		}
		//resize the image
		if (action.equals("resizeImage")) {	
			try {
				LOG.d(TAG, "action.equals("+action.toString()+")");
				double widthFactor;
				double heightFactor;

				//compression quality
				int quality = params.getInt("quality");

				//Pixels or Factor resize
				String resizeType = params.getString("resizeType");

				//Get width and height parameters
				double width = params.getDouble("width");
				double height = params.getDouble("height");

				//return object
				JSONObject res = new JSONObject();

				LOG.d(TAG, "action.equals("+action.toString()+"): params converted");
				
				if (resizeType.equals(RESIZE_TYPE_PIXEL)) {
					widthFactor = width / ((double) bmp.getWidth());
					heightFactor = height / ((double) bmp.getHeight());
				} else {
					widthFactor = width;
					heightFactor = height;
				}
				
				LOG.d(TAG, "action.equals("+action.toString()+"): start resize");
				
				Bitmap resized = getResizedBitmap(bmp, (float) widthFactor, (float) heightFactor);
				

				if (imageDataType.equals(IMAGE_DATA_TYPE_BASE64)) {

					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					if (format.equals(FORMAT_PNG)) {
						resized.compress(Bitmap.CompressFormat.PNG, quality, baos);
					} else {
						resized.compress(Bitmap.CompressFormat.JPEG, quality, baos);
					}

					byte[] b = baos.toByteArray();
					String returnString = Base64.encodeToString(b, Base64.DEFAULT);

					res.put("imageData", returnString);

					LOG.d(TAG, "action.equals("+action.toString()+"):base64 encode the resized Image");
				} else {
					// Obligatory Parameters, throw JSONException if not found
					String filename = params.getString("filename");
					//filename = (filename.contains(".")) ? filename : filename + "." + format;
					String directory = params.getString("directory");
					if ( directory.startsWith("file:") )
					{
						directory = directory.replace("file://", "");
					} else {
						directory = directory.startsWith("/") ? directory : "/" + directory;
						directory = Environment.getExternalStorageDirectory().toString() + directory;
					}
					
					LOG.d(TAG, "action.equals("+action.toString()+"): resized Image and save to "+ directory+filename);

					OutputStream outStream;
					//store the file locally using the external storage directory
					File file = new File(directory, filename);
					
					try {
						outStream = new FileOutputStream(file);
						if (format.equals(FORMAT_PNG)) {
							bmp.compress(Bitmap.CompressFormat.PNG, quality,
									outStream);
						} else {
							bmp.compress(Bitmap.CompressFormat.JPEG, quality,
									outStream);
						}
						outStream.flush();
						outStream.close();

						res.put("url", "file://" + file.getAbsolutePath());
						res.put("size", file.length() );

					} catch (IOException e) {
						result = new PluginResult(Status.ERROR, e.getMessage());
						callbackContext.sendPluginResult( result );
						
						return false;
					}
				}
				
				
				res.put("width", resized.getWidth());
				res.put("height", resized.getHeight());
				
				LOG.d(TAG, action.toString()+": Should be successfull");
				
				result = new PluginResult(Status.OK, res);
				callbackContext.sendPluginResult( result );
				
				return true;
			} catch (JSONException e) {
				LOG.d(TAG, action.toString()+": "+e.getMessage());
				result = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
				callbackContext.sendPluginResult( result );
				
				return false;
			}
		} else if (action.equals("imageSize")) {
			try {

				JSONObject res = new JSONObject();
				res.put("width", bmp.getWidth());
				res.put("height", bmp.getHeight());
				
				result = new PluginResult(Status.OK, res);
				callbackContext.sendPluginResult( result );
				
				return true;
			} catch (JSONException e) {
				result = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
				callbackContext.sendPluginResult( result );
				
				return false;
			}
		} else if (action.equals("storeImage")) {
			try {
				// Obligatory Parameters, throw JSONException if not found
				String filename = params.getString("filename");
				filename = (filename.contains(".")) ? filename : filename + "."
						+ format;
				String directory = params.getString("directory");
				directory = directory.startsWith("/") ? directory : "/"
						+ directory;
				int quality = params.getInt("quality");

				OutputStream outStream;
				//store the file locally using the external storage directory
				File file = new File(Environment.getExternalStorageDirectory()
						.toString() + directory, filename);
				try {
					outStream = new FileOutputStream(file);
					if (format.equals(FORMAT_PNG)) {
						bmp.compress(Bitmap.CompressFormat.PNG, quality,
								outStream);
					} else {
						bmp.compress(Bitmap.CompressFormat.JPEG, quality,
								outStream);
					}
					outStream.flush();
					outStream.close();
					JSONObject res = new JSONObject();
					res.put("url", "file://" + file.getAbsolutePath());
					res.put("size", file.length() );
					
					result = new PluginResult(Status.OK, res);
					callbackContext.sendPluginResult( result );
					
					return true;
				} catch (IOException e) {
					result = new PluginResult(Status.ERROR, e.getMessage());
					callbackContext.sendPluginResult( result );
					
					return false;
				}
			} catch (JSONException e) {
				result = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
				callbackContext.sendPluginResult( result );
				
				return false;
			}
		} else if (action.equals("storePDF")) {
			try {
				// Obligatory Parameters, throw JSONException if not found
				String filename = params.getString("filename");
				filename = (filename.contains(".")) ? filename : filename + "."
						+ format;
				String directory = params.getString("directory");
				directory = directory.startsWith("/") ? directory : "/"
						+ directory;

				OutputStream outStream;
				//store the file locally using the external storage directory
				File file = new File(Environment.getExternalStorageDirectory()
						.toString() + directory, filename);
				try {
					byte[] pdfAsBytes = Base64.decode(imageData.toString(), 0);
					outStream = new FileOutputStream(file);
					
					outStream.write( pdfAsBytes );
					outStream.flush();
					outStream.close();
					JSONObject res = new JSONObject();
					res.put("url", "file://" + file.getAbsolutePath());
					
					result = new PluginResult(Status.OK, res);
					callbackContext.sendPluginResult( result );
					
					return true;
				} catch (IOException e) {
					result = new PluginResult(Status.ERROR, e.getMessage());
					callbackContext.sendPluginResult( result );
					
					return false;
				}
			} catch (JSONException e) {
				result = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
				callbackContext.sendPluginResult( result );
				
				return false;
			}
		} else {
			callbackContext.sendPluginResult( new PluginResult(Status.ERROR) );
			
			return false;
		}
		
	}

	public Bitmap getResizedBitmap(Bitmap bm, float widthFactor, float heightFactor) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(widthFactor, heightFactor);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		LOG.d(TAG, "Width: "+widthFactor+", height:"+heightFactor);
		return resizedBitmap;
	}

	private Bitmap getBitmap(String imageData, String imageDataType) throws IOException {
			Bitmap bmp;
			LOG.d(TAG, "getBitmap called: imageDataType: "+imageDataType);
			if (imageDataType.equals(IMAGE_DATA_TYPE_BASE64)) {
				LOG.d(TAG, IMAGE_DATA_TYPE_BASE64+" called");
				byte[] blob = Base64.decode(imageData, Base64.DEFAULT);
				LOG.d(TAG, IMAGE_DATA_TYPE_BASE64+" still going");
				bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
				LOG.d(TAG, IMAGE_DATA_TYPE_BASE64+" still going 2");
			} else {
				
				
				if ( imageData.startsWith("file:") )
				{
					imageData = imageData.replace("file://", "");
				} else {
					imageData = imageData.startsWith("/") ? imageData : "/" + imageData;
					imageData = Environment.getExternalStorageDirectory().toString() + imageData;
				}
				
				LOG.d(TAG, "imageFile: " + imageData);
				
				File imagefile = new File( imageData );
				FileInputStream fis = new FileInputStream(imagefile);
				bmp = BitmapFactory.decodeStream(fis);
			}
			
			return bmp;
	}

}