package com.spb.sezam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spb.sezam.utils.ActivityUtil;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.VKRequest.VKRequestListener;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    
    private List<JSONObject> users = new ArrayList<>();
    private UsersAdapter usersAdapter = null;
    private JSONObject testUser = null;
    private Map<String, String> usersUnreadMsCount = new HashMap<>();
	private int unReadDialogsCount = 0;
    
    private Runnable usersInfoRunnable = null;
    private Runnable checkUnreadMessagesRunnable = null;
	private final Handler handler = new Handler();

    private Button headerButton;
    
    private Menu menu;
    
    //-----------------------VK listeners-----------------------------//
    private VKRequestListener loadFriendsListener = new VKRequestListener() {

		@Override
		public void onComplete(VKResponse response) {
			try {
				updateUsers(response.json.getJSONObject("response").getJSONArray("items"));
				//we have adapter with users already
				//and in this line we have new users
				updateUnreadeMessagesCounts();
				//usersAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
				ActivityUtil.showError(getActivity(), "������ ��� ��������� ������ ������");
			}
		}

		@Override
		public void onError(VKError error) {
			ActivityUtil.showError(getActivity(), error);
		}
	};
	
	private VKRequestListener recieveDialogsListener = new VKRequestListener() {
		
		@Override
		public void onComplete(VKResponse response) {
			try {
				JSONArray messages = response.json.getJSONObject("response").getJSONArray("items");
				unReadDialogsCount = messages.length();
				
				JSONObject dialogInfo = null;
				JSONObject message = null;
				String unreadCount = null;
				String userId = null;
				usersUnreadMsCount.clear();
				
				for (int i = 0; i < unReadDialogsCount; i++) {
					dialogInfo = messages.getJSONObject(i);
					unreadCount =  dialogInfo.getString("unread");
					message = dialogInfo.getJSONObject("message");
					userId = message.getString("user_id");
					usersUnreadMsCount.put(userId, unreadCount);
				}
				
				updateUnreadeMessagesCounts();
				
				switch (messages.length()) {
				case 0:
					menu.getItem(0).setIcon(R.drawable.count_0);
					break;
				case 1:
					menu.getItem(0).setIcon(R.drawable.count_1);
					break;
				case 2:
					menu.getItem(0).setIcon(R.drawable.count_2);
					break;
				default:
					menu.getItem(0).setIcon(R.drawable.count_many);
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	};

	
    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
        
        //Verev@ ?????????
        ////////////////----------------
        
//        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,sex,bdate"));
//		
//		request.executeWithListener(loadFriendsListener);
        recieveUsersInfoPeriodicly();
        checkUnreadeMessagesPeriodicly();
    }
    
    private void recieveUsersInfoPeriodicly(){
    	usersInfoRunnable = new Runnable() {
			@Override
			public void run() {
				VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,sex,bdate"));
				request.executeWithListener(loadFriendsListener);
				handler.postDelayed(this, 1000*60*5); //every 5 minutes
				//adapter notify in listener
			}
		};
		handler.post(usersInfoRunnable);
    }
    
    /**
     * Changes users form JSON to list, adds test user in first position, and 
     * adds '0' as 'unread_count' for each user
     * @param usersJson Initial users
     * @throws JSONException
     */
    private void updateUsers(JSONArray usersJson) throws JSONException {
    	//in good way we need to run all over the list and find
    	//if there is different user, and change its values (especially unread_count)
    	users.clear();
    	
    	int count = usersJson.length();
    	addTestUser(users);
		for(int i = 1; i <= count; i++){
			users.add(usersJson.getJSONObject(i-1).put("unread_count", "0"));
		}
	}
    
    private void addTestUser(List<JSONObject> users) throws JSONException {
    	if(testUser == null){
			//create Sezam Bot for test messages
			testUser = new JSONObject();
			testUser.put("last_name", "����");
			testUser.put("first_name", "�����");
			testUser.put("id", "53759969"); //old profile ID
			testUser.put("online", "0");
			testUser.put("unread_count", "0");
    	}
    	users.add(testUser);
    }
    
    private void updateUnreadeMessagesCounts() throws JSONException{
    	String unreadCount = null;
    	for(JSONObject user :users){
    		unreadCount = usersUnreadMsCount.get(user.getString("id"));
    		if(unreadCount != null){
    			user.put("unread_count", unreadCount);
    		} else {
    			user.put("unread_count", "0");
    		}
    	}
    	usersAdapter.notifyDataSetChanged();
    }
    
    private void checkUnreadeMessages(){
		VKRequest request = new VKRequest("messages.getDialogs", VKParameters.from("unread", "1", "preview_length", "20"));
		request.executeWithListener(recieveDialogsListener);
	}
    
    private void checkUnreadeMessagesPeriodicly() {
		checkUnreadMessagesRunnable = new Runnable() {
			public void run() {
				checkUnreadeMessages();
				handler.postDelayed(this, 7000);
			}
		};
		
		unReadDialogsCount = 0;
		handler.postDelayed(checkUnreadMessagesRunnable, 500);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
        		R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        }); mDrawerListView.setActivated(false);
       /* Button b = new Button(MainActivity.this);
        b.setText("asd");*/
        setAdapterForListView(users);
        return mDrawerListView;
    }
    
    private void setAdapterForListView(List<JSONObject> friendsArr){
    	usersAdapter = new UsersAdapter(getActionBar().getThemedContext(), friendsArr);
    	mDrawerListView.setAdapter(usersAdapter);
        //mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    
    public void closeDrawer(){
    	if(isDrawerOpen()){
    		mDrawerLayout.closeDrawer(mFragmentContainerView);
    	}
    }
    
    public void openDrawer(){
    	if(!isDrawerOpen()){
    		mDrawerLayout.openDrawer(mFragmentContainerView);
    	}
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);

        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        //mnacel er sari smbul@
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                //getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                //getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        JSONObject user = null;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
            user = (JSONObject)mDrawerListView.getItemAtPosition(position);
        }
        
        
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(user);
            checkUnreadeMessagesPeriodicly();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	handler.removeCallbacks(usersInfoRunnable);
    	handler.removeCallbacks(checkUnreadMessagesRunnable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

   @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        /*if (mDrawerLayout != null && isDrawerOpen()) {
            //inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }*/
	   this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if(item.getItemId() == R.id.action_message ){
			openDrawer();
			return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(JSONObject user);
    }
}
