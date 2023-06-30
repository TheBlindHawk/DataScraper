/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author leotomiselli
 */
//the parent class of all search classes
public abstract class Search extends Thread{
    protected Semaphore sem= new Semaphore(1),old,max,slow;
    protected pageSearch pages;
    protected ArrayList<String> oldURLs=new ArrayList();
    protected ArrayList<String> allURL=new ArrayList();
    protected ArrayList<Scrape> scrape=new ArrayList();
    protected String URL;
    protected String name;
    protected write2file file;
    Search(String URL,write2file file,Semaphore max,Semaphore old,Semaphore slow){
        try {
            sem.acquire();
            this.slow=slow;
            this.old=old;
            this.max=max;
            this.file=file;
            this.URL=URL;
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //starts the scrape
    protected void scrape(){
        for(int i=0;i<allURL.size();i++){
            acquireMax();
            Scrape s=new Scrape(allURL.get(i),file,max,slow);
            s.start();
            scrape.add(s);
        }
    }
    
    //slows the requests
    protected void slow(){
        try {
            slow.acquire();
            Thread.sleep(500);
            slow.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //gets the base of the URL
    public String getBase(String URL){
        if(URL.contains("http")){
            String split[]=URL.split("/");
            return split[0]+"//"+split[2];
        }
        else
            return "https://"+URL;
    }
    
    //checks if the link has not already been used
    protected boolean isNew(String Link){
        try {
            old.acquire();
            boolean accepted=true;
            for(String old : oldURLs)
                if(Link.equals(old))
                    accepted=false;
            old.release();
            return accepted;
        } catch (InterruptedException ex) {
            return false;
        }
    }
    
    //checks if the link does NOT contain some key words
    protected boolean check(String Link){
        return (!(Link.contains("student")||
                  Link.contains("study")||
                  Link.contains("library")||
                  Link.contains("alumni")||
                  Link.contains("help")||
                  Link.contains("support")));
    }
    
    //creates the new link
    protected String connect(String Link){
        String finish;
        String[] check = Link.split("/");
        int n=check.length;
        if(n>0)
            if(check[n-1].contains("...")){
                Link="";
                for(int i=0;i<n-1;i++)
                    Link+=check[i]+"/";
            }
        String[] divided = URL.split("/");
        if (!(Link.contains("http"))){
            if(!Link.contains("www")){
                if(Link.split("//").length>1)
                    finish="https:"+Link;
                else
                    finish="https://"+divided[2]+Link;
            }
            else
                finish="https:"+Link;
        }
        else 
            finish=Link;
        return finish;
    }
    
    public void acquire(){
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void release(){
        sem.release();
    }
    
    protected void acquireMax(){
        try {
            max.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void releaseMax(){
        max.release();
    }
    
    //checks if the link is acceptable
    protected boolean reliable(String[] reliable,String Link){
        int reliability=0;
        for(int i=0;i<reliable.length;i++)
            if(Link.contains(reliable[i]))
                reliability++;
        return reliability>0;
    }
    
    //gets all the scrapes from page search
    protected void pages(){
        pages=new pageSearch(oldURLs,URL,file,max,slow);
        pages.acquire();
        for(Scrape scrape : pages.getScrape())
            this.scrape.add(scrape);
        pages.release();
    }
    
    //gets all the mails from the scrapes
    protected ArrayList<String> getmail(){
        ArrayList<String> send=new ArrayList();
        for(Scrape s : scrape){
            s.acquire();
            for (String element : s.getAddress()) {
                if(!send.contains(element))
                    send.add(element);
            }
            s.release();
        }
        return send;
    }
}
