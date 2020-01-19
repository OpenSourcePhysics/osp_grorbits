package org.opensourcephysics.tuleja.grorbits;


public abstract class InitialConditionsM extends InitialConditions{
  
  public InitialConditionsM(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=(a);
    icData[1][1]=(Em);
    icData[2][1]=(Lm);
    icData[3][1]=(r);
    icData[6][1]=(dt);
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsM(Orbit orbit, double a, double r, double v0, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=(a);
    icData[3][1]=(r);
    icData[4][1]=(v0);
    icData[5][1]=(Math.toDegrees(theta0));
    icData[6][1]=(dt);
    adjustEmLmSign();
    
    computeInitialState();
  }
  
  private String[] labels = new String[] {
		    "J/M = a (units M)",
		    "E/m (units M)",
		    "L/m (units M)",
		    "r (units M)",
		    "v_ring",
		    "\u03b8_ring (units degrees)",
		    "dt (units M)",
		    "number of points"
  };
  
  public String getLabel(int i) {
	  return labels[i];
  }

  public void initializeVariables(){
    icData = new double[8][2];
    icData[0][0]=0;
    icData[1][0]=1;
    icData[2][0]=2;
    icData[3][0]=3;
    icData[4][0]=4;
    icData[5][0]=5;
    icData[6][0]=6;
    icData[7][0]=7;    
    icData[7][1]=orbit.getNumPoints();
  }
  
  public double getA(){
    return (icData[0][1]);
  }
  
  public double getEm(){
    return (icData[1][1]);
  }
  
  public double getLm(){
    return (icData[2][1]);
  }
  
  public double getEffPotParameter(){
    return getEm();
  }
  
  public String getEffPotParameterLabel(){
    return "E/m = ";
  }
  
  public String getEffPotParameterUnit(){
    return " M";
  }
  
  public double getR(){
    return (icData[3][1]);
  }
  
  public double getDT(){
    return (icData[6][1]);
  }
  
  public double getV0(){
    return (icData[4][1]);
  }
  
  public double getTheta0(){
    return Math.toRadians((icData[5][1]));
  }
  
  public void setA(double a){
    icData[0][1]=(a);
    orbit.reset();
  }
  
  public void setEm(double Em){
    icData[1][1]=(Em);
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setEm(value);
  }
  
  public void setLm(double Lm){
    icData[2][1]=(Lm);
    orbit.reset();
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-1) rr=1e-1;
    icData[3][1]=(rr);
    orbit.reset();
  }
  
  public void setDT(double dt){
    icData[6][1]=(dt);
    orbit.odeSolver.initialize(dt);
    orbit.reset();
  }
  
  public void setNumPoints(){
    orbit.numPoints=getNumPoints();
    orbit.orbitData = new double[orbit.numPoints][4];
    //initially we load initial condition to the orbitData array
    for(int i=0; i<orbit.numPoints; i++){
      orbit.orbitData[i][1]= (orbit.ic.getR());
    }
  }
  
  public void setV0(double v0){
    icData[4][1]=(v0);
    adjustEmLmSign();
    orbit.reset();
  }
  
  public void setTheta0(double theta0){
    icData[5][1]=(Math.toDegrees(theta0));
    adjustEmLmSign();
    orbit.reset();
  }
  
  public double getInvB() {
    //This has no meaning. It is here just because this extends InitialConditions
    return 0;
  }

  public void setInvB(double invB) {
    //do nothing here
  }
  
}
