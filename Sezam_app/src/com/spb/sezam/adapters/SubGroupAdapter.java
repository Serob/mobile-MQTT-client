package com.spb.sezam.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.spb.sezam.R;
import com.spb.sezam.management.GroupPictogram;
import com.spb.sezam.management.NameManager;
import com.spb.sezam.management.Pictogram;

public class SubGroupAdapter extends BaseAdapter{

	private final Context context;
	private List<Pictogram> subGroups = new ArrayList<>();
	
	static class ViewHolder {
		public Button subGroupItem;
	}
	
	//but list content should have GroupPictogram type
	public SubGroupAdapter(Context context, List<Pictogram> subGroups){
		this.context = context;
		this.subGroups = subGroups;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View elementView = convertView;
		
		// reuse views
		if(elementView == null){
			LayoutInflater inflater = (LayoutInflater)context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			elementView = inflater.inflate(R.layout.subgroup_layout, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.subGroupItem  = (Button)elementView.findViewById(R.id.subGroupItem);
			elementView.setTag(viewHolder);
		}
		
		// fill data
		ViewHolder holder = (ViewHolder) elementView.getTag();
		GroupPictogram group = getItem(position);
		String ruName = NameManager.getInstance().getGroupRuName(group.getPath());
		holder.subGroupItem.setText(ruName);
		
		return elementView;
	}
	
	public void updateView(List<Pictogram> subGroups) {
        this.subGroups = subGroups;
        notifyDataSetChanged();
    }
	
	@Override
	public int getCount() {
		return subGroups.size();
	}

	@Override
	public GroupPictogram getItem(int position) {
		return (GroupPictogram)subGroups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	
}