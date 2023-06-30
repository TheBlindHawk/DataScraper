/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leotomiselli
 */
public class write2file {
    private String file,fail;
    private Semaphore sem=new Semaphore (1);
    write2file(String file,String fail){
        this.file=file;
        this.fail=fail;
    }
    public void write(String s){
        try{
            sem.acquire();
            FileWriter fw=new FileWriter(file,true);
            BufferedWriter write=new BufferedWriter(fw);
            write.write(s+" \n");
            write.flush();
            write.close();
            sem.release();
        }catch(IOException | InterruptedException e){
            sem.release();
            System.out.println(e);
        }
    }
    public void fail(String s){
        try {
            FileWriter fw=new FileWriter(fail,true);
            BufferedWriter write=new BufferedWriter(fw);
            write.write(s+" \n");
            write.flush();
            write.close();
        } catch (IOException ex) {
            Logger.getLogger(write2file.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void reset(){
        try{
            FileWriter f1=new FileWriter(file);
            FileWriter f2=new FileWriter(fail);
            f1.write("");
            f2.write("");
        }catch(IOException e){
            System.out.println(e);
        }
    }
}
