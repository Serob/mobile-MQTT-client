package com.spb.sezam.management;

import java.util.ArrayList;
import java.util.List;

public class PictogramGroup extends Pictogram {

	List<Pictogram> innerPictograms = new ArrayList<>();
	
	public PictogramGroup(String path) {
		super(path);
	}

	@Override
	public ElementType getType() {
		return ElementType.GROUP;
	}
	
	/**
	 * @param pictogram Can be pictogram or group
	 */
	public void addInnerPictogram(Pictogram pictogram){
		innerPictograms.add(pictogram);
	}
	
	/**
	 * 
	 * @return {@link List} Which can contain Pictograms and/or PictogramGroups
	 */
	public List<Pictogram> getInnerPictoGrPictograms(){
		return innerPictograms;
	}

}
