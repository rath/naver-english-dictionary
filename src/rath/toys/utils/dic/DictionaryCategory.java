package rath.toys.utils.dic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 
 * @author rath
 * @version 1.0
 */
public class DictionaryCategory {
	private String id;
	private String frameTitle;
	private String entryURL;
	
	public DictionaryCategory( String id, String title, String entryURL ) {
		setId(id);
		setFrameTitle(title);
		setEntryURL(entryURL);
	}
	
	public String getFrameTitle() {
		return frameTitle;
	}
	public void setFrameTitle(String frameTitle) {
		this.frameTitle = frameTitle;
	}
	public void setEntryURL(String entryURL) {
		this.entryURL = entryURL;
	}
	public String getEntryURL() {
		return entryURL;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public String getEncodedQuery( String query ) {
		if( id.equals("kedic") ) {
			try {
				return URLEncoder.encode(query, "EUC-KR");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return query;
	}
}
