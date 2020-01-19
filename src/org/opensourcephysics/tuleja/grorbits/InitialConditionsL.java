package org.opensourcephysics.tuleja.grorbits;


public abstract class InitialConditionsL extends InitialConditions{
  
  public InitialConditionsL(Orbit orbit, double a, double invB, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=(a);
    icData[1][1]=(invB);
    icData[2][1]=(r);
    icData[4][1]=(dt);
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsL(Orbit orbit, double a, double r, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=(a);
    icData[2][1]=(r);
    icData[4][1]=(dt);
    icData[3][1]=(Math.toDegrees(theta0));
    adjustEmLmSign();
    
    computeInitialState();
  }
  
  private String[] labels = new String[] {
		  "J/M = a (units M)",
		  "1/b (units 1/M)",
		  "r (units M)",
		  "\u03b8_ring (units degrees)",
		  "dt (units M)",
		  "number of points"
  };
  
  public String getLabel(int i) {
	  return labels[i];
  }
  
  public void initializeVariables(){
    icData = new double[6][2];
    icData[0][0]=0;
    icData[1][0]=1;
    icData[2][0]=2;
    icData[3][0]=3;
    icData[4][0]=4;
    icData[5][0]=5;
    
    icData[5][1]=orbit.getNumPoints();
  }
  
//  public boolean isInward(){
//    if(sign==1) return false;
//    else return true;
//  }
//  
//  public boolean isOutward(){
//    if(sign==-1) return false;
//    else return true;
//  }
//  
//  public void setInward(){
//    sign=-1;
//  }
//  
//  public void setOutward(){
//    sign=1;
//  }
//  
  public double getEffPotParameter(){
    return getInvB();
  }
  
  public String getEffPotParameterLabel(){
    return "M/b = ";
  }
  
  public String getEffPotParameterUnit(){
    return " ";
  }
  
	public double getA() {
		return (icData[0][1]);
	}
	  
	public double getInvB() {
		return (icData[1][1]);
	}

  public double getR(){
    return (icData[2][1]);
  }
  
  public double getDT(){
    return (icData[4][1]);
  }
  
  public double getTheta0(){
    return Math.toRadians((icData[3][1]));
  }
  
  public void setA(double a){
    icData[0][1]=(a);
    orbit.reset();
  }
  
  public void setInvB(double invB){
    icData[1][1]=(invB);
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setInvB(value);
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-1) rr=1e-1;
    icData[2][1]=(rr);
    orbit.reset();
  }
  
  public void setDT(double dt){
    icData[4][1]=(dt);
    orbit.odeSolver.initialize(dt);
    orbit.reset();
  }
  
  public void setNumPoints(){
    orbit.numPoints=getNumPoints();
    orbit.orbitData = new double[orbit.numPoints][4];
    //initially we load initial condition to the orbitData array
    for(int i=0; i<orbit.numPoints; i++){
      orbit.orbitData[i][1]= orbit.ic.getR();
    }
  }
  
  public void setTheta0(double theta0){
    icData[3][1]=(Math.toDegrees(theta0));
    adjustEmLmSign();
    orbit.reset();
  }
  
  public double getEm() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public double getLm() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public double getV0() {
    return 1;
  }

  public void setEm(double Em) {
    //  do nothing here
    
  }

  public void setLm(double Lm) {
    // do nothing here
    
  }

  public void setV0(double v0) {
    // do nothing here
    
  }
  
}
