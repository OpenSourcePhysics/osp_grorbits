package org.opensourcephysics.tuleja.grorbits;

import javax.swing.JFrame;
import org.opensourcephysics.controls.*;
import javax.swing.JMenuItem;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.JOptionPane;
import java.beans.PropertyChangeListener;
import org.opensourcephysics.tools.FontSizer;
import java.beans.PropertyChangeEvent;
import org.opensourcephysics.display.OSPRuntime;

/**
 *
 * GROrbitsApp adds XML to the GRorbits2 program.
 *
 * @author W. Christian
 * @version 1.0
 */
public class GROrbitsApp extends GRorbits2 {

  public GROrbitsApp(String[] args) {
    //  Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);

    //Put icon containing image of black hole to the window.
    Image iLogo;
    java.net.URL imgURL = GROrbitsApp.class.getResource("/org/opensourcephysics/tuleja/images/black-hole.gif");
    if (imgURL != null) {
      iLogo = new ImageIcon(imgURL).getImage();
    } else {
      System.err.println("Couldn't find file.");
      iLogo = null;
    }

    final JFrame frame = new JFrame("GRorbits");
    frame.setSize(1024, 633);
    frame.setIconImage(iLogo);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initialize();
    createFileMenu();
    if(args!=null&& args.length>0)loadXML(args[0]);
    frame.getContentPane().add(this);
    frame.setVisible(true);

    // Changes font size
    FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        int level = ( (Integer) e.getNewValue()).intValue();
        FontSizer.setFonts(frame.getJMenuBar(), level);
        FontSizer.setFonts(frame.getContentPane(), level);
      }
    });

  }

  void createFileMenu(){
    JMenu fileMenu = new JMenu("File");
    mainMenu.add(fileMenu,0);
    JMenuItem mi;
    /*
    mi = new JMenuItem("Inspect");
    fileMenu.add(mi);
    mi.setMnemonic('I');
    mi.getAccessibleContext().setAccessibleDescription("Inspects the configuration.");
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // display the xml document in a tree view
        XMLControl xml = new XMLControlElement(GROrbitsApp.this);
        OSPFrame xmlFrame = new OSPFrame(new XMLTreePanel(xml));
        xmlFrame.setSize(650, 550);
        xmlFrame.setKeepHidden(false);
        xmlFrame.setVisible(true);
        xmlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      }
      });
      */

      // load configuration
      mi = new JMenuItem("Load");
      fileMenu.add(mi);
      mi.setMnemonic('L');
      if(!org.opensourcephysics.js.JSUtil.isJS) {
        mi.getAccessibleContext().setAccessibleDescription("Loads a configuration.");
      }
      mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loadXML();
        }
      });

      // save configuration
      mi = new JMenuItem("Save");
      fileMenu.add(mi);
      mi.setMnemonic('S');
      if(!org.opensourcephysics.js.JSUtil.isJS) {
        mi.getAccessibleContext().setAccessibleDescription("Saves the configuration.");
      }
      mi.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          saveXML();
        }
      });
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new GROrbitsLoader();
  }

  public void loadXML(String fileName) {
     if((fileName==null)||fileName.trim().equals("")) {
        return;
     }
     XMLControlElement xml = new XMLControlElement(fileName);
     xml.loadObject(this); // load the data
  }

  public void loadXML() {
     JFileChooser chooser = OSPRuntime.getChooser();
     if(chooser==null) {
        return;
     }
     String oldTitle = chooser.getDialogTitle();
     chooser.setDialogTitle("Load XML Data");
     int result = chooser.showOpenDialog(null);
     chooser.setDialogTitle(oldTitle);
     if(result==JFileChooser.APPROVE_OPTION) {
        org.opensourcephysics.display.OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
        String fileName = chooser.getSelectedFile().getAbsolutePath();
        XMLControlElement xml = new XMLControlElement(fileName);
        xml.loadObject(this); // load the data
     }
   }

   public void saveXML() {
        JFileChooser chooser = OSPRuntime.getChooser();
        if(chooser==null) {
           return;
        }
        String oldTitle = chooser.getDialogTitle();
        chooser.setDialogTitle("Save XML Data");
        int result = chooser.showSaveDialog(null);
        chooser.setDialogTitle(oldTitle);
        if(result==JFileChooser.APPROVE_OPTION) {
           File file = chooser.getSelectedFile();
           // check to see if file already exists
           if(file.exists()) {
              int selected = JOptionPane.showConfirmDialog(null, "Replace existing "+file.getName()+"?", "Replace File",
                 JOptionPane.YES_NO_CANCEL_OPTION);
              if(selected!=JOptionPane.YES_OPTION) {
                 return;
              }
           }
           org.opensourcephysics.display.OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
           String fileName = file.getAbsolutePath();
           // String fileName = XML.getRelativePath(file.getAbsolutePath());
           if((fileName==null)||fileName.trim().equals("")) {
              return;
           }
           int i = fileName.toLowerCase().lastIndexOf(".xml");
           if(i!=fileName.length()-4) {
              fileName += ".xml";
           }
           XMLControl xml = new XMLControlElement(this);
           xml.write(fileName);
        }
   }


  public static void main(String[] args) {
     new GROrbitsApp(args);
  }
}
