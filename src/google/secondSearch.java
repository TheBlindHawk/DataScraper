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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author leotomiselli
 */
public class secondSearch extends Search{
    private final String[] accepted={"member","staff","profile","researcher"};
    secondSearch(ArrayList<String> oldURLs,String URL,write2file file,Semaphore max,Semaphore old,Semaphore slow){
        super(URL,file,max,old,slow);
        this.oldURLs=oldURLs;
        System.out.println("second: "+URL);
        this.start();
    }
    
    //searches for the link inside the page
    private void starting() {
        try {
            //searches for the webpage
            Document doc = Jsoup.connect(URL).userAgent(userAgent).get();
            //gets all internal links of the page
            Elements links = doc.select("a[href][display!=none]");
            links.forEach(link -> {
                String Link=link.attr("href");
                if(check(Link)&&reliable(accepted,Link)){
                    try {
                        Link=connect(Link);
                        if(isNew(Link)){
                            old.acquire();
                            allURL.add(Link);
                            oldURLs.add(Link);
                            old.release();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(secondSearch.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (IllegalArgumentException | IOException e){
            file.fail("internal error:"+URL);
        }
    }
    
    //starts a search on the acceptable links
    private void Search2(){
        int count=0;
        for(int i=0;i<scrape.size();i++){
            Scrape s=scrape.get(i);
            s.acquire();
            if(s.getAddress().size()>0)
                count++;
            s.release();
        }
        if(count>5){
            acquireMax();
            pages();
        }
    }
    
    @Override
    public void run(){
        slow();
        starting();
        releaseMax();
        scrape();
        Search2();
        release();
    }
}
