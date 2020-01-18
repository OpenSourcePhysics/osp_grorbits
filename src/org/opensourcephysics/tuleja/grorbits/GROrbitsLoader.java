package org.opensourcephysics.tuleja.grorbits;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * An xml loader for the GROrbitsApp program.
 *
 * @author W. Christian
 */
public class GROrbitsLoader implements XML.ObjectLoader {
  /**
   * Creates a new GROrbitsApp.
   *
   * @param control XMLControl
   * @return Object
   */
  public Object createObject(XMLControl control) {
    
    GROrbitsApp model = new GROrbitsApp(null);
    return model;
  }
  
  /**
   * Saves the GROrbitsApp's data in the xml control.
   *
   * @param control XMLControl
   * @param obj Object
   */
  public void saveObject(XMLControl control, Object obj) {
    
    GROrbitsApp model = (GROrbitsApp) obj;
    
    control.setValue("time plot",model.menuItemTimePlot.isSelected());
    control.setValue("full orbit plot",model.menuItemFullOrbitPlot.isSelected());
    
    
    if (model.orbit instanceof OrbitNewton) {
      control.setValue("orbit", "newton");
    }
    else if (model.orbit instanceof OrbitRainM) {
      control.setValue("orbit", "rain m>0");
    }
    else if (model.orbit instanceof OrbitRainL) {
      control.setValue("orbit", "rain m=0");
    }
    else if (model.orbit instanceof OrbitBoyerLindquistM) {
      control.setValue("orbit", "bookkeeper m>0");
    }
    else if (model.orbit instanceof OrbitBoyerLindquistL) {
      control.setValue("orbit", "bookkeeper m=0");
    }
    
    control.setValue("show_grid",model.menuItemGrid.isSelected());
    control.setValue("show_scale",model.menuItemScale.isSelected());
    control.setValue("show_trail",model.menuItemTrail.isSelected());
    control.setValue("show_ring",model.menuItemRing.isSelected());
    
    control.setValue("inward",model.menuItemInward.isSelected());
    control.setValue("outward",model.menuItemOutward.isSelected());
    
    control.setValue("autozoom",model.menuItemAutoZoom.isSelected());
    
    control.setValue("a", model.orbit.ic.getA());
    control.setValue("r", model.orbit.ic.getR());
    control.setValue("v0", model.orbit.ic.getV0());
    control.setValue("theta0", model.orbit.ic.getTheta0());
    control.setValue("dt", model.orbit.ic.getDT());
    control.setValue("numPoints", model.orbit.ic.getNumPoints());
    
    control.setValue("rMax",model.potDrawingPanel.getRMax());
    control.setValue("EffPotYMin",model.potDrawingPanel.sy1);
    control.setValue("EffPotYMax",model.potDrawingPanel.sy2);
  }
  
  /**
   * Loads the GROrbitsApp with data from the xml control.
   *
   * @param control XMLControl
   * @param obj Object
   * @return Object
   */
  public Object loadObject(XMLControl control, Object obj) {
    
    GROrbitsApp model = (GROrbitsApp) obj;
      
    model.menuItemNewton.setSelected(false);
    model.menuItemRainM.setSelected(false);
    model.menuItemRainL.setSelected(false);
    model.menuItemBoyerLindquistM.setSelected(false);
    model.menuItemBoyerLindquistL.setSelected(false);
    String orbit = control.getString("orbit");
    if (orbit.equals("newton")) {
      model.menuItemNewton.setSelected(true);
      model.orbit = new OrbitNewton();
    }
    else if (orbit.equals("rain m>0")) {
      model.menuItemRainM.setSelected(true);
      model.orbit = new OrbitRainM();
    }
    else if (orbit.equals("rain m=0")) {
      model.menuItemRainL.setSelected(true);
      model.orbit = new OrbitRainL();
    }
    else if (orbit.equals("bookkeeper m>0")) {
      model.menuItemBoyerLindquistM.setSelected(true);
      model.orbit = new OrbitBoyerLindquistM();
    }
    else if (orbit.equals("bookkeeper m=0")) {
      model.menuItemBoyerLindquistL.setSelected(true);
      model.orbit = new OrbitBoyerLindquistL();
    }
    
    double a = control.getDouble("a");
    double r = control.getDouble("r");
    double v0 = control.getDouble("v0");
    double theta0 = control.getDouble("theta0");
    double dt = control.getDouble("dt");
    int numPoints = control.getInt("numPoints");
    
    model.orbit.initialize(a, r, v0, theta0, dt, numPoints);
    
    double Lm=model.orbit.getIC().getLm();
    double invB=model.orbit.getIC().getInvB();
    
    double rMax = control.getDouble("rMax");
    double yMin = control.getDouble("EffPotYMin");
    double yMax = control.getDouble("EffPotYMax");
    
    model.menuItemTimePlot.setSelected(control.getBoolean("time plot"));
    model.menuItemFullOrbitPlot.setSelected(control.getBoolean("full orbit plot"));
    
    model.prepareLayout(model.orbit);
    
    model.orbDrawingPanel.setShowGrid(control.getBoolean("show_grid"));
    model.menuItemGrid.setSelected(control.getBoolean("show_grid"));
    
    model.orbDrawingPanel.setShowScale(control.getBoolean("show_scale"));
    model.menuItemScale.setSelected(control.getBoolean("show_scale"));
    
    model.orbDrawingPanel.setShowTrail(control.getBoolean("show_trail"));
    model.menuItemTrail.setSelected(control.getBoolean("show_trail"));
    
    model.orbDrawingPanel.setShowRing(control.getBoolean("show_ring"));
    model.menuItemRing.setSelected(control.getBoolean("show_ring"));
    
    model.menuItemInward.setSelected(control.getBoolean("inward"));
    model.menuItemOutward.setSelected(control.getBoolean("outward"));
    
    model.menuItemAutoZoom.setSelected(control.getBoolean("autozoom"));
    
    
    model.potDrawingPanel.setRMax(rMax);
    model.potDrawingPanel.setPreferredMinMaxX(0,rMax);
    model.potDrawingPanel.setPreferredMinMaxY(yMin,yMax);
    
    model.orbDrawingPanel.setRMax(rMax);
    model.sliderControls.setA(a);
    if(model.orbit.twoPotentials){ 
      model.sliderControls.setB(1/invB);
    }
    else{
      model.sliderControls.setLm(Lm);
    }
    
    model.potDrawingPanel.repaint();
    model.sliderControls.repaint();
    model.orbDrawingPanel.repaint();
    model.icInspector.repaint();
    model.odInspector.repaint();
    
    
    
    return obj;
  }
}
