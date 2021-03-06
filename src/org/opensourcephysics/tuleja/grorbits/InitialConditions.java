package org.opensourcephysics.tuleja.grorbits;


public abstract class InitialConditions{
  double sign;
  double[][] icData ;
  Orbit orbit;
  
  public InitialConditions(Orbit orbit){
    
  }
  
  
  public abstract void initializeVariables();
  
  
  public double[][] getICData(){
    return icData;
  }
  
  
  public abstract String getLabel(int i);
  
  
  /**
   * According to known values of initial r, phi, Em, Lm, and sign 
   * it adjusts values of v0 and theta0  
   *
   */
  public abstract void adjustV0Theta0();
  
  /**
   * According to known values of initial r, phi, v0, theta0 
   * it adjusts values of Em, Lm, and sign  
   *
   */
  public abstract void adjustEmLmSign();
  
  
  
  
  /**
   * Sets and computes initial conditions r, dr/dt, phi, dphi/dt from
   * given vlaues of energy Em and angular momentum Lm. It sets initial values to
   * state[]
   */
  public abstract void computeInitialState();
  
  public boolean isInward(){
    if(sign==1) return false;
    else return true;
  }
  
  public boolean isOutward(){
    if(sign==-1) return false;
    else return true;
  }
  
  public void setInward(){
    sign=-1;
  }
  
  public void setOutward(){
    sign=1;
  }
  
  public abstract double getA();
  
  public abstract double getEm();
  
  public abstract double getLm();
  
  public abstract double getInvB();
  
  public abstract double getEffPotParameter();
  
  public abstract double getR();
  
  public abstract double getV0();
  
  public abstract double getDT();
  
  public abstract double getTheta0();
  
  public int getNumPoints(){
	return (int) icData[icData.length - 1][1];
  }	  

  public double getSign(){
    return sign;
  }
  
  public abstract String getEffPotParameterLabel();
  
  public abstract String getEffPotParameterUnit();
  
  public abstract void setA(double a);
  
  public abstract void setEm(double Em);
  
  public abstract void setLm(double Lm);
  
  public abstract void setInvB(double invB);
  
  public abstract void setEffPotParameter(double value);
  
  public abstract void setR(double r);
  
  public abstract void setV0(double v0);
  
  public abstract void setDT(double dt);
  
  public abstract void setNumPoints();
  
  public void setSign(int sign){
    this.sign=sign;
    orbit.reset();
  }
  
  public abstract void setTheta0(double theta0);
}
