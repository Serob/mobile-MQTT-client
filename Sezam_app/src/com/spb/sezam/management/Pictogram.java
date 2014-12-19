package com.spb.sezam.management;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pictogram {
	
	/** Full path of the file/folder (inside assets)*/
	private String path;
	
	public Pictogram(String path){
		this.path = path;
	}

	public ElementType getType() {
		return ElementType.FILE;
	}

	/** Returns full path of the file/folder (inside assets)
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 *  Returns only file/folder name (without path information) 
	 */
	public String getFileName(){
		Pattern afterLastSlash = Pattern.compile("[^\\" + File.separator + "]([^\\" +  File.separator + "]*)$");
		Matcher m = afterLastSlash.matcher(path);
	    if(m.find()){
			return m.group(0);
	    }
	    
	    return null;
	}
	
	/**
	 *  Name as will be seen in the app(without '.jpg', path and other information) 
	 */
	public String getName(){
		String[] texts = path.split(".");
		int size = texts.length;
		if(ElementType.FILE == getType()){
			//assume size > 1
			return texts[size-2];
		} else {
			//assume that there is a dot in a folder's name
			return texts[size-1];
		}
		
		/*Armen's Varant
		Pattern regex = Pattern.compile(".*\\.(.*?)\\..*?$");
		Matcher regexMatcher = regex.matcher(path);
		return regexMatcher.replaceAll("$1");*/
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pictogram))
			return false;
		if (obj == this)
			return true;
		//let throw null pointer to check
		return path.equals(((Pictogram)obj).path);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}
	
}
