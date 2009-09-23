package rath.toys.utils.dic;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author rath
 * @version 1.0, $Id$
 */
public class NaverDictionaryCrawler {
	
	private String query;
	private DictionaryCategory category;
	
	public NaverDictionaryCrawler() {
		
	}
	
	public String requestPage() throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet();
//		get.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; ko; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3");
//		get.addHeader("Referer", "http://eedic.naver.com/eedic.naver?mode=word&id=28906");

		String result = null;
		try {
			String page = getPageContent(client, String.format(category.getEntryURL(), category.getEncodedQuery(this.query)));			
			result = getResult(client, page);
			
		} finally {
			get.abort();
		}
		
		return result;
	}
	
	private String getPageContent( HttpClient client, String url ) throws IOException {
		HttpGet get = new HttpGet(url);
		HttpResponse res = client.execute(get);
		int code = res.getStatusLine().getStatusCode();
		try {
			if( code==200 ) {
				HttpEntity entity = res.getEntity();
				String page = EntityUtils.toString(entity, "EUC-KR");
				get.abort();
				return page;
			}
			if( code==302 ) {
				String location = res.getHeaders("Location")[0].getValue();
				get.abort();
				return getPageContent(client, location);
			}
		} finally {
			get.abort();
		}
		throw new IOException(code + " error occurs. something went wrong.");
	}
	
	private String getHiddenEntrySource( String page ) {
		Pattern p = Pattern.compile("<input type=\"hidden\" name=\"source_contents\" id=\"entryBody\" value=\"([^\"]+?)\"");
		Matcher m = p.matcher(page);
		if( m.find() ) {
			String source = m.group(1);
			return replaceHtmlEntities(source);
		}
		else 
		{
			System.out.println(page);
		}
		return null;
	}
	
	protected String getResult( HttpClient client, String page ) throws IOException {
		String result = null;
		
		if( category.getId().equals("eedic") ) {
			// english-english dictionary
			result = getHiddenEntrySource(page);
	
			if( result!=null ) {
				Pattern pt = Pattern.compile("<strong>Thesaurus</strong>.*?</span>\\)<br>(.*?)</span>", Pattern.DOTALL);
				Matcher mt = pt.matcher(page);
				if( mt.find() ) {
					result += "\n[Thesaurus]\n\n" + replaceHtmlEntities(mt.group(1)).trim();
				}
			} 
		}
		if( category.getId().equals("ekdic") ) {
			Pattern p = Pattern.compile("<td style=\"table-layout:fixed.*?href=\".*?[0-9]+\\?([^\"]*?)\".*?<strong>.+?</strong>");
			Matcher m = p.matcher(page);
			if( m.find() ) {
				String target = m.group(1);
				page = getPageContent(client, target);
			}
			result = getHiddenEntrySource(page);
		}
		if( category.getId().equals("kedic") ) {
			Pattern p = Pattern.compile("<td style=\"table-layout:fixed.*?href=\".*?[0-9]+\\?([^\"]*?)\".*?<strong>.+?</strong>");
			Matcher m = p.matcher(page);
			if( m.find() ) {
				String target = m.group(1);
				page = getPageContent(client, target);
			}
			result = getHiddenEntrySource(page);
		}
		
		if( result==null ) {
			result = "Not found!\nOr, Page layout of naver dictionary was modified.";
		}
		return result;
	}
	
	private String replaceHtmlEntities( String source )
	{
		source = source.replace("&quot;", "\"");
		source = source.replace("&lt;", "<");
		source = source.replace("&gt;", ">");
		source = source.replace("&amp;", "&");
		
		source = source.replace("&nbsp;", " ");
		source = source.replace("<br>", "\n");
	
		// english-english
		source = source.replaceAll("<img src=\"http://sstatic\\.naver\\.com/eedic/pron2/([a-z_]*\\.gif)\" align=\"absbottom\">", "{{$1}}");
		// english-korean
		System.out.println(source);
		source = source.replaceAll("<img src=\"http://sstatic\\.naver\\.com/endic/2005/images/font/syn/([0-9a-z_]*\\.gif)\" align=\"?absmiddle\"?>", "{{$1}}");
		source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("\\t", "");
		source = source.replaceAll("\\n\\n(\\n)+", "\n\n");

		return source;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
	
	public void setCategory(DictionaryCategory dictionaryCategory) {
		this.category = dictionaryCategory;
	}
	
	public DictionaryCategory getCategory() {
		return this.category;
	}
	
	public static void main( String[] args ) throws Exception
	{
		NaverDictionaryCrawler c = new NaverDictionaryCrawler();
		c.setCategory(new DictionaryCategory("eedic", "네이버 영영사전", "http://eedic.naver.com/search.nhn?query_euckr=&dic_where=eedic&mode=all&query=%s&x=0&y=0"));
		c.setQuery("industry");
		System.out.println( c.requestPage() );
	}

}
