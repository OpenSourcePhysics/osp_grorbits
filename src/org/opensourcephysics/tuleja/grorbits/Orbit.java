package org.opensourcephysics.tuleja.grorbits;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.event.SwingPropertyChangeSupport;
import org.opensourcephysics.tuleja.numerics.*;


public abstract class Orbit implements ODE{
  double[] state;
  int numPoints;
  InitialConditions ic;
  ODEAdaptiveSolver odeSolver;
  double t;
  PropertyChangeSupport support;
  double[][] orbitData;
  public boolean twoPotentials = false;
  
  
  public Orbit(){
    initializeVariables();
    
    support = new SwingPropertyChangeSupport(this);
    
    //inicialization of ODE Solver
    odeSolver = new RK45GRorbitsMultiStep(this);
    odeSolver.setTolerance(1e-6);
    //odeSolver.setMaximumNumberOfErrorMessages(1);
    //...error in one step
    odeSolver.initialize(ic.getDT());
    //...size of a fixed step
    
    reset();
  }
  
  public abstract void initializeVariables();
  
  public abstract void initialize(double a, double r, double v0, double theta0, double dt, int numPoints);
  
  
  /**
   * According to the initial position adjust the value of fixed dt for 
   * the entire orbit.
   *
   */
  public void adjustDTAutomatically(){
    double exp = Math.log(2*Math.PI*Math.pow(ic.getR(),1.5)/500)/Math.log(2);
    double dt=Math.pow(2,Math.floor(exp));
    ic.setDT(dt);
    odeSolver.initialize(ic.getDT());
  }
  
  public void reset(){
    //first check if effPotParamater (Em or invB) and r are OK
    double r = ic.getR();
    double param=ic.getEffPotParameter();
    if(r<getRHorizon()){
      ic.setR(getRHorizon()+1e-5);
    }
    
    if(!twoPotentials){
      if(param<=getVm(r)){
        ic.setEffPotParameter(getVm(r)+1e-15);
      }
    }
    else {//two potentials
      if(param<=getVm(r)&&param>0){
        ic.setEffPotParameter(getVm(r)+1e-15);
      }
      else if(param>=getVmLower(r) && param<0){
        ic.setEffPotParameter(getVmLower(r)-1e-15);
      }
    }
    
    ic.adjustV0Theta0();
    
    //recompute initial state
    ic.computeInitialState(); // here we compute dr/dt, dphi/dt,...
    
    //  initially we load initial condition to the orbitData array
    orbitData[0][0]= (0);
    orbitData[0][1]=(ic.getR());
    orbitData[0][2]= (0);
    orbitData[0][3]= (0);
    //we fill remaining rows with zeros 
    for(int i=1; i<numPoints; i++){
      orbitData[i][0]= (0);
      orbitData[i][1]= (ic.getR());
      orbitData[i][2]= (0);
      orbitData[i][3]= (0);
    }
    
    
  }
  
  
  
  
  /**
   * @param i
   * @return returns the time coordinate of the i-th event of the orbit
   */
  public double getT(int i) {
    return orbitData[i][0];
  }
  
  /**
   * @param i
   * @return returns the r coordinate of the i-th event of the orbit
   */
  public double getR(int i) {
    return orbitData[i][1];
  }
  
  /**
   * @param i
   * @return returns the phi coordinate of the i-th event of the orbit
   */
  public double getPhi(int i) {
    return orbitData[i][2];
  }
  
  /**
   * @param i
   * @return returns the tau coordinate of the i-th event of the orbit
   */
  public double getTau(int i) {
    return orbitData[i][3];
  }
  
  
  
  
  
  public void doStep(){
    
    //  makes a single step in calculation
    try {
      t += odeSolver.step();
    }
    catch (InsufficientConvergenceException e){
      support.firePropertyChange("numConvException",null,null);
    }  
    
    
    for (int i = numPoints-2; i >=0; i--) {
      orbitData[i+1][0] = orbitData[i][0];
      orbitData[i+1][1] = orbitData[i][1];
      orbitData[i+1][2] = orbitData[i][2];
      orbitData[i+1][3] = orbitData[i][3];
    }
    
    if(getT()>0){
      orbitData[0][0] = (getT());
      orbitData[0][1] = (getR());
      orbitData[0][2] = (getPhi());
      orbitData[0][3] = (getTau());
    }
    else{
      orbitData[0][0]= (0);
      orbitData[0][1]= (ic.getR());
      orbitData[0][2]= (0);
      orbitData[0][3]= (0);
    }
    
  }
  
