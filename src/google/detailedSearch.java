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
public final class detailedSearch extends Search{
    private ArrayList<String> first=new ArrayList(); // primary URLs
    private ArrayList<String> second=new ArrayList(); // secondary URLs
    private ArrayList<secondSearch> deepSearch=new ArrayList();
    //search terms for links
    private final String[] primary={"contact","staff","team","researcher","Contact","Staff","Team","Researcher"}; 
    private final String[] secondary={"corporate","committee","office","collaborate","members","profile"};
    private String company,country;
    detailedSearch(String URL,write2file file,Semaphore max,Semaphore old,Semaphore slow){
        super(URL,file,max,old,slow);
        System.out.println(this.URL);
    }
    
    //searches for the links inside the page
    private void starting(){
        try {
            //searches for the webpage
            Document doc = Jsoup.connect(URL).userAgent(userAgent).get();
            //gets all internal links of the page
            Elements links = doc.select("a[href][display!=none]");
            links.forEach(link -> {
                String Link=link.attr("href");
                //checks if link is acceptable and adds it
                if(check(Link)){
                    Link=connect(Link);
                    if (reliable(primary,Link)){
                        try {
                            first.add(Link);
                            old.acquire();
                            oldURLs.add(Link);
                            old.release();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(detailedSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else if(reliable(secondary,Link)){
                        try {
                            second.add(Link);
                            old.acquire();
                            oldURLs.add(Link);
                            old.release();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(detailedSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        } catch (IllegalArgumentException | IOException e){
            file.fail("base error:"+URL);
        }
    }
    
    /*private String attach(String Link){
        String finish;
        String[] check = Link.split("/");
        int n=check.length;
        if(n>0)
            if(check[n-1].contains("...")){
                Link="";
                for(int i=0;i<n-1;i++)
                    Link+=check[i]+"/";
            }
        if (!(Link.contains("http"))){
            finish="https://"+Link;
        }
        else 
            finish=Link;
        return finish;
    }*/
    
    //scrapes all the acceptable links found
    @Override
    protected void scrape(){
        for(int i=0;i<first.size();i++){
            acquireMax();
            Scrape s=new Scrape(first.get(i),file,max,slow);
            s.start();
            scrape.add(s);
        }
        for(int i=0;i<second.size();i++){
            acquireMax();
            Scrape s=new Scrape(second.get(i),file,max,slow);
            s.start();
            scrape.add(s);
        }
    }
    
    //starts a search on the acceptable links
    private void Search2(){
        int count=0;
        for(int i=0;i<scrape.size();i++){
            Scrape s=scrape.get(i);
            s.acquire();
            if(s.getAddress().size()<5){
                acquireMax();
                deepSearch.add(new secondSearch(oldURLs,s.getURL(),file,max,old,slow));
            }
            if(s.getAddress().size()>0)
                count++;
            s.release();
        }
        if(count>5){
            acquireMax();
            pages();
        }
    }
    
    //gets all mails found
    private ArrayList<String> getAllMails(){
        ArrayList<String> mails=getmail();
        for(secondSearch search : deepSearch){
            search.acquire();
            for(String mail : search.getmail())
                if(!mails.contains(mail))
                    mails.add(mail);
            search.release();
        }
        return mails;
    }
    
    //gets the name from the mail if possible
    private String getName(String mail){
        String name="not found",sur;
        mail=mail.replaceAll("[0-9]","");
        String divide[]=mail.split("@");
        String s[]=divide[0].split("\\.");
        if(s.length==2){
            name=s[0].substring(0, 1).toUpperCase() + s[0].substring(1);
            sur=s[1].substring(0, 1).toUpperCase() + s[1].substring(1);
            if(name.length()>2&sur.length()>4)
                name=name+" "+sur;
            else
                name="not found";
        }
        return name;
    }
    
    //gets the informations related to the search
    public void addInfo(String name,String country,String company){
        this.company=company;
        this.country=country;
        this.name=name;
    }
    
    //prints all the mails found
    public void print(){
        ArrayList<String> mails=getAllMails();
        name=name.replaceAll("\\,", " ");
        String base=getBase(URL);
        System.out.println("printing");
        for (String mail : mails) {
            file.write(country+", "+name+", "+company+", "+base+", "+getName(mail)+", "+mail);
        }
    }
    
    //prints all mails found in case of single link
    public void singlePrint(){
        file.reset();
        file.write("URL, name, email");
        ArrayList<String> mails=getAllMails();
        String base=getBase(URL);
        for (String mail : mails) {
            file.write(base+", "+getName(mail)+", "+mail);
        }
    }
    
    //deletes all data (hope it works)
    public void reset(){
        this.oldURLs=null;
        this.deepSearch=null;
        this.pages=null;
        this.first=null;
        this.second=null;
        this.allURL=null;
        this.scrape=null;
    }
    
    //the thread that runs this class
    @Override
    public void run(){
        slow();
        starting();
        releaseMax();
        scrape();
        Search2();
        print();
        reset();
        release();
    }
}
