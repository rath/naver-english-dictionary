package rath.toys.utils.dic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 * 
 * @author rath
 * @version 1.0, $Id$
 */
@SuppressWarnings("serial")
public class NaverDictionary extends JPanel {
	
	private JFrame frame;
	private JTextPane resultBox;
	private JTextField searchField;
	
	private ExecutorService threadPool;
	private JButton actionButton;
	private JProgressBar progressBar;
	
	private Font font = new Font("Dialog", Font.PLAIN, 12);
	private JScrollPane resultScroll;
	
	private List<DictionaryCategory> dictCategory = new ArrayList<DictionaryCategory>();
	private int currentCategory = 0;
	
	private Map<String, Icon> iconMap = new HashMap<String, Icon>();
	
	private LinkedList<SearchEntry> history = new LinkedList<SearchEntry>();
	private int currentHistory = -1;
	
	public NaverDictionary(JFrame f)
	{
		this.frame = f;
		init();
		threadPool = Executors.newFixedThreadPool(1);
		
		installProviders();
	}
	
	static class SearchEntry {
		String categoryId;
		String query;
		String result;
		
		SearchEntry( String id, String query, String result ) {
			this.categoryId = id;
			this.query = query;
			this.result = result;
		}
	}
	
	private DictionaryCategory findCategoryAsId( String id ) {
		for(DictionaryCategory dc : dictCategory) {
			if( dc.getId().equals(id) ) 
				return dc;
		}
		return null;
	}

