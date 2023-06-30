/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import java.util.concurrent.Semaphore;
import javax.swing.WindowConstants;

public class Google {
    private static Semaphore max=new Semaphore(5),old=new Semaphore(1);
    public static void main(String[] args){
        //cretes the frame that shows the options
        frame f=new frame();
        f.setSize(700,500);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
