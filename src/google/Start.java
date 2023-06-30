/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
public class Start {
    //my userAgent (put yours in the frame or change the one here)
    public static String userAgent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36";
    public final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    private ArrayList<String> baseURLs=new ArrayList();
    private final ArrayList<String> countries=new ArrayList();
    private final String[] organisation={"University"};
    //the files where the program puts the output
    private final write2file file=new write2file("output.csv","fail.txt");
    //max is the maximum amount of semaphores
    //old is the semaphore used for the arraylist of the used URLs
    private Semaphore max=new Semaphore(70),old=new Semaphore(1);
    private void acquireMax(){
        try {
            max.acquire();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    protected void releaseMax(){
        max.release();
    }
    
    //slows speed of requests
    private void stop(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //reads all the countries from file
    private void getCountries(){
        try {
            BufferedReader csvReader;
            csvReader = new BufferedReader(new FileReader("advanced-country.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                countries.add(row);
            }
            csvReader.close();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    //resets the output file
    public void reset(){
        file.reset();
        file.write("Country, Company, Type , URL, name, email");
    }
    
    //starts a single link search
    public void singleSearch(String UserAgent,String text){
        if(!UserAgent.equals(""))
            userAgent=UserAgent;
        Semaphore slow=new Semaphore(1);
        detailedSearch s=new detailedSearch(text,file,max,old,slow);
        s.singlePrint();
    }
    
    //starts a world search (multiple country searches)
    public void worldSearch(String UserAgent,int n){
        getCountries();
        for(int i=0;i<5;i++){
            String split[]=countries.get(i).split(",");
            System.out.println(split[1]);
            split[1]=split[1].replaceAll("\\s+","");
            for(String org: organisation){
                acquireMax();
                search(split[1],split[0],org,"research innovation","contact research",UserAgent,n);
                stop(2000);
            }
        }
    }
    
    //starts a single country/organisation search
    public void search(String country,String countryName,String company,String keyWords,String search,String UserAgent,int amount){
        try {
            //ArrayList of the websites
            ArrayList<detailedSearch> websites = new ArrayList();
            ArrayList<String> allNames =new ArrayList();
            String SearchTerms="",AnyTerms="";
            String terms[]=keyWords.split(" ");
            String Aterms[]=search.split(" ");
            for (String term : terms) {
                SearchTerms += term + "+";
            }
            for (String Aterm : Aterms) {
                AnyTerms += Aterm + "+";
            }
            SearchTerms+=company;
            AnyTerms+=company;
            if(!UserAgent.equals(""))
                userAgent=UserAgent;
            //Creates the final search URL and googles it
            String searchURL = GOOGLE_SEARCH_URL + "?as_q="+SearchTerms+"&as_oq="+AnyTerms+"&cr="+country+"&num="+amount;
            System.out.println(searchURL);
            Document doc = Jsoup.connect(searchURL).userAgent(userAgent).get();
            Elements links=doc.select("div.r a");
            //Creates a new info class for each link found
            links.forEach(link -> {
                Semaphore slow=new Semaphore(1);
                String URL=link.attr("href");
                String split[]=link.text().split("https");
                String s=split[0].replace("Cached", "");
                s=s.replace("Similar", "");
                s=s.replace("\n", "");
                if(!(URL.equals("#")||URL.contains("webcache"))){
                    detailedSearch ds=new detailedSearch(URL,file,max,old,slow);
                    baseURLs.add(ds.getBase(URL));
                    websites.add(ds);
                    allNames.add(s);
                }
            });
            releaseMax();
            for(int i=0;i<websites.size();i++){
                if(allNames.size()==websites.size()){
                    websites.get(i).addInfo(allNames.get(i),countryName,company);
                    acquireMax();
                    websites.get(i).start();
                }
                else
                    file.fail("complete fail!");
            }
            stop(1000);
            for(int i=0;i<websites.size();i++){
                websites.get(i).acquire();
                websites.get(i).release();
            }
        } catch (IOException ex) {
            releaseMax();
            java.util.logging.Logger.getLogger(Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
