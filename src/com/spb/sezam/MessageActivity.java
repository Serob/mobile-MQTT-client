package com.spb.sezam;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spb.sezam.utils.ActivityUtil;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MessageActivity extends Activity {

	public static final String ICON_SPLIT_SYMBOLS = "|_";
	
	private static final int MESSAGE_RECIEVE_COUNT = 7;
	
	private List<String> messageToSend = new ArrayList<String>();
	private JSONArray allMessages = new JSONArray();
	private String activeFriendName = null;
	private int activeFriendId;
	
	private Runnable recieveMessagesRunnable = null;
	private final Handler recieveMessagesHandler = new Handler();
	
	private VKRequestListener messageSendListener  = new VKRequestListener(){

		@Override
		public void onComplete(VKResponse response) {
			LinearLayout formLayout = (LinearLayout) findViewById(R.id.linearLayout1);
			formLayout.removeAllViews();
			messageToSend.clear();
			Toast showSent = Toast.makeText(getApplicationContext(), "Сообщение отправлено", Toast.LENGTH_SHORT);
			showSent.show();
			recieveMessageHistory(MESSAGE_RECIEVE_COUNT);
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
	        	
	            //must be only when Activity starts
	        	
	        	JSONArray newMessages = findNewMessages(allMessages, messages);
	        	if(newMessages.length() != 0){
		            showHistory(newMessages);
		            scorllDown((ScrollView)findViewById(R.id.scrollView1));
		            if(isThereRecieved(newMessages) && newMessages != messages){
		            	Toast showSent = Toast.makeText(getApplicationContext(), "Получено новое сообщение", Toast.LENGTH_SHORT);
		    			showSent.show();
		            }
	        	}
	            allMessages = messages;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		//createButtonsFromAssets();
		createButtonsFromDrawables();
		
		/*ImageButton btn1 = (ImageButton)findViewById(R.id.imageButton1);
        btn1.setOnClickListener(this);

        ImageButton btn2 = (ImageButton)findViewById(R.id.imageButton2);
        btn2.setOnClickListener(this);

        ImageButton btn3 = (ImageButton)findViewById(R.id.imageButton3);
        btn3.setOnClickListener(this);

        ImageButton btn4 = (ImageButton)findViewById(R.id.imageButton4);
        btn4.setOnClickListener(this);*/

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
        
        /*TextView txt = (TextView)findViewById(R.id.textView1);
        txt.setText(activeFriendName);*/
        //scorllDown((ScrollView)findViewById(R.id.scrollView1));
        //decodeTextToImages();
        
        //up button for actionbar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(activeFriendName);
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#aaaaaa")));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_exit:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
		case R.id.action_email:
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"annainternest@gmail.com"});
			i.putExtra(Intent.EXTRA_SUBJECT, "Письмо администратору");
			i.putExtra(Intent.EXTRA_TEXT   , "\nОтправлено с приложения Sezam");
			try {
			    startActivity(Intent.createChooser(i, "Send mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
			    Toast.makeText(MessageActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
		
	
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
	        switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	        	Log.e("token=", VKSdk.getAccessToken().userId);
	        	VKSdk.logout();
	        	if(recieveMessagesHandler != null){
	    			recieveMessagesHandler.removeCallbacks(recieveMessagesRunnable);
	    		}
	        	startActivity(VKActivity.class);
	        	setContentView(R.layout.activity_vk);

				// ------------------------------

				Button b = (Button) findViewById(R.id.sign_in_button);
				// predefined in .xml as Войти
				if (VKSdk.wakeUpSession()) {
					Log.e("wakeUp", "wakeUp");
					startActivity(FriendsActivity.class);
					// skzbi hamar shat el a
					b.setText("Выход!");
					b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							VKSdk.logout();
							((Button) view).setText("Войти");
						}
					});
					//
					return;
				}

				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (VKSdk.isLoggedIn()) {
							Log.e("Uje logged in", "Uje Loged in");
						}
						 String[] myScope = new String[] {
					         VKScope.FRIENDS,
					         VKScope.MESSAGES,
					         VKScope.OFFLINE
						 };						
						VKSdk.authorize(myScope);
						if (VKSdk.isLoggedIn()) {
							Log.e("Uje logged in2", "Uje Loged in2");
						}
					}
				});
				// ------------------------------
	            
	            
	            //Yes button clicked
	            break;

	        case DialogInterface.BUTTON_NEGATIVE:
	            //No button clicked
	            break;
	        }
	    }
	};	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_message_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	public void showHistory(JSONArray messages) throws JSONException{
		String messageString = null;
		JSONObject messageJson = null;
		String[] messageArr = null;
		LinearLayout historyLayout = (LinearLayout)findViewById(R.id.messageHistory);
		
		for (int i = messages.length() - 1; i >= 0; i--) {
			messageJson = messages.getJSONObject(i);
			TextView nameView = new TextView(MessageActivity.this);
			if(messageJson.getInt("out") == 1){
				nameView.setText("Я");
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
		
		int bgResourceId;
		try {
			bgResourceId = R.drawable.class.getField(text).getInt(null);
			image.setBackgroundResource(bgResourceId);
			lLayout.addView(image);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			showTextAsString(text, lLayout);
		}
		
		/*if("чувствовать".equals(text)){
			image.setBackgroundResource(R.drawable.image_1_thumb);
		} else if("я".equals(text)){
			image.setBackgroundResource(R.drawable.image_2_thumb);
		} else if("хорошо".equals(text)){
			image.setBackgroundResource(R.drawable.image_3_thumb);
		} else if("чувствовать себя".equals(text)){
			image.setBackgroundResource(R.drawable.image_4_thumb);
		//none of words
		} else{
			showTextAsString(text, lLayout);
			return;
		}
		
		//if icon name is found
		lLayout.addView(image);*/
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		VKUIHelper.onResume(this);
		
		//first time call with more messages
		recieveMessageHistory(70);
		//then as written in recieveMessagePeriodicly
		recieveMessagePeriodicly();
		
		//permission test
//		VKRequest request = new VKRequest("account.getAppPermissions");
//		request.executeWithListener(messageSendListener);
		
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
	        long guId = new Date().getTime();
	        VKRequest request = new VKRequest("messages.send", VKParameters.from(
		        		"user_id", String.valueOf(activeFriendId), 
		        		"message", messageString.toString(), "guid", guId));
			request.executeWithListener(messageSendListener);
		}
		
		//test for picture
		
		 //VKApi.uploadWallPhotoRequest(image, userId, groupId)
		 
		VKRequest request = new VKRequest("photos.getMessagesUploadServer");
		request.executeWithListener(messageSendListener);
	}
	
	//messageCount must be used in real later(the idea is to load more messages at first, then less)
	public void recieveMessageHistory(int messagesCount){
		if(messagesCount > 0){
			VKRequest request = new VKRequest("messages.getHistory", VKParameters.from("user_id", activeFriendId, "count", messagesCount));
			request.executeWithListener(messageRecieveListener);
		}
	}
	
	private void addImageNameToSendMessages(String imageName){
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
	protected void onPause() {
		super.onPause();
		if(recieveMessagesHandler != null){
			recieveMessagesHandler.removeCallbacks(recieveMessagesRunnable);
		}
	}

	private void removeMessageHistory(){
		LinearLayout historyLayout = (LinearLayout)findViewById(R.id.messageHistory);
		historyLayout.removeAllViews();
	}
	
	private void recieveMessagePeriodicly() {
		recieveMessagesRunnable = new Runnable() {
			public void run() {
				recieveMessageHistory(MESSAGE_RECIEVE_COUNT);
				recieveMessagesHandler.postDelayed(this, 7000);
			}
		};
		
		recieveMessagesHandler.postDelayed(recieveMessagesRunnable, 7000);
	}
	
	public JSONArray findNewMessages(JSONArray oldList, JSONArray newList) throws JSONException{
		if(newList == null || newList.length() == 0){
			return new JSONArray();
		}
		if(oldList == null || oldList.length() == 0){
			return newList;
		}
		
		//need to find oldList[0] in newList
		JSONArray onlyNew = new JSONArray();
		for(int i = 0; i < newList.length(); i++){
			//we believe in VK API that every message should have its unique id...
			JSONObject messageInNew = newList.getJSONObject(i);
			if(messageInNew.getInt("id") == oldList.getJSONObject(0).getInt("id")){
				break;
			} else {
				onlyNew.put(messageInNew);
			}
		}
		return onlyNew;
	}
	
	/**
	 * Returns {@code true} if there is any received message in the list, otherwise returns {@code false} 
	 * @param messages The list of messages to be check
	 * @return {@code boolean}
	 * @throws JSONException
	 */
	private boolean isThereRecieved(JSONArray messages) throws JSONException{
		JSONObject message = null;
		for (int i = 0; i < messages.length(); i++) {
			message = messages.getJSONObject(i);
			if(message.getInt("out") == 0){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Uses JAVA reflection
	 */
	private void createButtonsFromDrawables(){
		//using reflection
		LinearLayout lLayout = (LinearLayout) findViewById(R.id.linearLayout2);
		Field[] drawableFields = R.drawable.class.getFields();
		int resId;
		for(Field field : drawableFields){
			String fieldName = field.getName();
			if(fieldName.startsWith("image_") && !fieldName.endsWith("thumb")){
				try {
					resId = field.getInt(null);
					ImageButton btn = new ImageButton(MessageActivity.this);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					params.leftMargin = 0;
					
					btn.setImageResource(resId);
					btn.setContentDescription(fieldName);
					btn.setLayoutParams(params);
					
					btn.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	LinearLayout piktogram = (LinearLayout)findViewById(R.id.linearLayout1);
			        		ImageView image = new ImageView(MessageActivity.this);
			        		
			        		String bgResourceName = view.getContentDescription() + "_thumb";
			        		addImageNameToSendMessages(bgResourceName);
			        		try {
								int bgResourceId = R.drawable.class.getField(bgResourceName).getInt(null);
								image.setBackgroundResource(bgResourceId);
			        		} catch (IllegalAccessException
									| IllegalArgumentException
									| NoSuchFieldException e) {
								e.printStackTrace();
							}
			    	        
			    	        piktogram.addView(image);
			            }
			        });
					
					lLayout.addView(btn);
					
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		
	}
	
	/**
	 * Uses assets
	 */
	private void createButtonsFromAssets(){
		AssetManager am = getAssets();
		LinearLayout lLayout = (LinearLayout) findViewById(R.id.linearLayout2);
		try {
			for(String name : am.list("test")){
				ImageButton btn = new ImageButton(MessageActivity.this);
				//Bitmap a = BitmapFactory.decodeStream(am.open(name));
				BitmapDrawable bd = new BitmapDrawable(getResources(), am.open("test" + File.separator + name));
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.leftMargin = 0;
				
				
				//btn.setBackground(bd);
				//btn.setImageDrawable(bd);
				btn.setContentDescription(name);
				btn.setLayoutParams(params);
				lLayout.addView(btn);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		VKUIHelper.onDestroy(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
	}

	private void startActivity(Class<? extends Activity> a) {
		Intent startNewActivityOpen = new Intent(this, a);
		startActivityForResult(startNewActivityOpen, 0);
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
			} else if("я".equals(word)){
				image.setBackgroundResource(R.drawable.image_2_thumb);
			} else if("хорошо".equals(word)){
				image.setBackgroundResource(R.drawable.image_3_thumb);
			} else if("чувствовать себя".equals(word)){
				image.setBackgroundResource(R.drawable.image_4_thumb);
			}
			innerLayout.addView(image);
		}
		//
	}
}*/
	
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
	
}
