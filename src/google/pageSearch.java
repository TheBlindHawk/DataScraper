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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author leotomiselli
 */
public class pageSearch extends Search{
    private boolean madeit=true;
    private ArrayList<String> pageURL=new ArrayList();
    pageSearch(ArrayList<String> oldURLs,String URL,write2file file,Semaphore max,Semaphore slow){
        super(URL,file,max,null,slow);
        this.oldURLs=oldURLs;
        //System.out.println("page: "+URL);
        this.start();
    }
    
    //searches if there might be multiple pages
    private void starting() {
        try {
            //searches for the webpage
            Document doc = Jsoup.connect(URL).userAgent(userAgent).get();
            //gets all internal links of the page
            Elements links = doc.select("a[href][display!=none]");
            links.forEach(link -> {
                String Link=link.attr("href");
                if(Link.contains("page")){
                    Link=connect(Link);
                    allURL.add(Link);
                }
            });
        } catch (IllegalArgumentException | IOException e){
            file.fail("pages error:"+URL);
        }
    }
    
    //for all pages found starts a page search
    private void pageScrape(){
        for(int i=0;i<pageURL.size();i++){
            acquireMax();
            pageScrape s=new pageScrape(pageURL.get(i),file,max,slow);
            s.start();
            scrape.add(s);
        }
    }
    
    //searches for multiple pages
    public void tryHard(String first,String last){
        System.out.println(URL);
        String num="";
        int c1=0,c2=0,n1=0,n2=0;
        String divide[];
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(first);
        while(m.find()) {
            c1++;
            num=m.group();
            n1=Integer.parseInt(num);
        }
        m = p.matcher(last);
        while(m.find()) {
            c2++;
            num=m.group();
            n2=Integer.parseInt(num);
        }
        if(c1==1&&c2==1){
            divide=last.split(num);
            while(n1<=n2){
                String s=divide[0]+Integer.toString(n1);
                if(divide.length==2)
                    s+=divide[1];
                System.out.println(s);
                pageURL.add(s);
                n1++;
            }
        }
        if(c1!=1||c2!=1)
            madeit=false;
    }
    
    public ArrayList<Scrape> getScrape(){
        return scrape;
    }
    
    //the thread that runs this class
    @Override
    public void run(){
        slow();
        starting();
        if(!allURL.isEmpty())
            tryHard(allURL.get(0),allURL.get(allURL.size()-1));
        releaseMax();
        if(madeit)
            pageScrape();
        else
            scrape();
        release();
    }
}
