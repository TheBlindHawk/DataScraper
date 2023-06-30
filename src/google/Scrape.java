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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author leotomiselli
 */
//scrapes the emails
public class Scrape extends Thread{
    //searches for the emails inside a page
    protected String URL;
    protected ArrayList<String> URLs=new ArrayList();
    protected ArrayList<String> emails = new ArrayList();
    protected ArrayList<Document> docs=new ArrayList();
    protected write2file file;
    private String[] accepted={"contact","staff","team","researcher","members","profile"}; 
    protected Semaphore s=new Semaphore(1),max,slow;
    Scrape(String URL,write2file file,Semaphore max,Semaphore slow) {
        try {
            s.acquire();
            this.slow=slow;
            this.max=max;
            this.URL=URL;
            this.file=file;
            System.out.println("scrape: "+URL);
        } catch (InterruptedException ex) {
            Logger.getLogger(Scrape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //slows down the request speed
    protected void slow(){
        try {
            slow.acquire();
            Thread.sleep(500);
            slow.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //starts the scrape of the page
    protected void starting(){
        try {
            //searches for the webpage
            docs.add(Jsoup.connect(URL).userAgent(userAgent).get());
            hashSearch();
            for(Document doc : docs){
                //byHREF(doc);
                byAT(doc);
                byroundAT(doc);
            }
            emails=removeDuplicates(emails);
        } catch (IOException | IllegalArgumentException ex) {
            file.fail("scrape error: "+URL);
        }
    }
    
    //searches for micro links (idk if its the right term)
    protected void hashSearch(){
        Elements links = docs.get(0).select("a[href]");
        links.forEach(link -> {
            String Link=link.attr("href");
            if(Link.contains("#")&&reliable(accepted,Link)){
                URLs.add(URL+Link);
            }
        });
        URLs=removeDuplicates(URLs);
        try {
            for(String url : URLs)
                docs.add(Jsoup.connect(url).userAgent(userAgent).get());
        } catch (IOException ex) {
            file.fail("scraping error: "+docs.get(docs.size()-1).baseUri());
        }
    }
    
    /*protected void byHREF(Document doc){
        //gets all internal links of the page
        Elements links = doc.select("a[href]");
        links.forEach(link -> {
            String Link=link.attr("href");
            String mailto[]=Link.split(":");
            if(mailto[0].equals("mailto")&& mailto.length>1){
                if(check(mailto[1]))
                    emails.add(mailto[1].replace("%20", ""));
            }
        });
    }*/
    
    //searches for the emails
    protected void byAT(Document doc){
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(doc.text());
        while (matcher.find()) {
            String str=matcher.group();
            if(check(str)){
                str=str.replace("%20", "");
                emails.add(str);
            }
        }
    }
    
    //searches for the emails
    protected void byroundAT(Document doc){
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+(at)[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(doc.text());
        while (matcher.find()) {
            String str=matcher.group();
            if(check(str)){
                str=str.replace("%20", "");
                emails.add(str);
            }
        }
    }
    
    //checks if email does NOT contain certain key words
    private boolean check(String Link){
        return !(Link.contains("student")||
                 Link.contains("study")||
                 Link.contains("name")||
                 Link.contains("info")||
                 Link.contains("secretar")||
                 Link.contains("sekretar")||
                 Link.contains("office")||
                 Link.contains("finance")||
                 Link.contains("business")||
                 Link.contains("opportunity")||
                 Link.contains("recruit")||
                 Link.contains("communicait")||
                 Link.contains("customer")||
                 Link.contains("help")||
                 Link.contains("assist")||
                 Link.contains("security")||
                 Link.contains("loan")||
                 Link.contains("commercial")||
                 Link.contains("clinic")||
                 Link.contains("ethics")||
                 Link.contains("contract")||
                 Link.contains("service")||
                 Link.contains("research")||
                 Link.contains("training")||
                 Link.contains("graduate")||
                 Link.contains("admission")||
                 Link.contains("recruitment")||
                 Link.contains("international")||
                 Link.contains("support")||
                 Link.contains("contact")||
                 Link.contains("development")||
                 Link.contains("development")||
                 Link.contains("learn")||
                 Link.contains("alumni"));
    }
    
    //checks if micro link is acceptable
    protected boolean reliable(String[] reliable,String Link){
        int reliability=0;
        for (String rely : reliable) {
            if (Link.contains(rely)) {
                reliability++;
            }
        }
        return reliability>0;
    }
    
    //removes duplicates from inside arrays
    protected ArrayList<String> removeDuplicates(ArrayList<String> oldList){
        ArrayList<String> newList=new ArrayList();
        for (String element : oldList) {
            if (!newList.contains(element)) {
                newList.add(element); 
            } 
        } 
        return newList;
    }
    
    public void acquire(){
        try {
            s.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Scrape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void release(){
        s.release();
    }
    
    protected void acquireMax(){
        try {
            max.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void releaseMax(){
        max.release();
    }
    
    //returns the URL that has been scraped
    public String getURL(){
        return URL;
    }
    
    //returns all the mails found
    public ArrayList<String> getAddress(){
        return emails;
    }
    
    //the thread that runs the class
    @Override
    public void run() {
        slow();
        starting();
        releaseMax();
        release();
    }
}
