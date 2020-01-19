package org.opensourcephysics.tuleja.grorbits;


public abstract class InitialConditionsL extends InitialConditions{
  double sign;
  Object[][] icData ;
  Orbit orbit;
  
  public InitialConditionsL(Orbit orbit, double a, double invB, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=new Double(a);
    icData[1][1]=new Double(invB);
    icData[2][1]=new Double(r);
    icData[4][1]=new Double(dt);
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsL(Orbit orbit, double a, double r, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[0][1]=new Double(a);
    icData[2][1]=new Double(r);
    icData[4][1]=new Double(dt);
    icData[3][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    
    computeInitialState();
  }
  
  public void initializeVariables(){
    icData = new Object[6][2];
    icData[0][0]="J/M = a (units M)";
    icData[1][0]="1/b (units 1/M)";
    icData[2][0]="r (units M)";
    icData[3][0]="\u03b8_ring (units degrees)";
    icData[4][0]="dt (units M)";
    icData[5][0]="number of points";
    
    icData[5][1]=new Integer(orbit.getNumPoints());
  }
  
  public Object[][] getICData(){
    return icData;
  }
  
  
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
  
  public double getA(){
    return ((Double)icData[0][1]).doubleValue();
  }
  
  public double getInvB(){
    return ((Double)icData[1][1]).doubleValue();
  }

  public double getEffPotParameter(){
    return getInvB();
  }
  
  public String getEffPotParameterLabel(){
    return "M/b = ";
  }
  
  public String getEffPotParameterUnit(){
    return " ";
  }
  
  public double getR(){
    return ((Double)icData[2][1]).doubleValue();
  }
  
  public double getDT(){
    return ((Double)icData[4][1]).doubleValue();
  }
  
  public double getTheta0(){
    return Math.toRadians(((Double)icData[3][1]).doubleValue());
  }
  
  public int getNumPoints(){
    return ((Number)icData[5][1]).intValue();
  }
  
  public double getSign(){
    return sign;
  }
  
  public void setA(double a){
    icData[0][1]=new Double(a);
    orbit.reset();
  }
  
  public void setInvB(double invB){
    icData[1][1]=new Double(invB);
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setInvB(value);
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-1) rr=1e-1;
    icData[2][1]=new Double(rr);
    orbit.reset();
  }
  
  public void setDT(double dt){
    icData[4][1]=new Double(dt);
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
  
  public void setSign(int sign){
    this.sign=sign;
    orbit.reset();
  }
  
  public void setTheta0(double theta0){
    icData[3][1]=new Double(Math.toDegrees(theta0));
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