  public void doStepBack(){

    //shift all the data 1 place to lower indexes
    for (int i = 1; i <numPoints; i++) {
      orbitData[i-1][0] = orbitData[i][0];
      orbitData[i-1][1] = orbitData[i][1];
      orbitData[i-1][2] = orbitData[i][2];
      orbitData[i-1][3] = orbitData[i][3];
    }
    
    //  change dt to -dt
    odeSolver.initialize(-odeSolver.getStepSize());
    
    //  makes numPoints  steps in calculation backwards
    for(int i=0; i<numPoints;i++){
      try{
        t += odeSolver.step();
      }
      catch (InsufficientConvergenceException e){
        support.firePropertyChange("numConvException",null,null);
      }
    }
    
    if(getT()>0){
      orbitData[numPoints-1][0] = (getT());
      orbitData[numPoints-1][1] = (getR());
      orbitData[numPoints-1][2] = (getPhi());
      orbitData[numPoints-1][3] = (getTau());
    }
    else {
      orbitData[numPoints-1][0]= (0);
      orbitData[numPoints-1][1]= (ic.getR());
      orbitData[numPoints-1][2]= (0);
      orbitData[numPoints-1][3]= (0);
    }
    
    //  again change dt to -dt
    odeSolver.initialize(-odeSolver.getStepSize());
    
    //  makes numPoints-1  steps in calculation forward
    for(int i=0; i<numPoints-1;i++){
      try{
      t += odeSolver.step();
      }
      catch(InsufficientConvergenceException e){
        support.firePropertyChange("numConvException",null,null);
      }
    }
    
    
  }
  
  public synchronized void computeOrbitAtOnce(){
    for(int k=0; k<numPoints; k++){
      orbitData[numPoints-k-1][0] = (getT());
      orbitData[numPoints-k-1][1] = (getR());
      orbitData[numPoints-k-1][2] = (getPhi());
      orbitData[numPoints-k-1][3] = (getTau());
      // makes a single step in calculation
      
      
      try{
        t += odeSolver.step();
      }
      catch(InsufficientConvergenceException e){
        support.firePropertyChange("numConvException",null,null);
        for(int j=k+1; j<numPoints;j++){
          orbitData[numPoints-j-1][0] = (getT());
          orbitData[numPoints-j-1][1] = (getR());
          orbitData[numPoints-j-1][2] = (getPhi());
          orbitData[numPoints-j-1][3] = (getTau());
        }
        break;
      }
      
    }
  }
  
  
  
  public void addPropertyChangeListener(PropertyChangeListener listener){
    support.addPropertyChangeListener(listener);
  }
  
  public abstract void getRate(double[] state, double[] rates);
  
  public double[] getState() {
    return state;
  }
  
  public InitialConditions getIC(){
    return ic;
  }
  
  public int getNumPoints(){
    return numPoints;
  }
  
  public ODEAdaptiveSolver getODESolver(){
    return odeSolver;
  }
  
  public double[][] getOrbitData(){
    return orbitData;
  }
  
  public double rTorPlot(double r){
    double a = ic.getA();
    return Math.sqrt(r*r+a*a);
  }
  
  public double rPlotTor(double rPlot){
    double a = ic.getA();
    if(rPlot*rPlot>a*a) return Math.sqrt(rPlot*rPlot-a*a);
    else return 0;
  }
  
  public double getRHorizon(){
    double a = ic.getA();
    return (1+Math.sqrt(1-a*a));
  }
  
  public double getRInnerHorizon(){
    double a = ic.getA();
    return 1-Math.sqrt(1-a*a);
  }
  
  public abstract double getRingAngle();
  
  public abstract double getR();
  
  public abstract double getPhi();
  
  public abstract double getTau();
  
  public abstract double getT();
  
  public abstract void setT(double t);
  
  public abstract double getVm(double r);
  
  public abstract double getVmLower(double r);
  
  public abstract double getVmAtHorizon();
  
  
}

