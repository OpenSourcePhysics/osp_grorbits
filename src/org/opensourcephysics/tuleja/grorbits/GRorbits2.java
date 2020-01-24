package org.opensourcephysics.tuleja.grorbits;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.*;

import javajs.async.SwingJSUtils;
import javajs.async.SwingJSUtils.StateHelper;
import javajs.async.SwingJSUtils.StateMachine;

/**
 * Application: GRorbits2
 * Author: Slavomir Tuleja
 * Work started: August 18, 2002 - August 31, 2002
 * First major revision: Febuary and March 2003
 * Last major revision: May 2006 
 * Last change: July 17, 2006
 * Contributors: Wolfgang Christian, Tomas Jezo, Jozef Hanc. 
 * Description: The application simulates motion of an orbiter or light moving 
 * freely in equatorial plane of a Kerr black hole
 * in Boyer-Lindquist coordinates and in rain coordinates
 */
public class GRorbits2 extends JApplet implements Runnable, ActionListener, PropertyChangeListener, StateMachine {
	
  Container cp;
  Thread animationThread;  
  
  ImageIcon iconPlay, iconStop, iconStepForward, iconStepBack;
  JButton btnStartStop, btnStepForward, btnStepBack, btnReset, btnPlotOrbit, btnResetExplore, btnShowOrbitData;
  JPanel pnlSouth, pnlButtons, pnlEffPot, pnlOrbit, pnlExplore;
  JSplitPane splitPane, splitPaneOrbitAndPotential;
  JSplitPane splitPaneProperties;
  JLabel lblComment;
  OrbitSliderControls sliderControls;
  InitialConditionsInspector icInspector;
  OrbitDataInspector odInspector;
  
  Orbit orbit;
  
  JMenuBar mainMenu;
  
  JMenu menuProgramMode, menuMetric, menuOrbits, menuDisplay, menuInitial, menuZoom, menuAbout;
  JMenuItem menuItemCircStable, menuItemCircUnstable, menuItemCircNegUnstable, menuItemCircPosUnstable;
  ButtonGroup bgMode, bgOrbits;
  JRadioButtonMenuItem menuItemNewton, menuItemBoyerLindquistM,
  menuItemBoyerLindquistL, menuItemRainM, menuItemRainL;
  JCheckBoxMenuItem menuItemGrid, menuItemTrail, menuItemScale, menuItemRing;
  
  JRadioButtonMenuItem menuItemTimePlot, menuItemFullOrbitPlot;
  
  ButtonGroup bgInitial;
  JRadioButtonMenuItem menuItemInward, menuItemOutward;
  
  JCheckBoxMenuItem menuItemAutoZoom;
  
  JMenuItem menuItemAbout;
  
  DecimalFormat format = new DecimalFormat("0.000");
  
  double rMaxOld;
  GridBagConstraints c;
  
  OrbitDrawingPanel orbDrawingPanel;
  PotentialDrawingPanel potDrawingPanel;
  
  public GRorbits2(){
    //  look and feel
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {
      System.err.println("Couldn't set the desired look and feel...");
    }
    
  }
  
