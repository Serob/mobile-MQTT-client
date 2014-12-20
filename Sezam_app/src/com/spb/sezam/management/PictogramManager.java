package com.spb.sezam.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.spb.sezam.MessageActivity;
import com.spb.sezam.R;

public class PictogramManager {
	
	private static PictogramManager instance = null;
	public static final String FOLDER_NAME = "test";
	
	private List<Pictogram> linearPicotgrams = new ArrayList<>();
	private List<Pictogram> treePictograms = new ArrayList<>();
	
	private Context ctx;
	
	private PictogramManager(){
	}
	
	public static PictogramManager getInstance() {
		if(instance == null){
			instance = new PictogramManager();
		}
		return instance;
	}
	
	public PictogramManager init(Context ctx){
		this.ctx = ctx;
		AssetManager am = ctx.getAssets();
		try {
			collectPictograms(am);
		} catch (IOException e) {
			Log.e("I/O Error in Assets", e.getMessage());
			e.printStackTrace();
		}
		return instance;
	}
	
	private void collectPictograms(AssetManager am) throws IOException{
		//Only folders in first level
		for(String name : am.list(FOLDER_NAME)){
			
			/*if(!name.matches(".*?[\\.jpg|\\.png]")){
				GroupPictogram pGroup = new GroupPictogram(name);
				treePictograms.add(pGroup);
				linearPicotgrams.add(pGroup);
				collectPictograms(pGroup, am);
			}*/

			//for test
			BitmapDrawable bd = new BitmapDrawable(ctx.getResources(), am.open(FOLDER_NAME + File.separator + name));
			Pictogram p = new Pictogram(name);
			p.setIcon(bd);
			linearPicotgrams.add(p);
			
		}
	}
	
	private void collectPictograms(GroupPictogram pGroup, AssetManager am) throws IOException{
		String path = pGroup.getPath();
		String fullFolderPath = FOLDER_NAME + File.separator + path;
		for(String name : am.list(fullFolderPath)){
			//assumes that we have only image files
			//and no directory ends with .jpg or .png
			//(because of am.list() is slow, otherwise we can call .list for every file)
			if(name.matches(".*?[\\.jpg|\\.png]")){
				//maybe here
				BitmapDrawable bd = new BitmapDrawable(ctx.getResources(), am.open(fullFolderPath + File.separator + name));
				Pictogram pictogram= new Pictogram(path + File.separator + name);
				pictogram.setIcon(bd);
				pGroup.addInnerPictogram(pictogram);
			} else {
				GroupPictogram nestedGroup = new GroupPictogram(path + File.separator + name);
				pGroup.addInnerPictogram(nestedGroup);
				linearPicotgrams.add(nestedGroup);
				collectPictograms(nestedGroup, am);
			}
		}
	}

	public List<Pictogram> getLinearPicotgrams() {
		return linearPicotgrams;
	}

	public List<Pictogram> getTreePictograms() {
		return treePictograms;
	}
	
	
}
