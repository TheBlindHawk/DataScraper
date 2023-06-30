/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 *
 * @author leotomiselli
 */
public class frame extends JFrame{
    //declares all the items to show on the frame
    private int searching;
    private final Start start;
    private final JPanel panel=new JPanel();
    private final JLabel lamount=new JLabel("AMOUNT");
    private final JLabel lcountry=new JLabel("COUNTRY");
    private final JLabel lcompany=new JLabel("COMPANY");
    private final JLabel lkeyWords=new JLabel("KEYWORDS");
    private final JLabel lwords=new JLabel("WORDS");
    private final JLabel UA=new JLabel("Your User Agent");
    private final JSpinner amount=new JSpinner();
    private final JTextField country=new JTextField("countryAU");
    private final JTextField company=new JTextField("University");
    private final JTextField keyWords=new JTextField("research innovation");
    private final JTextField words=new JTextField("contact");
    private final JTextField UserAgent=new JTextField("");
    private final JButton button=new JButton("submit");
    private final String[] s={"world-search","search country","single link"};
    private final JComboBox mode=new JComboBox(s);
    frame(){
        //declares the class that starts the search/scraping
        start=new Start();
        set();
    }
    
    //depending on the option the frame shows different things
    //single search is a single link to start the scraping
    public void addSingle(){
        panel.add(lcountry);
        lcountry.setText("URL");
        panel.add(country);
        panel.add(UA);
        panel.add(UserAgent);
        panel.add(button);
        panel.add(mode);
    }
    //country search searches for contacts in a country
    private void addCountry(){
        lcountry.setText("COUNTRY");
        panel.add(lcountry);
        panel.add(lcompany);
        panel.add(lkeyWords);
        panel.add(lwords);
        panel.add(lamount);
        panel.add(amount);
        panel.add(country);
        panel.add(company);
        panel.add(keyWords);
        panel.add(words);
        panel.add(button);
        panel.add(UA);
        panel.add(UserAgent);
        panel.add(mode);
    }
    //world search searches for the contacts in all the countries
    private void addWorld(){
        panel.add(lamount);
        panel.add(amount);
        panel.add(button);
        panel.add(UA);
        panel.add(UserAgent);
        panel.add(mode);
    }
    
    //frame at the beginning (set on worldSearch)
    private void set(){
        panel.setLayout(null);
        lamount.setText("webpages");
        lamount.setBounds(250,25,150,30);
        amount.setBounds(350,25,50,30);
        country.setBounds(250,70,200,30);
        company.setBounds(250,140,200,30);
        keyWords.setBounds(250,210,200,30);
        words.setBounds(250,280,200,30);
        lcountry.setBounds(250,50,200,30);
        lcompany.setBounds(250,120,200,30);
        lkeyWords.setBounds(250,190,200,30);
        lwords.setBounds(250,260,200,30);
        button.setBounds(300,400,100,30);
        UA.setBounds(250,325,200,30);
        UserAgent.setBounds(250,350,200,30);
        //the combobox that chooses the mode
        mode.setBounds(50,50,100,30);
        mode.addActionListener(new ActionListener(
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                searching = cb.getSelectedIndex();
                System.out.println(searching);
                panel.removeAll();
                switch(searching){
                    case 0:{
                        addWorld();
                        break;
                    }
                    case 1:{
                        addCountry();
                        break;
                    }
                    case 2:{
                        addSingle();
                        break;
                    }
                }
                repaint();
            }
        });
        //the button that starts the search
        button.addActionListener(new ActionListener(
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                start.reset();
                switch(searching){
                    case 0:{
                        start.worldSearch(UserAgent.getText(),(Integer)amount.getValue());
                    }
                    case 1:{
                        start.search(country.getText(),country.getText(),company.getText(),keyWords.getText(),words.getText(),UserAgent.getText(),(Integer)amount.getValue());
                    }
                    case 2:{
                        start.singleSearch(UserAgent.getText(),country.getText());
                    }
                }
            }
        });
        addWorld();
        this.add(panel);
    }
}