  /**
   * BH Was initialize(), but then the applet could be run by itself.
   * 
   */
  public void init(){
    
    //definition of Menu
    mainMenu = new JMenuBar();
    menuProgramMode = new JMenu("Program mode");
    
    menuMetric = new JMenu("Metric");
    menuOrbits = new JMenu("Orbit");
    menuDisplay = new JMenu("Display");
    menuInitial = new JMenu("Initial");
    menuZoom = new JMenu("Zoom");
    menuAbout = new JMenu("About");
    
    bgOrbits=new ButtonGroup();
    menuItemTimePlot = new JRadioButtonMenuItem("Time plot",true);
    menuItemTimePlot.setEnabled(true);
    menuItemFullOrbitPlot = new JRadioButtonMenuItem("Full orbit plot",false);
    menuItemFullOrbitPlot.setEnabled(true);
    menuOrbits.add(menuItemTimePlot);
    menuOrbits.add(menuItemFullOrbitPlot);
    bgOrbits.add(menuItemTimePlot);
    bgOrbits.add(menuItemFullOrbitPlot);
    
    bgMode = new ButtonGroup();
    
    menuItemBoyerLindquistM = new JRadioButtonMenuItem("Bookkeeper m>0", true);
    menuItemBoyerLindquistL = new JRadioButtonMenuItem("Bookkeeper m=0", false);
    menuItemRainM = new JRadioButtonMenuItem("Rain m>0", false);
    menuItemRainL = new JRadioButtonMenuItem("Rain m=0", false);
    
    menuItemNewton = new JRadioButtonMenuItem("Newton", false);
    
    //for the following no button group necessary...
    menuItemGrid = new JCheckBoxMenuItem("Show grid", true);
    menuItemTrail = new JCheckBoxMenuItem("Show trail", true);
    menuItemScale = new JCheckBoxMenuItem("Show scale", true);
    menuItemRing = new JCheckBoxMenuItem("Show ring", true);
    
    bgInitial = new ButtonGroup();
    menuItemInward = new JRadioButtonMenuItem("Inward", true);
    menuItemOutward = new JRadioButtonMenuItem("Outward", false);
    menuItemCircStable = new JMenuItem("Set to stable circular orbit");
    menuItemCircUnstable = new JMenuItem("Set to unstable circular orbit");
    menuItemCircPosUnstable = new JMenuItem("Set to unstable counterclockwise circular orbit");
    menuItemCircNegUnstable = new JMenuItem("Set to unstable clockwise circular orbit");
    
    
    menuItemAutoZoom = new JCheckBoxMenuItem("Auto adjust", false);
    
    menuItemAbout = new JMenuItem("About this program");
    
    menuMetric.add(menuItemBoyerLindquistM);
    bgMode.add(menuItemBoyerLindquistM);
    menuMetric.add(menuItemBoyerLindquistL);
    bgMode.add(menuItemBoyerLindquistL);
    menuMetric.addSeparator();
    menuMetric.add(menuItemRainM);
    bgMode.add(menuItemRainM);
    menuMetric.add(menuItemRainL);
    bgMode.add(menuItemRainL);
    menuMetric.addSeparator();
    menuMetric.add(menuItemNewton);
    bgMode.add(menuItemNewton);
    
    menuDisplay.add(menuItemGrid);
    menuDisplay.add(menuItemTrail);
    menuDisplay.add(menuItemScale);
    menuDisplay.add(menuItemRing);
    
    
    bgInitial.add(menuItemInward);
    bgInitial.add(menuItemOutward);
    
    
    
    
    
    menuZoom.add(menuItemAutoZoom);
    
    
    menuAbout.add(menuItemAbout);
    
    menuProgramMode.add(menuOrbits);
    menuProgramMode.add(menuMetric);
    mainMenu.add(menuProgramMode);
    mainMenu.add(menuDisplay);
    mainMenu.add(menuInitial);
    mainMenu.add(menuZoom);
    mainMenu.add(menuAbout);
    setJMenuBar(mainMenu);
    
    
    //Menu action listeners
    menuItemTimePlot.addActionListener(this);
    menuItemFullOrbitPlot.addActionListener(this);
    menuItemNewton.addActionListener(this);
    menuItemBoyerLindquistM.addActionListener(this);
    menuItemBoyerLindquistL.addActionListener(this);
    menuItemRainM.addActionListener(this);
    menuItemRainL.addActionListener(this);
    menuItemGrid.addActionListener(this);
    menuItemTrail.addActionListener(this);
    menuItemScale.addActionListener(this);
    menuItemRing.addActionListener(this);
    menuItemInward.addActionListener(this);
    menuItemOutward.addActionListener(this);
    menuItemCircStable.addActionListener(this);
    menuItemCircUnstable.addActionListener(this);
    menuItemCircNegUnstable.addActionListener(this);
    menuItemCircPosUnstable.addActionListener(this);
    menuItemAutoZoom.addActionListener(this);
    menuItemAbout.addActionListener(this);
    
    
    //Buttons...
    //loading images
    String imagePath = "/org/opensourcephysics/tuleja/images/";
    iconPlay = createImageIcon(imagePath + "Play.gif");
    iconStop = createImageIcon(imagePath + "Stop.gif");
    iconStepForward = createImageIcon(imagePath + "StepForward.gif");
    iconStepBack = createImageIcon(imagePath + "StepBack.gif");
    
    btnStartStop = new JButton("Start", iconPlay);
    btnStartStop.addActionListener(this);
    
    btnStepForward = new JButton("Step", iconStepForward);
    btnStepForward.setActionCommand("stepForward");
    btnStepForward.addActionListener(this);
    
    btnStepBack = new JButton("Step", iconStepBack);
    btnStepBack.setActionCommand("stepBack");
    btnStepBack.addActionListener(this);
    
    btnReset = new JButton("Reset");
    btnReset.addActionListener(this);
    
    btnPlotOrbit = new JButton("Plot the orbit");
    btnPlotOrbit.addActionListener(this);
    btnResetExplore = new JButton("Reset");
    btnResetExplore.addActionListener(this);
    btnShowOrbitData = new JButton("Show orbit data");
    btnShowOrbitData.addActionListener(this);
    
    
    //  Orbit
    orbit = new OrbitBoyerLindquistM();
    prepareLayout(orbit);
    
    //remember initial rMax
    rMaxOld=potDrawingPanel.getRMax();
  }
  
  
  public void prepareLayout(Orbit orbit){
    
    //menu Initial
    menuInitial.removeAll();
    menuInitial.add(menuItemInward);
    menuInitial.add(menuItemOutward);
    menuInitial.addSeparator();
    
    if(menuItemBoyerLindquistM.isSelected()||menuItemRainM.isSelected()){
      menuInitial.add(menuItemCircStable);
      menuInitial.add(menuItemCircUnstable);
    }
    else if(menuItemNewton.isSelected()){
      menuInitial.add(menuItemCircStable);
    }
    else{
      menuInitial.add(menuItemCircPosUnstable);
      menuInitial.add(menuItemCircNegUnstable);
    }
    
    
    pnlButtons = new JPanel();
    pnlButtons.setLayout(new GridLayout(1,4));
    pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
    pnlButtons.add(btnStartStop);
    pnlButtons.add(btnStepBack);
    pnlButtons.add(btnStepForward);
    pnlButtons.add(btnReset);
    
    lblComment = new JLabel(" ");
    lblComment.setFont(new Font("SansSerif", Font.BOLD, 10));
    lblComment.setForeground(Color.red);
    lblComment.setBorder(BorderFactory.createTitledBorder(""));
    
    pnlExplore = new JPanel();
    pnlExplore.setLayout(new GridLayout(1,3));
    pnlExplore.setBorder(BorderFactory.createTitledBorder(""));
    pnlExplore.add(btnPlotOrbit);
    pnlExplore.add(btnResetExplore);
    pnlExplore.add(btnShowOrbitData);
    
    pnlSouth = new JPanel();
    pnlSouth.setBackground(new Color(250,250,250));
    pnlSouth.setLayout(new GridBagLayout());
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    c.ipady = 0;
    pnlSouth.add(lblComment,c);
    
    
    if(menuItemTimePlot.isSelected()){
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 1;
      pnlSouth.add(pnlButtons,c);
    }
    else {//if menuItemFullOrbitPlot is selected
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 1;
      pnlSouth.add(pnlExplore,c);
      pnlSouth.validate();
      pnlSouth.repaint();
      btnShowOrbitData.setText("Show orbit data");
      splitPaneProperties.setBottomComponent(sliderControls);
      splitPaneProperties.setResizeWeight(0.25);
      orbit.reset();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
      odInspector.repaint();
      orbDrawingPanel.setInitialArrowVisible(true);
    }
    
    
    
    //Drawing Panels
    orbDrawingPanel = new OrbitDrawingPanel(orbit);
    orbDrawingPanel.setPreferredMinMaxX(-32,32);
    orbDrawingPanel.setPreferredMinMaxY(-32,32);
    
    pnlOrbit = new JPanel();
    pnlOrbit.setLayout(new BorderLayout());
    pnlOrbit.add(orbDrawingPanel, BorderLayout.CENTER);
    if(menuItemBoyerLindquistM.isSelected())pnlOrbit.setBorder(BorderFactory.createTitledBorder("Orbit: Bookkeeper m>0"));
    else if(menuItemBoyerLindquistL.isSelected())pnlOrbit.setBorder(BorderFactory.createTitledBorder("Orbit: Bookkeeper m=0"));
    else if(menuItemRainM.isSelected())pnlOrbit.setBorder(BorderFactory.createTitledBorder("Orbit: Rain m>0"));
    else if(menuItemRainL.isSelected())pnlOrbit.setBorder(BorderFactory.createTitledBorder("Orbit: Rain m=0"));
    else if(menuItemNewton.isSelected())pnlOrbit.setBorder(BorderFactory.createTitledBorder("Orbit: Newton"));
    
    sliderControls = new OrbitSliderControls(orbit);
    potDrawingPanel = new PotentialDrawingPanel(orbit);
    
    pnlEffPot = new JPanel();
    pnlEffPot.setLayout(new BorderLayout());
    pnlEffPot.add(potDrawingPanel, BorderLayout.CENTER);
    pnlEffPot.setBorder(BorderFactory.createTitledBorder("Effective potential versus r"));
    potDrawingPanel.setPreferredMinMaxX(0,25);
    sliderControls.removeAll();
    if(menuItemBoyerLindquistM.isSelected()||menuItemRainM.isSelected()){
      potDrawingPanel.setPreferredMinMaxY(0.94,1.0038);
      sliderControls.add(sliderControls.slA);
      sliderControls.add(sliderControls.slLm);
    }
    else if(menuItemNewton.isSelected()){
      potDrawingPanel.setPreferredMinMaxY(-0.06,0.0038);
      sliderControls.add(sliderControls.slA);
      sliderControls.add(sliderControls.slLm);
    }
    else{
      potDrawingPanel.setPreferredMinMaxY(-0.30,0.30);
      sliderControls.add(sliderControls.slA);
      sliderControls.add(sliderControls.slB);
      sliderControls.setB(1.0/orbit.getIC().getInvB());
      sliderControls.repaint();
      
    }
    
    icInspector = new InitialConditionsInspector(orbit);
    icInspector.setBorder(BorderFactory.createTitledBorder("Initial conditions"));
    
    odInspector = new OrbitDataInspector(orbit);
    odInspector.setBorder(BorderFactory.createTitledBorder("Orbit data"));
    
    splitPaneProperties = new JSplitPane(JSplitPane.VERTICAL_SPLIT, icInspector, sliderControls);
    splitPaneProperties.setOneTouchExpandable(true);
    splitPaneProperties.setResizeWeight(0.25);
    
    splitPaneOrbitAndPotential = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlOrbit, pnlEffPot);
    splitPaneOrbitAndPotential.setOneTouchExpandable(true);
    splitPaneOrbitAndPotential.setResizeWeight(0.7);
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPaneOrbitAndPotential, splitPaneProperties);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(0.8);
    splitPane.setDividerLocation(0.75);
    
    //everything added to content pane...
    cp = getContentPane();
    cp.removeAll();
    cp.setLayout(new BorderLayout());
    cp.add(pnlSouth,BorderLayout.SOUTH);
    cp.add(splitPane,BorderLayout.CENTER);
    
    cp.validate();
    cp.repaint();
    
    //setting up property change listeners
    sliderControls.addPropertyChangeListener(this);
    orbit.addPropertyChangeListener(this);
    orbDrawingPanel.addPropertyChangeListener(this);
    potDrawingPanel.addPropertyChangeListener(this);
    icInspector.addPropertyChangeListener(this);
    odInspector.addPropertyChangeListener(this);
    
  }
  
  /** Returns an ImageIcon, or null if the path was invalid. */
  protected ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = GRorbits2.class.getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL);
    }
    else {
      System.err.println("Couldn't find file: " + path);
      return null;
    }
    
  }
  
  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals("Start")){
      btnStartStop.setText("Pause");
      btnStartStop.setIcon(iconStop);
      menuProgramMode.setEnabled(false);
      menuInitial.setEnabled(false);
      menuZoom.setEnabled(false);
      menuAbout.setEnabled(false);
      icInspector.table.setEnabled(false);
      odInspector.table.setEnabled(false);
      btnStepForward.setEnabled(false);
      btnStepBack.setEnabled(false);
      btnReset.setEnabled(false);
      sliderControls.slA.setEnabled(false);
      sliderControls.slLm.setEnabled(false);
      
      splitPaneProperties.setBottomComponent(odInspector);
      splitPaneProperties.setResizeWeight(0.25);
     
      splitPane.setDividerLocation(1.0);
      String sA = "J/M = ".concat(format.format(orbit.getIC().getA())).concat(" M");
      String sEm = "E/m = ".concat(format.format(orbit.getIC().getEm())).concat(" M");
      String sLm = "L/m = ".concat(format.format(orbit.getIC().getLm())).concat(" M");
      String sInvB = "M/b = ".concat(format.format(orbit.getIC().getInvB()));
      lblComment.setForeground(Color.BLACK);
      if(menuItemBoyerLindquistM.isSelected()||menuItemRainM.isSelected()||menuItemNewton.isSelected()){
        lblComment.setText(sA+"   "+sEm+"   "+sLm);
      }
      else lblComment.setText(sA+"   "+sInvB);
      
      
      orbDrawingPanel.setInitialArrowVisible(false);
      
      startAnimation();
      
    }
    else if(e.getActionCommand().equals("Pause")){
      btnStartStop.setText("Start");
      btnStartStop.setIcon(iconPlay);
      stopAnimation();
      menuProgramMode.setEnabled(true);
      menuInitial.setEnabled(true);
      menuZoom.setEnabled(true);
      menuAbout.setEnabled(true);
      icInspector.table.setEnabled(true);
      odInspector.table.setEnabled(true);
      btnStepForward.setEnabled(true);
      btnStepBack.setEnabled(true);
      btnReset.setEnabled(true);
      sliderControls.slA.setEnabled(true);
      sliderControls.slLm.setEnabled(true);
      
      splitPane.setDividerLocation(0.75);
      lblComment.setForeground(Color.red);
      lblComment.setText(" ");
      
      odInspector.repaint();
    }
    else if(e.getActionCommand().equals("stepForward")){
      splitPaneProperties.setBottomComponent(odInspector);
      splitPaneProperties.setResizeWeight(0.25);
      
      orbit.doStep();
      if (menuItemAutoZoom.isSelected()) adjustZoom();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      
      if((orbit.getT()<orbit.getIC().getDT()) && (orbit.getT()>-orbit.getIC().getDT())) orbDrawingPanel.setInitialArrowVisible(true);
      else orbDrawingPanel.setInitialArrowVisible(false);
      
      orbDrawingPanel.repaint();
      odInspector.repaint();
      potDrawingPanel.repaint();
      
    }
    else if(e.getActionCommand().equals("stepBack")){
      splitPaneProperties.setBottomComponent(odInspector);
      splitPaneProperties.setResizeWeight(0.25);
      
      orbit.doStepBack();
      if (menuItemAutoZoom.isSelected()) adjustZoom();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      
      if((orbit.getT()<orbit.getIC().getDT()) && (orbit.getT()>-orbit.getIC().getDT())) orbDrawingPanel.setInitialArrowVisible(true);
      else orbDrawingPanel.setInitialArrowVisible(false);
      
      orbDrawingPanel.repaint();
      odInspector.repaint();
      potDrawingPanel.repaint();
      
    }
    else if(e.getActionCommand().equals("Reset")){
      btnStartStop.setEnabled(true);
      btnStepForward.setEnabled(true);
      btnStepBack.setEnabled(true);
      orbit.reset();
      odInspector.repaint();
      icInspector.repaint();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      
      
      //set old value of rMax to orbDrawingPanel and potDrawingPanel
      if (menuItemAutoZoom.isSelected()) {
        orbDrawingPanel.setRMax(rMaxOld);
        potDrawingPanel.setRMax(rMaxOld);
        potDrawingPanel.rescale();
      }
      
      splitPaneProperties.setBottomComponent(sliderControls);
      splitPaneProperties.setResizeWeight(0.25);
      
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
      
      btnShowOrbitData.setText("Show orbit data");
      
      orbDrawingPanel.setInitialArrowVisible(true);
      
      
      
    }
    else if(e.getActionCommand().equals("Full orbit plot")){
      pnlSouth.remove(pnlButtons);
      pnlSouth.add(pnlExplore,c);
      pnlSouth.validate();
      pnlSouth.repaint();
      btnShowOrbitData.setText("Show orbit data");
      splitPaneProperties.setBottomComponent(sliderControls);
      splitPaneProperties.setResizeWeight(0.25);
      orbit.reset();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
      odInspector.repaint();
      orbDrawingPanel.setInitialArrowVisible(true);
    }
    else if(e.getActionCommand().equals("Time plot")){
      btnStartStop.setEnabled(true);
      pnlSouth.remove(pnlExplore);
      pnlSouth.add(pnlButtons,c);
      pnlSouth.validate();
      pnlSouth.repaint();
      btnShowOrbitData.setText("Show orbit data");
      splitPaneProperties.setBottomComponent(sliderControls);
      splitPaneProperties.setResizeWeight(0.25);
      orbit.reset();
      btnStepForward.setEnabled(true);
      btnStepBack.setEnabled(true);
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
      odInspector.repaint();
    }
    else if(e.getActionCommand().equals("Plot the orbit")){
      orbit.reset();
      orbit.computeOrbitAtOnce();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
      odInspector.repaint();
    }
    else if(e.getActionCommand().equals("Show orbit data")){
      btnShowOrbitData.setText("Show sliders");
      splitPaneProperties.setBottomComponent(odInspector);
      splitPaneProperties.setResizeWeight(0.25);
    }
    else if(e.getActionCommand().equals("Show sliders")){
      btnShowOrbitData.setText("Show orbit data");
      splitPaneProperties.setBottomComponent(sliderControls);
      splitPaneProperties.setResizeWeight(0.25);
    }
    else if(e.getActionCommand().equals("Show grid")){
      if(orbDrawingPanel.getShowGrid()) orbDrawingPanel.setShowGrid(false);
      else {
        orbDrawingPanel.setShowGrid(true);
      }
      orbDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Show trail")){
      if(orbDrawingPanel.getShowTrail()) orbDrawingPanel.setShowTrail(false);
      else {
        orbDrawingPanel.setShowTrail(true);
      }
      orbDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Show scale")){
      if(orbDrawingPanel.getShowScale()) orbDrawingPanel.setShowScale(false);
      else {
        orbDrawingPanel.setShowScale(true);
      }
      orbDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Show ring")){
      if(orbDrawingPanel.getShowRing()) orbDrawingPanel.setShowRing(false);
      else {
        orbDrawingPanel.setShowRing(true);
      }
      orbDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Inward")){
      orbit.getIC().setInward();
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Outward")){
      orbit.getIC().setOutward();
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Set to stable circular orbit")){
      if(!potDrawingPanel.adjustICToCircStable()) {
        JOptionPane.showMessageDialog(this, "The effective potential has no local minimum on the diagram!\nOrbiter can not be set to stable circular orbit!",
            "Non existing stable orbit",
            JOptionPane.WARNING_MESSAGE);
      }
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      icInspector.repaint();
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Set to unstable circular orbit")){
      if(!potDrawingPanel.adjustICToCircUnstable()) {
        JOptionPane.showMessageDialog(this, "The effective potential has no local maximum on the diagram!\nOrbiter can not be set to unstable circular orbit!",
            "Non existing unstable orbit",
            JOptionPane.WARNING_MESSAGE);
      }
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      icInspector.repaint();
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Set to unstable counterclockwise circular orbit")){
      if(!potDrawingPanel.adjustICToPosCircUnstable()) {
        JOptionPane.showMessageDialog(this, "The upper effective potential has no local maximum on the diagram!\nPhoton can not be set to unstable counterclockwise circular orbit!",
            "Non existing counterclockwise unstable circular orbit",
            JOptionPane.WARNING_MESSAGE);
      }
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      icInspector.repaint();
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Set to unstable clockwise circular orbit")){
      if(!potDrawingPanel.adjustICToNegCircUnstable()) {
        JOptionPane.showMessageDialog(this, "The lower effective potential has no local minimum on the diagram!\nPhoton can not be set to unstable clockwise circular orbit!",
            "Non existing clockwise unstable circular orbit",
            JOptionPane.WARNING_MESSAGE);
      }
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      icInspector.repaint();
      odInspector.repaint();
      orbDrawingPanel.repaint();
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("Bookkeeper m>0")){
      orbit = new OrbitBoyerLindquistM();
      prepareLayout(orbit);
    }
    else if(e.getActionCommand().equals("Bookkeeper m=0")){
      orbit = new OrbitBoyerLindquistL();
      prepareLayout(orbit);
    }
    else if(e.getActionCommand().equals("Rain m>0")){
      orbit = new OrbitRainM();
      prepareLayout(orbit);
    }
    else if(e.getActionCommand().equals("Rain m=0")){
      orbit = new OrbitRainL();
      prepareLayout(orbit);
    }
    else if(e.getActionCommand().equals("Newton")){
      orbit = new OrbitNewton();
      prepareLayout(orbit);
      potDrawingPanel.repaint();
    }
    else if(e.getActionCommand().equals("About this program")){
      //after selecting menu About->About this program
      JOptionPane.showMessageDialog(this, "This software in intended to be used to accompany\nthe text written by Edwin F. Taylor and John A. Wheeler,\n"+
      "EXPLORING BLACK HOLES.\n\nBased on a program by Adam G. Riess and Edwin F. Taylor.\n"+""
      		+ "Professional advisor: Edmund Bertschinger of MIT.\n\n"
      +"Java version:\n(c) Slavom\u00EDr Tuleja, Tom\u00E1\u0161 Je\u017Eo, and Jozef Han\u010D\n"+
      "JavaScript version:\n(c) Developed by Wolfgang Christian and Robert Hanson using the SwingJS transpiler.\n\n"+
      "Please send any comments to:\ntuleja@stonline.sk\n"+
      "Please send JavaScript version comments to:\nwochristian@davidson.edu\n\n"+
      "This program uses classes from Open Source Physics (OSP) Project\nwww.opensourcephysics.org\noriginated by Wolfgang Christian et al.\n\n"+
      "The program is published under the GNU GPL licence.\n\n"+
      "OSP JavaScript Version released January 21, 2020",
          "About this program",
          JOptionPane.INFORMATION_MESSAGE);
      repaint();
    }
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    if(evt.getPropertyName().equals("slLmChange")){
      orbit.getIC().setLm(sliderControls.getLm());
      orbit.reset();
      
      if(menuItemFullOrbitPlot.isSelected()){
        orbit.reset();
        orbit.computeOrbitAtOnce();
        potDrawingPanel.repaint();
      }
      
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.repaint();
      if(!orbit.twoPotentials) potDrawingPanel.rescale(); //Do it only for material particles
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("slBChange")){
      orbit.getIC().setEffPotParameter(1.0/sliderControls.getB());
      orbit.reset();
      sliderControls.repaint();
      
      
      if(menuItemFullOrbitPlot.isSelected()){
        orbit.reset();
        orbit.computeOrbitAtOnce();
        potDrawingPanel.repaint();
      }
      
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.repaint();
      if(!orbit.twoPotentials) potDrawingPanel.rescale(); //Do it only for material particles
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("ICChange")){//when b slider value is illegal
      if(orbit.twoPotentials) {
        sliderControls.setB(1.0/orbit.getIC().getEffPotParameter());
        sliderControls.repaint();
      }
    }
    else if(evt.getPropertyName().equals("slAChange")){
      orbit.getIC().setA(sliderControls.getA());
      orbit.reset();
      
      if(menuItemFullOrbitPlot.isSelected()){
        orbit.reset();
        orbit.computeOrbitAtOnce();
        potDrawingPanel.repaint();
      }
      
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.repaint();
      if(!orbit.twoPotentials) potDrawingPanel.rescale(); //Do it only for material particles
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeEm")){
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      sliderControls.repaint();
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbDrawingPanel.repaint();
      //potDrawingPanel.rescale();
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeLm")){
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      sliderControls.setLm(orbit.getIC().getLm());
      sliderControls.repaint();
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbDrawingPanel.repaint();
      if(!orbit.twoPotentials) potDrawingPanel.rescale(); //Do it only for material particles
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeA")){
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      sliderControls.setA(orbit.getIC().getA());
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      sliderControls.repaint();
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbDrawingPanel.repaint();
      potDrawingPanel.rescale();
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeR")){
      orbit.reset();
      orbit.adjustDTAutomatically();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      sliderControls.repaint();
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbDrawingPanel.repaint();
      //potDrawingPanel.rescale();
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeDT")){
      orbit.getODESolver().initialize(orbit.getIC().getDT());
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbDrawingPanel.repaint();
      //potDrawingPanel.rescale();
      potDrawingPanel.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeV0")){
      orbit.getIC().adjustEmLmSign();
      sliderControls.setLm(orbit.getIC().getLm());
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.setRMax(rMaxOld);
      orbDrawingPanel.repaint();
      potDrawingPanel.rescale();
      potDrawingPanel.repaint();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeTheta0")){
      orbit.getIC().adjustEmLmSign();
      sliderControls.setLm(orbit.getIC().getLm());
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      sliderControls.repaint();
      
      if(orbit.getIC().getSign()==1) menuItemOutward.setSelected(true);
      else menuItemInward.setSelected(true);
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.setRMax(rMaxOld);
      orbDrawingPanel.repaint();
      //potDrawingPanel.rescale();
      potDrawingPanel.repaint();
    }
    else if(evt.getPropertyName().equals("icInspectorChangeNumPoints")){
      orbit.getIC().setNumPoints();
      odInspector.setTableModel();
      orbit.reset();
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      odInspector.repaint();
      icInspector.repaint();
      orbDrawingPanel.setRMax(rMaxOld);
      orbDrawingPanel.repaint();
      //potDrawingPanel.rescale();
      potDrawingPanel.repaint();
    }
    else if(evt.getPropertyName().equals("effPotMouseChange")){
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      orbDrawingPanel.setRMax2(orbit.getIC().getR());
      orbit.adjustDTAutomatically();
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      if(menuItemFullOrbitPlot.isSelected()){
        orbit.reset();
        orbit.computeOrbitAtOnce();
        potDrawingPanel.repaint();
      }
      
      orbDrawingPanel.repaint();
      icInspector.repaint();
      odInspector.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("effPotMouseXResize")){
      double rMax = potDrawingPanel.getRMax();
      orbDrawingPanel.setRMax(rMax);
      orbDrawingPanel.repaint();
      potDrawingPanel.setPreferredMinMaxX(0,rMax);
      potDrawingPanel.repaint();
      icInspector.repaint();
      odInspector.repaint();
      rMaxOld=orbDrawingPanel.getRMax();
    }
    else if(evt.getPropertyName().equals("orbMouseChange")){
      if(menuItemFullOrbitPlot.isSelected()){
        orbit.reset();
        orbit.computeOrbitAtOnce();
        potDrawingPanel.repaint();
      }
      if(orbit.twoPotentials) sliderControls.setB(1.0/orbit.getIC().getInvB());
      
      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
      potDrawingPanel.repaint();
      icInspector.repaint();
      odInspector.repaint();
      sliderControls.setLm(orbit.getIC().getLm());
      sliderControls.repaint();
      if(orbit.getIC().getSign()==1) menuItemOutward.setSelected(true);
      else menuItemInward.setSelected(true);
      
      lblComment.setText("Drag arrowhead direction. Drag length by holding ALT or OPTION.");
      
    }
    else if(evt.getPropertyName().equals("orbMouseEntered")){
      lblComment.setText("Drag arrowhead direction. Drag length by holding ALT or OPTION.");
    }
    else if(evt.getPropertyName().equals("orbMouseExited")){
      lblComment.setText(" ");
    }
    else if(evt.getPropertyName().equals("potMouseEntered")){
      lblComment.setText("Drag orbiter black dot to change initial conditions. Drag the plot to rescale horizontally. Drag the plot by holding ALT or OPTION to rescale vertically. Drag near right edge to shift the plot vertically.");
      //Click near the upper or lower edge to shift the plot vertically.
    }
    else if(evt.getPropertyName().equals("potMouseExited")){
      lblComment.setText(" ");
    }
    else if(evt.getPropertyName().equals("slidersMouseEntered")){
      lblComment.setText("Change the J/M parameter of the black hole or change the L/m of the orbiter.");
    }
    else if(evt.getPropertyName().equals("slidersMouseExited")){
      lblComment.setText(" ");
    }
    else if(evt.getPropertyName().equals("icMouseEntered")){
      lblComment.setText("Input numerical values of parameters.");
    }
    else if(evt.getPropertyName().equals("icMouseExited")){
      lblComment.setText(" ");
    }
    else if(evt.getPropertyName().equals("odMouseEntered")){
      lblComment.setText("To copy the data to a spreadsheet (e.g. Excel) highlight the relevant cells and press CRTL-C.");
    }
    else if(evt.getPropertyName().equals("odMouseExited")){
      lblComment.setText(" ");
    }
    else if(evt.getPropertyName().equals("numConvException")){
      stopAnimation();
      /*JOptionPane.showMessageDialog(this,
          "Numerical method failed to converge...",
          "Loss of precision warning",
          JOptionPane.WARNING_MESSAGE);
          */
      btnStartStop.setText("Start");
      btnStartStop.setEnabled(false);
      btnStartStop.setIcon(iconPlay);
      menuProgramMode.setEnabled(true);
      menuInitial.setEnabled(true);
      menuZoom.setEnabled(true);
      menuAbout.setEnabled(true);
      icInspector.table.setEnabled(true);
      odInspector.table.setEnabled(true);
      btnStepForward.setEnabled(false);
      btnStepBack.setEnabled(false);
      btnReset.setEnabled(true);
      sliderControls.slA.setEnabled(true);
      sliderControls.slLm.setEnabled(true);
      
      splitPane.setDividerLocation(0.75);
      lblComment.setForeground(Color.red);
      lblComment.setText(" ");
      
      odInspector.repaint();
    }
    
  }
  
  
  /**
   * startAnimation
   */
  public void startAnimation() {
    if (animationThread != null)
      return; //already running
    animationThread = new Thread(this);
    animationThread.start(); 
    potDrawingPanel.setStopped(false);
    orbDrawingPanel.setStopped(false);
    sliderControls.setStopped(false);
    icInspector.setStopped(false);
    odInspector.setStopped(false);
  }
  
  /**
   * stopAnimation
   */
  public void stopAnimation() {
    Thread tempThread = animationThread; //temporary reference
    animationThread = null; //signal the animation to stop
    if (tempThread != null) {
      try {
        tempThread.interrupt(); //get out of the sleep state
        tempThread.join(); //wait for the thread to die
      } catch (InterruptedException e) {
      }
    }
    potDrawingPanel.setStopped(true);
    orbDrawingPanel.setStopped(true);
    sliderControls.setStopped(true);
    icInspector.setStopped(true);
    odInspector.setStopped(true);
  }
  
  
  // BH switching from while/sleep loop to state loop. 
  
	private final static int STATE_INIT = 0;
	private final static int STATE_LOOP = 1;
	private final static int STATE_DONE = 2;

	private StateHelper stateHelper;
	private int frameDelay = (/**@j2sNative 2 || */20);

	@Override
	public boolean stateLoop() {
		while (animationThread != null 
				&& !animationThread.isInterrupted() 
				&& stateHelper.isAlive()) {
			switch (stateHelper.getState()) {
			default:
			case STATE_INIT:
				stateHelper.setState(STATE_LOOP);
				stateHelper.sleep(frameDelay);
				return true;
			case STATE_LOOP:
		        orbit.doStep();
		        if (menuItemAutoZoom.isSelected()) adjustZoom();
		        pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
		        
		        orbDrawingPanel.repaint();
//		        odInspector.repaint();
		        potDrawingPanel.repaint();
				stateHelper.sleep(frameDelay );
				return true;
			case STATE_DONE:
		        odInspector.repaint();
				return false;
			}
		}
		return false;
	}
    
  public void run() {
	stateHelper = new SwingJSUtils.StateHelper(this);  
	stateHelper.setState(STATE_INIT);
	stateHelper.sleep(0);
//	
//    while (animationThread == Thread.currentThread()) {
//      try {
//        Thread.sleep(20);
//        
//        orbit.doStep();
//      } catch (InterruptedException e) {
//        System.out.println("Interrupted...");
//      }
//      
//      if (menuItemAutoZoom.isSelected()) adjustZoom();
//      pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
//      
//      orbDrawingPanel.repaint();
//      odInspector.repaint();
//      potDrawingPanel.repaint();
//    }
    
  }
  
  public void adjustZoom() {
    double p = 0.2;
    double rMax = potDrawingPanel.getRMax();
    rMax = rMax - p * (0.9*rMax - orbit.getR());
    orbDrawingPanel.setRMax(rMax);
    potDrawingPanel.setRMax(rMax);
    potDrawingPanel.setPreferredMinMaxX(0,rMax);
  }
  
  
  public static void main(String[] args) {
    //Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);
    
    //Put icon containing image of black hole to the window.
    Image iLogo;
    java.net.URL imgURL =GRorbits2.class.getResource("/org/opensourcephysics/tuleja/images/black-hole.gif");
    if (imgURL != null) {
      iLogo = new ImageIcon(imgURL).getImage();
    } else {
      System.err.println("Couldn't find file.");
      iLogo = null;
    }
    
    JFrame frame = new JFrame("GRorbits");
    frame.setSize(1024, 633);
    frame.setIconImage(iLogo);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    GRorbits2 app = new GRorbits2();
    // BH was initialize(); just renamed
    app.init();
    // BH adding the applet content pane, not the applet itself
    // and then moving the menu. 
    frame.setContentPane(app.getContentPane());
    frame.setJMenuBar(app.mainMenu);
    frame.setVisible(true); 
  }

  
}


