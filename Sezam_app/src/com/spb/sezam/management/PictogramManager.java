package com.spb.sezam.management;

public class PictogramManager {
	
	private static PictogramManager instance = null;
	
	private PictogramManager(){
	}
	
	public static PictogramManager getInstance() {
		if(instance == null){
			instance = new PictogramManager();
		}
		return instance;
	}
}
