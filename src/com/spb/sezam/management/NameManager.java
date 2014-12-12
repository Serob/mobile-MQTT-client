package com.spb.sezam.management;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class NameManager {

	private static NameManager instance = null;
	
	private Map<String, String> filesEngKey = new HashMap<>();
	private Map<String, String> filesRuKey = new HashMap<>();
	private Map<String, String> groupsEngKey = new HashMap<>();
	private Map<String, String> groupsRuKey = new HashMap<>();
	
	public static String FILE_TAG = "file";
	public static String GROUP_TAG = "group";
	
	private NameManager(){
	}
	
	public static NameManager getInstance() {
		if(instance == null){
			instance = new NameManager();
		}
		return instance;
	}
	
	public void init(XmlPullParser parser){
		 try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG){
					addValuesToMaps(parser);
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException | IOException e) {
			//clearAllMaps();
			e.printStackTrace();
		} finally{
			if (parser instanceof XmlResourceParser){
				((XmlResourceParser) parser).close();
			}
		}
	}
	
	/**
	 * Adds element's attributes into the necessary {@link Map}s
	 * @param parser {@link XmlPullParser} element content
	 */
	private void addValuesToMaps(XmlPullParser parser){
		//0 - English name
		//1 - Russian name
		String valEng = null;
		String valRu = null;
		try{
			valEng = parser.getAttributeValue(0);
			valRu = parser.getAttributeValue(1);
			if(FILE_TAG.equals(parser.getName())){
				filesEngKey.put(valEng.trim(), valRu.trim());
				filesRuKey.put(valRu.trim(), valEng.trim());
			} else if(GROUP_TAG.equals(parser.getName())){
				groupsEngKey.put(valEng.trim(), valRu.trim());
				groupsRuKey.put(valRu.trim(), valEng.trim());
			}
		} catch (IndexOutOfBoundsException e) {
			//continue;
			Log.w("Empty value(s)", "Some of the values in XML tag '" + parser.getName() + "' are empty!");
		}
		
	}
	
	/**
	 * Returns Russian name of the <b>file</b>
	 * @param engName file's English name
	 * @return Russian name of the files by English name
	 */
	public String getFileRuName(String engName){
		if(engName != null){
			return filesEngKey.get(engName.trim());
		}
		return null;
	}
	
	/**
	 * Returns English name of the <b>file</b>
	 * @param ruName files's Russian name
	 * @return English name of the files by Russian name
	 */
	public String getFileEngName(String ruName){
		if(ruName != null){
			return filesRuKey.get(ruName.trim());
		} 
		return null;
	}
	
	/**
	 * Returns Russian name of the <b>group</b>
	 * @param engName group's English name
	 * @return Russian name of the group by English name
	 */
	public String getGroupRuName(String engName) {
		if (engName != null) {
			return groupsEngKey.get(engName.trim());
		}
		return null;
	}
	
	/**
	 * Returns English name of the <b>group</b>
	 * @param ruName group's Russian name
	 * @return English name of the group by Russian name
	 */
	public String getGroupEngName(String ruName){
		if(ruName != null){
			return groupsRuKey.get(ruName.trim());
		}
		return null;
	}
	
	private void clearAllMaps(){
		filesEngKey.clear();
		filesRuKey.clear();
		groupsEngKey.clear();
		groupsRuKey.clear();
	}
}
