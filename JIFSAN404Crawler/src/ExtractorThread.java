//Import necessary files
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * @author Bob Zimmerman
 */
public class ExtractorThread extends Thread {
	//Instance variables
	private URL url;
	private MyQueue<URL> linkQueue;
	private MySet<URL> beenThere;
	private MySet<URL> bad404s;
	//The format of the links we care about adding the linkQueue and searching:
	private static Pattern LINK_PATTERN = Pattern.compile("href *= *\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

	
	/**
	 * Constructor for the class
	 * 
	 * @param url			The URL to be scanned by the thread
	 * @param linkQueue		A Queue of links that have yet to be processed by a thread
	 * @param beenThere		A set of visited links
	 * @param bad404s		A set of URLs for 404 pages
	 */
	public ExtractorThread(URL url, MyQueue<URL> linkQueue, MySet<URL> beenThere, MySet<URL> bad404s) {
		this.url = url; //URL of the page to be searched
		this.linkQueue = linkQueue; //List of links to be searched
		this.beenThere = beenThere; //List of links that have already been searched
		this.bad404s = bad404s; //List of found 404 pages
	}
	
	/**
	 * Returns the thread's assigned URL as a string
	 * 
	 * @return	This thread's assigned URL, converted to a string
	 */
	public String getCurrentURL() {
		return url.toString();
	}

	/**
	 * Returns a set of all links within the supplied string which match the pre-specified pattern
	 * 
	 * @param s				String to be searched
	 * @param currentURL	Current URL for context in new URLs
	 * @return				Set of links found in the given string
	 */
	private static Set<URL> getLinks(String s, URL currentURL) {
		Matcher m = LINK_PATTERN.matcher(s);
		Set<URL> links = new HashSet<URL>();
		while ( m != null && s!= null && m.find()) {
			String found = m.group(1);
			try {
				links.add(new URL(currentURL, found));
			} catch (MalformedURLException e) { /*ignore*/ }
		}
		return links;
	}
	
	@Override
	/**
	 * Thread's primary run method that scans the given page for URLs that match the criteria and adds them to the queue
	 **/
	public void run() {
		//Create a BufferedReader to grab the input stream line by line
		BufferedReader buffReader = null;
		try {
			buffReader = new BufferedReader(new InputStreamReader(url.openStream()));
		}  catch(FileNotFoundException e){
			bad404s.add(url);
			return;
		} catch (IOException e) {
			//If a socket error happens, add the link back to the queue to be tried again
			linkQueue.enqueue(url);
			return;
		}
		
		//Get the first line of input, if possible
		String nextLine = "";
		try {
			nextLine = buffReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//Iterate through all lines of input
		while(nextLine!=null){
			//Get the set of all links on the current line
			Set<URL> linkSet = getLinks(nextLine, url);
			
			//Add all unprocessed links to the list
			for(URL currURL:linkSet){
				if(currURL!=null){
					if(currURL.getProtocol().equals("file") || currURL.getProtocol().equals("http")){
						//TODO pre-screen for images, files, and parts of the site not to be searched?
						if(!beenThere.contains(currURL)){
							linkQueue.enqueue(currURL);
							beenThere.add(currURL);
						}
					}
				}
			}
			try {
				nextLine=buffReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

}