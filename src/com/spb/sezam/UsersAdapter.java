package com.spb.sezam;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UsersAdapter extends ArrayAdapter<JSONObject> {

	private final Context context;
	private final JSONObject[] users;
	
	static class ViewHolder {
		public TextView text;
		public ImageView onlineIcon;
		public ImageView hasMessageIcon;
	}
	
	public UsersAdapter(Context context, JSONObject[] users) {
		super(context, R.layout.row_layout, users);
		this.context = context;
		this.users = users;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;

		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater)context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.row_layout, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.user_label);
			viewHolder.onlineIcon = (ImageView) rowView
					.findViewById(R.id.online_status);
			viewHolder.hasMessageIcon = (ImageView) rowView
					.findViewById(R.id.has_unread);
			rowView.setTag(viewHolder);
		}

		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		JSONObject user = users[position];
		String username = "";
		int onlineStatus = 0;
		try {
			username = user.getString("first_name") + " "+ user.getString("last_name");
			onlineStatus = user.getInt("online");
			// maybe maybe unreadmessage here
		} catch (JSONException e) {
			e.printStackTrace();
		}

		holder.text.setText(username);
		if (onlineStatus == 1) {
			holder.onlineIcon.setImageResource(R.drawable.online);
		} else {
			holder.onlineIcon.setImageResource(R.drawable.ofline);
		}

		return rowView;
	}

	
}
