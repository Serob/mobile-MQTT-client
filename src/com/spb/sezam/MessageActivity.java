package com.spb.sezam;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spb.sezam.utils.ActivityUtil;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MessageActivity extends Activity implements OnClickListener{

	public static final String ICON_SPLIT_SYMBOLS = "|_";
	
	private List<String> messageToSend = new ArrayList<String>();
	private JSONArray allMessages = new JSONArray();
	private String activeFriendName = null;
	private int activeFriendId;
	
	private VKRequestListener messageSendListener  = new VKRequestListener(){

		@Override
		public void onComplete(VKResponse response) {
			LinearLayout formLayout = (LinearLayout) findViewById(R.id.linearLayout1);
			formLayout.removeAllViews();
			messageToSend.clear();
			Toast showSent = Toast.makeText(getApplicationContext(), "—ообщение отправлено", Toast.LENGTH_SHORT);
			showSent.show();
		}

		@Override
		public void onError(VKError error) {
			ActivityUtil.showError(MessageActivity.this, error);
		}
		
	};
	
	private VKRequestListener messageRecieveListener  = new VKRequestListener(){

		@Override
		public void onComplete(VKResponse response) {    
	        //List<String[]> ourMessages = null; 
	        try {
	        	JSONArray messages = response.json.getJSONObject("response").getJSONArray("items");
	        	//ourMessages = filterMessages(messages, true);
	        	//show images
	            //decodeTextToImages(ourMessages);
	        	showHistory(messages);
	            //must be only when Activity starts
	            scorllDown((ScrollView)findViewById(R.id.scrollView1));
	            //
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onError(VKError error) {
			ActivityUtil.showError(MessageActivity.this, error);
		}
	};
	
	/*public List<String[]> filterMessages(JSONArray messages, boolean incomeOnly) throws JSONException{
		String message = null;
		List<String[]> ourMessages = new ArrayList<String[]>();
		JSONObject messageJson = null;
		
		for(int i=0; i < messages.length(); i++ ){
			messageJson = messages.getJSONObject(i);
			int out = messageJson.getInt("out");
			
			if(incomeOnly && out == 1){
				continue;
			}
			
			message =  messageJson.getString("body").trim();
			if(message.startsWith(APP_SEPARATOR_MESSAGE)){
				message = message.replace(APP_SEPARATOR_MESSAGE, "");
				String[] messageArr =  message.split(",");
				ourMessages.add(messageArr);
			}
		}
		return ourMessages;
	}
	
	public List<String[]> filterMessages(JSONArray messages) throws JSONException{
		return filterMessages(messages, false);
	}*/
	
	public void showHistory(JSONArray messages) throws JSONException{
		String messageString = null;
		JSONObject messageJson = null;
		String[] messageArr = null;
		LinearLayout historyLayout = (LinearLayout)findViewById(R.id.messageHistory);
		
		for(int i=messages.length() - 1; i >= 0; i-- ){
			messageJson = messages.getJSONObject(i);
			TextView nameView = new TextView(MessageActivity.this);
			if(messageJson.getInt("out") == 1){
				nameView.setText("¬ы");
				nameView.setTextColor(Color.BLACK);
			} else {
				nameView.setText(activeFriendName);
				nameView.setTextColor(Color.BLUE);
			}
			nameView.setTypeface(null, Typeface.BOLD);
			historyLayout.addView(nameView);
			
			messageString = messageJson.getString("body").trim();
			messageArr =  messageString.split("\\" + ICON_SPLIT_SYMBOLS);
			
			LinearLayout linearLayout = new LinearLayout(MessageActivity.this);
			historyLayout.addView(linearLayout);
			
			//Analyze message parts
			for(String text : messageArr){
				showTextWithImages(text, linearLayout);
			}
		}
	}
	
	/**
	 * Shows text into the history layout as it is. Should be called if there is no info about image icon in the text.
	 * @param text Text to be shown
	 * @param lLayout {@link TextView} with {@code 'text'} parameter will be added to this layout
	 */
	private void showTextAsString(String text, LinearLayout lLayout) {
		//if there is no info about image icon in the text
		
		if(text == null || "".equals(text)){
			return;
		}
		
		TextView textView = new TextView(MessageActivity.this);
		textView.setText(text);
		lLayout.addView(textView);
	}
	
	private void showTextWithImages(String text, LinearLayout lLayout){
		if(text == null || "".equals(text)){
			return;
		}
		
		ImageView image = new ImageView(MessageActivity.this);
		
		if("чувствовать".equals(text)){
			image.setBackgroundResource(R.drawable.image_1_thumb);
		} else if("€".equals(text)){
			image.setBackgroundResource(R.drawable.image_2_thumb);
		} else if("хорошо".equals(text)){
			image.setBackgroundResource(R.drawable.image_3_thumb);
		} else if("чувствовать себ€".equals(text)){
			image.setBackgroundResource(R.drawable.image_4_thumb);
		//none of words
		} else{
			showTextAsString(text, lLayout);
			return;
		}
		
		//if icon name is found
		lLayout.addView(image);
		
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		ImageButton btn1 = (ImageButton)findViewById(R.id.imageButton1);
        btn1.setOnClickListener(this);

        ImageButton btn2 = (ImageButton)findViewById(R.id.imageButton2);
        btn2.setOnClickListener(this);

        ImageButton btn3 = (ImageButton)findViewById(R.id.imageButton3);
        btn3.setOnClickListener(this);

        ImageButton btn4 = (ImageButton)findViewById(R.id.imageButton4);
        btn4.setOnClickListener(this);

        Intent intent = getIntent();
        //String activefriend = intent.getStringExtra(FriendsActivity.EXTRA_MESSAGE);
        
		try {
			JSONObject activeFriend = new JSONObject(intent.getStringExtra(FriendsActivity.EXTRA_MESSAGE));
			activeFriendName = activeFriend.getString("first_name") + " " + activeFriend.getString("last_name");
			activeFriendId = activeFriend.getInt("id");
		} catch (JSONException e) {
			// TODO To be handled
			e.printStackTrace();
		}

        Toast showName = Toast.makeText(getApplicationContext(), activeFriendName, Toast.LENGTH_SHORT);      
        showName.show();
        
        TextView txt = (TextView)findViewById(R.id.textView1);
        txt.setText(activeFriendName);
        //scorllDown((ScrollView)findViewById(R.id.scrollView1));
        //decodeTextToImages();
	}
	
	public void backButton(View v){
        LinearLayout formLayout = (LinearLayout)findViewById(R.id.linearLayout1);
        if(messageToSend.size() != 0){
        	formLayout.removeViewAt(formLayout.getChildCount() - 1 );
        	messageToSend.remove(messageToSend.size() - 1);
        }
	}	
	
	public void sendMessage(View v){
		if(messageToSend.size() > 0){
	        StringBuilder messageString = new StringBuilder();
	        for(String msg : messageToSend){
	        	messageString.append(msg);
	        }
	        VKRequest request = new VKRequest("messages.send", VKParameters.from("user_id", 
	        				String.valueOf(activeFriendId), "message", messageString.toString()));
			request.executeWithListener(messageSendListener);
		}
		
		//test for picture
		
		 //VKApi.uploadWallPhotoRequest(image, userId, groupId)
		 
		VKRequest request = new VKRequest("photos.getMessagesUploadServer");
		request.executeWithListener(messageSendListener);
	}
	
	public void recieveMessage(){
		VKRequest request = new VKRequest("messages.getHistory", VKParameters.from("user_id", activeFriendId, "count", 50));
		request.executeWithListener(messageRecieveListener);
	}
	
	public void onClick(View v) {

		LinearLayout piktogram = (LinearLayout)findViewById(R.id.linearLayout1);
		ImageView image = new ImageView(MessageActivity.this);
		switch (v.getId()) {

		case R.id.imageButton1:
			addImageToSendMessages("чувствовать");
	        image.setBackgroundResource(R.drawable.image_1_thumb);
	        piktogram.addView(image);
			break;

		case R.id.imageButton2:
			addImageToSendMessages("€");
	        image.setBackgroundResource(R.drawable.image_2_thumb);
	        piktogram.addView(image);
			break;

		case R.id.imageButton3:
			addImageToSendMessages("хорошо");
	        image.setBackgroundResource(R.drawable.image_3_thumb);
	        piktogram.addView(image);
			break;

		case R.id.imageButton4:
			addImageToSendMessages("чувствовать себ€");
	        image.setBackgroundResource(R.drawable.image_4_thumb);
	        piktogram.addView(image);
			break;
			
		default:
			break;
		}
	}
	
	private void addImageToSendMessages(String imageName){
		messageToSend.add(ICON_SPLIT_SYMBOLS + imageName + ICON_SPLIT_SYMBOLS);
	}

	private void scorllDown(final ScrollView view) {
		view.post(new Runnable() {
	        @Override
	        public void run() {
	        	view.fullScroll(ScrollView.FOCUS_DOWN);
	        }
	    });
	} 
	
	@Override
	protected void onResume() {
		super.onResume();
		recieveMessage();
	}
	
	private void removeMessageHistory(){
		LinearLayout historyLayout = (LinearLayout)findViewById(R.id.messageHistory);
		historyLayout.removeAllViews();
	}
	
	private void recieveMessagePeriodicly2() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				removeMessageHistory();
				recieveMessage();
			}
		}, 0, 2000);
	}
	
	private void recieveMessagePeriodicly() {
		final Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				removeMessageHistory();
				recieveMessage();
				handler.postDelayed(this, 5000);
			}
		};
		
		handler.post(r);
	}
	
	/*private void decodeTextToImages(List<String[]> messages){
	LinearLayout historyLayout = (LinearLayout)findViewById(R.id.messageHistory);
	String[] message = null;
	for(int i = messages.size() - 1; i >= 0; i--){
		message = messages.get(i);
		LinearLayout innerLayout = new LinearLayout(MessageActivity.this);
		historyLayout.addView(innerLayout);
		
		//tufta mas
		ImageView image = null;
		for(String word : message){
			image = new ImageView(MessageActivity.this); 
			if("чувствовать".equals(word)){
				image.setBackgroundResource(R.drawable.image_1_thumb);
			} else if("€".equals(word)){
				image.setBackgroundResource(R.drawable.image_2_thumb);
			} else if("хорошо".equals(word)){
				image.setBackgroundResource(R.drawable.image_3_thumb);
			} else if("чувствовать себ€".equals(word)){
				image.setBackgroundResource(R.drawable.image_4_thumb);
			}
			innerLayout.addView(image);
		}
		//
	}
}*/
	
}
