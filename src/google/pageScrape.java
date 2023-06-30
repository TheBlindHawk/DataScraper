/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import static google.Start.userAgent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author leotomiselli
 */
//scrapes multiple pages
public class pageScrape extends Scrape{
    private String Base;
    private final ArrayList<String> multi=new ArrayList();
    private final ArrayList<Scrape> pluri=new ArrayList();
    pageScrape(String URL, write2file file,Semaphore max,Semaphore slow) {
        super(URL,file,max,slow);
    }
    
    //searches for all profile links
    public void plurisearch(){
        try {
            //searches for the webpage
            Document doc = Jsoup.connect(URL).userAgent(userAgent).get();
            //gets all internal links of the page
            Elements links = doc.select("a[href]");
            links.forEach(link -> {
                String Link=link.attr("href");
                if(Link.contains("profile")){
                    multi.add(Base+Link);
                }
            });
        } catch (IllegalArgumentException | IOException e){
            file.fail("scraping p error:"+URL);
        }
    }
    
    //gets the base of the URL
    public String getBase(String URL){
        if(URL.contains("http")){
            String split[]=URL.split("/");
            String p2[]=split[2].split("\\?");
            return split[0]+"//"+p2[0];
        }
        else
            return "https://"+URL;
    }
    
    //starts a scrapes for all the pages found
    private void scrape(){
        for(String m:multi){
            acquireMax();
            Scrape str=new Scrape(m,file,max,slow);
            str.start();
            pluri.add(str);
        }
        addmail();
    }
    
    //puts all emails from the single scrapes together
    private void addmail(){
        for(Scrape s : pluri){
            s.acquire();
            for (String element : s.getAddress()) {
                if(!emails.contains(element))
                    emails.add(element); 
            } 
            s.release();
        }
    }
    
    //the thread that runs this class
    @Override
    public void run(){
        slow();
        starting();
        Base=getBase(URL);
        if(emails.size()<4){
            plurisearch();
            releaseMax();
            scrape();
        }
        else
            releaseMax();
        release();
    }
}
