package org.opensourcephysics.tuleja.grorbits;


public abstract class InitialConditionsM extends InitialConditions{
  double sign;
  Object[][] icData ;
  Orbit orbit;
  
  public InitialConditionsM(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[1][1]=new Double(Em);
    icData[2][1]=new Double(Lm);
    icData[3][1]=new Double(r);
    icData[0][1]=new Double(a);
    icData[6][1]=new Double(dt);
    this.sign=sign;
    
    adjustV0Theta0();
    computeInitialState();
  }
  
  public InitialConditionsM(Orbit orbit, double a, double r, double v0, double theta0, double dt){
    super(orbit);
    this.orbit=orbit;
    initializeVariables();
    icData[3][1]=new Double(r);
    icData[0][1]=new Double(a);
    icData[6][1]=new Double(dt);
    icData[4][1]=new Double(v0);
    icData[5][1]=new Double(Math.toDegrees(theta0));
    adjustEmLmSign();
    
    computeInitialState();
  }
  
  public void initializeVariables(){
    icData = new Object[8][2];
    icData[0][0]="J/M = a (units M)";
    icData[1][0]="E/m (units M)";
    icData[2][0]="L/m (units M)";
    icData[3][0]="r (units M)";
    icData[4][0]="v_ring";
    icData[5][0]="\u03b8_ring (units degrees)";
    icData[6][0]="dt (units M)";
    icData[7][0]="number of points";
    
    icData[7][1]=new Integer(orbit.getNumPoints());
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
  
  public double getEm(){
    return ((Double)icData[1][1]).doubleValue();
  }
  
  public double getLm(){
    return ((Double)icData[2][1]).doubleValue();
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
    return ((Double)icData[3][1]).doubleValue();
  }
  
  public double getDT(){
    return ((Double)icData[6][1]).doubleValue();
  }
  
  public double getV0(){
    return ((Double)icData[4][1]).doubleValue();
  }
  
  public double getTheta0(){
    return Math.toRadians(((Double)icData[5][1]).doubleValue());
  }
  
  public int getNumPoints(){
    //return Math.round((float)((Double)icData[7][1]).doubleValue());
    return ((Number)icData[7][1]).intValue();
  }
  
  public double getSign(){
    return sign;
  }
  
  public void setA(double a){
    icData[0][1]=new Double(a);
    orbit.reset();
  }
  
  public void setEm(double Em){
    icData[1][1]=new Double(Em);
    orbit.reset();
  }
  
  public void setEffPotParameter(double value){
    setEm(value);
  }
  
  public void setLm(double Lm){
    icData[2][1]=new Double(Lm);
    orbit.reset();
  }
  
  public void setR(double r){
    double rr=r;
    if(rr<1e-1) rr=1e-1;
    icData[3][1]=new Double(rr);
    orbit.reset();
  }
  
  public void setDT(double dt){
    icData[6][1]=new Double(dt);
    orbit.odeSolver.initialize(dt);
    orbit.reset();
  }
  
  public void setNumPoints(){
    orbit.numPoints=getNumPoints();
    orbit.orbitData = new Double[orbit.numPoints][4];
    //initially we load initial condition to the orbitData array
    for(int i=0; i<orbit.numPoints; i++){
      orbit.orbitData[i][0]= new Double(0);
      orbit.orbitData[i][1]= new Double(orbit.ic.getR());
      orbit.orbitData[i][2]= new Double(0);
      orbit.orbitData[i][3]= new Double(0);
    }
  }
  
  public void setSign(int sign){
    this.sign=sign;
    orbit.reset();
  }
  
  public void setV0(double v0){
    icData[4][1]=new Double(v0);
    adjustEmLmSign();
    orbit.reset();
  }
  
  public void setTheta0(double theta0){
    icData[5][1]=new Double(Math.toDegrees(theta0));
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
