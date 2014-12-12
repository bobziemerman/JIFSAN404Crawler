import java.net.*;
import java.io.*;

/**
 * @author Bob Zimmerman
 */
public class Crawler {

	public static void main(String[] args) 
	throws InterruptedException, IOException {
		//create sets and queue
		MyQueue<URL> linkQueue = new MyQueue<URL>();
		MySet<URL> beenThere = new MySet<URL>();
		MySet<URL> bad404s = new MySet<URL>();
		
		//threads
		final int MAX_NUM_EXTRACTORS = 5;
		ExtractorThread[] extractors = new ExtractorThread[MAX_NUM_EXTRACTORS];
		
		//instantiate GUI
		new CrawlerGUI(linkQueue, beenThere, bad404s, extractors);
		URL url = null;

		//If an thread in the array is dead or null, waits until the linkQueue
		// is not empty and then processes the next URL
		while(true) {
			for(ExtractorThread ex:extractors){
				if(ex==null || !ex.isAlive()){
					synchronized(linkQueue){
						while(linkQueue.size()==0){
							linkQueue.wait();
						}
					}
					synchronized(extractors){
						boolean gate=false;
						while(gate==false){
							url = linkQueue.dequeue();
							URLConnection urlCon = url.openConnection();
							String content = urlCon.getContentType();
							if(content!=null){
								String conSub = null;
								try{
									conSub = content.substring(0,9);
								} catch(StringIndexOutOfBoundsException e){
									continue;
								}
								if(conSub.equals("text/html")){
									gate=true;
									for(int i=0;i<5;i++){
										if(extractors[i]==null ||
												!extractors[i].isAlive()){
											//ex = new ExtractorThread(url, linkQueue, picQueue, beenThere, doneThat);
											ex = new ExtractorThread(url, linkQueue, beenThere, bad404s);
											extractors[i]=ex;
											extractors[i].start();
											break;
										}
									}
								}

							}
						}
					}
				}
			}
		}
	}
}