	private void installProviders() {
		dictCategory.add(new DictionaryCategory("eedic", "네이버 영영사전", "http://eedic.naver.com/search.nhn?query_euckr=&dic_where=eedic&mode=all&query=%s&x=0&y=0"));
		dictCategory.add(new DictionaryCategory("ekdic", "네이버 영한사전", "http://endic.naver.com/search.nhn?query_utf=%s&x=0&y=0&kind=keyword"));
		dictCategory.add(new DictionaryCategory("kedic", "네이버 한영사전", "http://endic.naver.com/search.nhn?query_euckr=&dic_where=endic&mode=all&kind=keyword&query=%s&x=0&y=0"));
	
		
		String iconList = 
			"d0c0.gif	d0d9.gif	e0e3.gif	e0f7.gif	e1ae.gif	e2ea.gif	ge.gif " +
			"d0c4.gif	d0da.gif	e0e4.gif	e0f8.gif	e1af.gif	e2eb.gif	ge_u.gif " +
			"d0c5.gif	d0dc.gif	e0e5.gif	e0f9.gif	e1b0.gif	e2ec.gif	ge_up.gif " +
			"d0c6.gif	e0d0.gif	e0e6.gif	e0fa.gif	e1b1.gif	e2ed.gif	go_u.gif " +
			"d0c7.gif	e0d2.gif	e0e7.gif	e0fb.gif	e1b2.gif	e2ee.gif	gu_u.gif " +
			"d0c8.gif	e0d4.gif	e0e8.gif	e0fc.gif	e1b3.gif	e2ef.gif	ia_u.gif " +
			"d0ca.gif	e0d5.gif	e0e9.gif	e0fd.gif	e1b4.gif	e2f0.gif	id.gif " +
			"d0cb.gif	e0d6.gif	e0ea.gif	e0fe.gif	e1b5.gif	e2f1.gif	ii.gif " +
			"d0cc.gif	e0d7.gif	e0eb.gif	e1a1.gif	e1b6.gif	e2f2.gif	ii_em.gif " +
			"d0cd.gif	e0d8.gif	e0ec.gif	e1a2.gif	e1b7.gif	e2f3.gif	ii_u.gif " +
			"d0ce.gif	e0d9.gif	e0ed.gif	e1a3.gif	e1b8.gif	e2f5.gif	in.gif " +
			"d0cf.gif	e0da.gif	e0ee.gif	e1a5.gif	e1b9.gif	e2f6.gif	io_u.gif " +
			"d0d0.gif	e0db.gif	e0ef.gif	e1a6.gif	e1ba.gif	e2f7.gif	it.gif " +
			"d0d1.gif	e0dc.gif	e0f0.gif	e1a7.gif	e1bb.gif	e2f8.gif	iu_em.gif " +
			"d0d2.gif	e0dd.gif	e0f1.gif	e1a8.gif	e1bc.gif	e2fb.gif	iu_u.gif " +
			"d0d3.gif	e0de.gif	e0f2.gif	e1a9.gif	e1bf.gif	e2fc.gif	iz.gif " +
			"d0d4.gif	e0df.gif	e0f3.gif	e1aa.gif	e2e5.gif	e2fd.gif	r_up.gif " +
			"d0d5.gif	e0e0.gif	e0f4.gif	e1ab.gif	e2e7.gif	e2fe.gif	tt.gif " +
			"d0d6.gif	e0e1.gif	e0f5.gif	e1ac.gif	e2e8.gif	ga.gif		tt_u.gif " +
			"d0d7.gif	e0e2.gif	e0f6.gif	e1ad.gif	e2e9.gif	ga_u.gif	is.gif ";


		String[] icons = iconList.split("\\s+");
		for(String i : icons) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(getResourceBytes(i));
				iconMap.put(i, icon);
			} catch (IOException e) {
			}
		}
	}
	
	private byte[] getResourceBytes( String resourceName ) throws IOException {
		InputStream in = getClass().getResourceAsStream("resources/" + resourceName);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			if( in==null ) {
				in = new FileInputStream("src/rath/toys/utils/dic/resources/" + resourceName);
			}
			while(true) {
				int value = in.read();
				if( value==-1 )
					break;
				bos.write(value);
			}
		} finally {
			in.close();
		}
		return bos.toByteArray();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		add(createSearchBox(), "North");
		add(createResultBox(), "Center");
		add(createProgressBar(), "South");
	}

	private Component createProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setMaximum(50);
		progressBar.setValue(0);
		progressBar.setIndeterminate(false);
		return progressBar;
	}

	private Component createResultBox() {
		resultBox = new JTextPane();
//		resultBox.setLineWrap(true);
		resultBox.setAutoscrolls(false);
//		resultBox.setWrapStyleWord(true);
		resultBox.setFont(font);
		resultBox.setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5) );
		
		resultBox.addKeyListener(new KeyAdapter() {
			public void keyReleased( KeyEvent e ) {
				if( e.getModifiers()==4 && e.getKeyChar()=='s' ) {
					String selected = resultBox.getSelectedText();
					// Ignore when a selected text includes newline.
					if( selected.indexOf('\n')!=-1 ) 
						return;
					searchField.setText(selected.trim());
					doSearch();
				}
			}
		});
		
		resultScroll = new JScrollPane(resultBox);
		resultScroll.setBorder( BorderFactory.createEtchedBorder() );
		return resultScroll;
	}

	private Component createSearchBox() {
		JPanel p = new JPanel(new BorderLayout(4, 4));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel findLabel = new JLabel("Find");
		p.add(findLabel, "West");
		searchField = new JTextField("");
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				switch(keyCode) {
				case KeyEvent.VK_UP:
					if( currentCategory==0 ) 
						currentCategory = dictCategory.size() - 1;
					else 
						currentCategory--;
					frame.setTitle(dictCategory.get(currentCategory).getFrameTitle());
					
					break;
				case KeyEvent.VK_DOWN:
					if( currentCategory==dictCategory.size()-1 ) 
						currentCategory = 0;
					else 
						currentCategory++;
					frame.setTitle(dictCategory.get(currentCategory).getFrameTitle());
					break;
				}
				
				if( e.getModifiers()==4 && currentHistory!=-1 ) {
					switch(keyCode) {
					case KeyEvent.VK_LEFT:
						if( currentHistory==0 )
							return;
						currentHistory--;
						showCurrentHistory();
						break;
					case KeyEvent.VK_RIGHT:
						if( currentHistory>=history.size()-1)
							return;
						currentHistory++;
						showCurrentHistory();
						break;
					}
				}
			}
		});
		ActionListener searchAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSearch();
			}
		};
		searchField.addActionListener(searchAction);
		p.add(searchField, "Center");
		actionButton = new JButton("Search");
		actionButton.addActionListener(searchAction);
		p.add(actionButton, "East");
		
		return p;
	}
	
	protected void showCurrentHistory() {
		SearchEntry entry = history.get(currentHistory);
		DictionaryCategory category = findCategoryAsId(entry.categoryId);
		frame.setTitle(category.getFrameTitle());
		searchField.setText(entry.query);
		showResult(null, entry.result);
	}

	private void lockFields( boolean lock ) {
		searchField.setEnabled(!lock);
		actionButton.setEnabled(!lock);
		progressBar.setIndeterminate(lock);
	}

	protected void doSearch() {
		final String query = searchField.getText().trim();
		if( query.length()==0 ) {
			JOptionPane.showMessageDialog(this, "Please enter the keyword in the search box", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		lockFields(true);
		
		threadPool.execute(new Runnable() {
			public void run() {
				final NaverDictionaryCrawler crawl = new NaverDictionaryCrawler();
				crawl.setCategory(dictCategory.get(currentCategory));
				crawl.setQuery(query);
				try {
					final String page = crawl.requestPage();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showResult(crawl, page);							
						}
					});
				} catch (IOException e) {
					JOptionPane.showMessageDialog(NaverDictionary.this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} finally {
					lockFields(false);
					
				}
			}
		});
		
	}

	/**
	 * 
	 * @param crawl If not null, this is live search. Or, it's just a navigate in history.
	 * @param page
	 */
	protected void showResult(NaverDictionaryCrawler crawl, String page) {
//		System.out.println(page);
		resultBox.setText("");
		StyledDocument doc = (StyledDocument)this.resultBox.getDocument();
		
		Pattern p = Pattern.compile("\\{\\{([0-9a-z_]+\\.gif)\\}\\}");
		Matcher m = p.matcher(page);
		int offset = 0;
		while( m.find() ) {
			String prefix = page.substring(offset, m.start());
			try {
				doc.insertString(doc.getLength(), prefix, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			Icon icon = this.iconMap.get(m.group(1));
//			System.out.println("icon: " + icon + ", " + m.group(1));
			if( icon!=null ) { 
				resultBox.insertIcon(icon);
			}
			offset = m.end();
		}
		try {
			doc.insertString(doc.getLength(), page.substring(offset), null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
				
//		this.resultBox.setText(page);
		resultBox.setCaretPosition(0);
		
		searchField.selectAll();
		searchField.requestFocus();
		
		if( crawl!=null ) {
			// clear forward history from currentHistory.
			for(int i=history.size()-1; i>currentHistory; i--) {
				history.remove(i);
			}
			
			history.add(new SearchEntry(crawl.getCategory().getId(), crawl.getQuery(), page));
			if( history.size() > 50 ) 
				history.remove(0);
			currentHistory = history.size() - 1;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame("네이버 영영사전");
		f.setBounds( 50, 50, 300, 420 );
		System.out.println( "Icon: " + new ImageIcon("images.png").getImage() );
		f.setIconImage(new ImageIcon("images.png").getImage());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.add(new NaverDictionary(f), "Center");
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				f.setVisible(true);
			}
		});

	}

}
