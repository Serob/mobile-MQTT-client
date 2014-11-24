package com.spb.sezam.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.vk.sdk.api.VKError;

public class ActivityUtil {

	private ActivityUtil(){
	}
	
	public static void showError(Context context, VKError error) {
		String message = error.errorMessage;
		if(message == null && error.apiError != null){
			message = error.apiError.errorMessage;
		}
        showError(context, message);

        if (error.httpError != null) {
            Log.w("Test", "Error in request or upload: " + error.httpError.getMessage(), error.httpError);
        }
    }
	
	public static void showError(Context context, String message){
		new AlertDialog.Builder(context)
		        .setMessage(message)
		        .setPositiveButton("OK", null)
		        .show();
	}
}
