package org.opensourcephysics.tuleja.grorbits;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;


public class OrbitDrawingPanel extends DrawingPanel implements MouseListener, MouseMotionListener{
  Orbit orbit;
  // BH can't do this - too late in SwingJS
  //PropertyChangeSupport support = new SwingPropertyChangeSupport(this);
  double gridDivision=2;
  boolean cursorOver=false, isStopped=true;
  int curI, curJ;
  boolean showGrid=true, showTrail=true, showScale=true, showRing=true, arrowDragged=true;
  boolean showInitialArrow=true;

  private final static Color cWithinHorizon = new Color(255, 255, 127);
  private final static Color cGrey200 = new Color(200, 200, 200);
  private final static Color cGrey247 = new Color(247, 247, 247);
  
  public OrbitDrawingPanel(Orbit orbit) {
    setSquareAspect(true);
    this.orbit=orbit;
    addMouseListener(this);
    addMouseMotionListener(this);
  }


  private static float[] dashed = {4f,2f};
  private static BasicStroke dbs = new BasicStroke(0.75f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,1.0f,dashed,0.0f);
  private static BasicStroke bs = new BasicStroke(0.75f);
  private static BasicStroke bs1 = new BasicStroke(1.0f);
  
  public void draw(Graphics2D g2){

    //draws static limit in light gray
    double rPlot = (xToPix(orbit.rTorPlot(2)) - xToPix(-orbit.rTorPlot(2)))/2.0;
    //...size of static limit in pixels
    g2.setColor(new Color(128, 128, 128));
    g2.fill(new Ellipse2D.Double(xToPix(0) - rPlot, yToPix(0) - rPlot, 2 * rPlot, 2 * rPlot ));

    //draws black hole
    rPlot = (xToPix(orbit.rTorPlot(orbit.getRHorizon())) - xToPix(-orbit.rTorPlot(orbit.getRHorizon())))/2.0;
    //...size of a black hole in pixels
    g2.setColor(Color.black);
    g2.fill(new Ellipse2D.Double(xToPix(0) - rPlot, yToPix(0) - rPlot, 2 * rPlot, 2 * rPlot));

    double rMax = Math.max(sx2,sy2);
    double rr;
    if(showGrid){
      g2.setStroke(dbs);
      //draws the radial grid in the Orbit Plot
      g2.setColor(new Color(200, 200, 200));
      rr = 0;
      while (rr < 1.6 * rMax) {
        g2.draw(new Ellipse2D.Double(xToPix(-orbit.rTorPlot(rr)), yToPix(orbit.rTorPlot(rr)), xToPix(orbit.rTorPlot(rr)) - xToPix(-orbit.rTorPlot(rr)), yToPix(-orbit.rTorPlot(rr)) - yToPix(orbit.rTorPlot(rr))));
        rr += 4 * gridDivision;
      }
      for (int i = 0; i <= 17; i++) {
        g2.draw(new Line2D.Double(xToPix(orbit.rTorPlot(0) * Math.cos(2 * Math.PI * i / 18f)), yToPix(orbit.rTorPlot(0) * Math.sin(2 * Math.PI * i / 18f)), xToPix(Math.sqrt(2) * orbit.rTorPlot(rMax) * Math.cos(2 * Math.PI * i / 18f)), yToPix(Math.sqrt(2) * orbit.rTorPlot(rMax) * Math.sin(2 * Math.PI * i / 18f))));
      }
      g2.setStroke(bs);
    }

    //draws INNER Cauchy horizon
    rPlot = (xToPix(orbit.rTorPlot(orbit.getRInnerHorizon())) - xToPix(-orbit.rTorPlot(orbit.getRInnerHorizon())))/2.0;
    //...size of a black hole in pixels
    g2.setColor(Color.green);
    g2.draw(new Ellipse2D.Double(xToPix(0) - rPlot, yToPix(0) - rPlot, 2 * rPlot, 2 * rPlot));

    //draws ring singularity
    rPlot = (xToPix(orbit.rTorPlot(0)) - xToPix(-orbit.rTorPlot(0)))/2.0;
    //...size of a black hole in pixels
    g2.setColor(new Color(230, 232, 101));

    g2.fill(new Ellipse2D.Double(xToPix(0) - rPlot, yToPix(0) - rPlot, 2 * rPlot, 2 * rPlot));



    double r1, p1;
    if(showTrail){
      //draw orbit
      g2.setStroke(bs);

      
      // BH optimizations. See note below about trails
      double horizon =  orbit.getRHorizon();
      Color lastColor = null;
      p1 = orbit.getPhi(0);
      r1 = orbit.rTorPlot(orbit.getR(0));
      int x1 = (int) xToPix(r1 * Math.cos(p1));
      int y1 = (int) yToPix(r1 * Math.sin(p1));

      for (int n = orbit.getNumPoints(), i = 1; i < n; i++) {
        //int colorComp=Math.round((float)i/((float)orbit.numPoints)*255f);
        //g2.setColor(new Color(255, colorComp, colorComp));
    	
        double pi = orbit.getPhi(i);
        // BH SwingJS Problem is that trails slow down the animation significantly.
        // This next check allows early points to run faster. 
        // Clearly, we might not want that, so feel free to veto that.
        //
        if (pi == p1)
        	break;
        p1 = pi;
        double ri = orbit.rTorPlot(orbit.getR(i));
        int x = (int) xToPix(ri * Math.cos(pi));
        int y = (int) yToPix(ri * Math.sin(pi));
        //g2.draw(new Line2D.Double(x1, y1, x, y));
    	Color c = (orbit.getR(i) < horizon ? cWithinHorizon : Color.BLACK);
        if (c != lastColor) {
        	g2.setColor(lastColor = c);
        }
        g2.drawLine(x1, y1, x, y);
        x1 = x;
        y1 = y;
      }

    }



    // draws the orbiter
    g2.setStroke(bs1);
    g2.setColor(Color.red);
    r1 = orbit.rTorPlot(orbit.getR(0));
    p1 = orbit.getPhi(0);
    g2.fill(new Ellipse2D.Double(xToPix(r1 * Math.cos(p1)) - 2.5,
        yToPix(r1 * Math.sin(orbit.getPhi(0))) - 2.5, 5, 5));

    if(showScale){
      //  draws the horizontal linear scale in the Orbit plot
      g2.setFont(f2);
      g2.setColor(cGrey200);
      g2.draw(new Line2D.Double(xToPix(orbit.rTorPlot(orbit.getRInnerHorizon())), yToPix(0),
          xToPix(orbit.rTorPlot(orbit.getRHorizon())), yToPix(0)));
      g2.setColor(Color.black);
      g2.draw(new Line2D.Double(xToPix(orbit.rTorPlot(orbit.getRHorizon())), yToPix(0),
          xToPix(orbit.rTorPlot(1.1 * rMax)), yToPix(0)));
      rr = 0;
      while (rr < 1.1 * rMax) {
        if (rr < orbit.getRHorizon())
          g2.setColor(cGrey200);
        else
          g2.setColor(Color.black);
        g2.draw(new Line2D.Double(xToPix(orbit.rTorPlot(rr)), yToPix(0) - 3,
            xToPix(orbit.rTorPlot(rr)), yToPix(0) + 3));
        rr += 4 * gridDivision;
      }
      rr = 0;
      double scaleDivision = 0.5 *
      Math.pow(2, Math.round( (float) (Math.log(0.5 * (rMax)) / Math.log(2))));
      int digits = -Math.round( (float) (Math.log(0.02 * scaleDivision) /
          Math.log(10)));
      if (digits < 0)
        digits = 0;
      while (rr < 1.1 * rMax) {
        String s = MyUtils.roundOff(rr, digits);
        FontMetrics fm = g2.getFontMetrics();
        int sW = fm.stringWidth(s) + 4;
        int sH = fm.getHeight();
        g2.setColor(cGrey247);
        g2.fill(new Rectangle2D.Double(xToPix(orbit.rTorPlot(rr)) - sW / 2, yToPix(0) + 3, sW, sH));
        //if(rr<ode.getRHorizon()) g2.setColor(new Color(200,200,200));
        //else
        g2.setColor(Color.black);
        g2.draw(new Rectangle2D.Double(xToPix(orbit.rTorPlot(rr)) - sW / 2, yToPix(0) + 3, sW, sH));
        MyUtils.drawString(s, xToPix(orbit.rTorPlot(rr)), yToPix(0) + 7, 2, f2, g2);

        rr += 4 * gridDivision;
      }
    }

    if(cursorOver){
      //draws blue rectangle in the lower right of trajectory plot
      //with cursor position
      g2.setFont(f2);
      double curX = pixToX(curI);
      double curY = pixToY(curJ);
      double curR = orbit.rPlotTor(Math.sqrt(curX * curX + curY * curY));
      double cPhiDeg = (Math.atan2(curY, curX)) * 180.0 / Math.PI;
      if (cPhiDeg < 0)
        cPhiDeg += 360.0;
      FontMetrics fm = g2.getFontMetrics();

      if (curR > 0) {
        String sText="r = ".concat(MyUtils.roundOff(curR, 2)).concat(" M;  ").concat("phi = ").concat(MyUtils.roundOff(cPhiDeg, 1)).concat("�");
        int sW = fm.stringWidth(sText) + 6;
        int sH = fm.getHeight()+2;
        g2.setColor(new Color(207, 207, 255, 120));
        g2.fill(new Rectangle2D.Double(0, size.getHeight()-sH, sW, sH));
        g2.setColor(Color.black);
        //g2.draw(new Rectangle2D.Double(0, size.getHeight()-sH, sW, sH));
        g2.drawString(sText, 3-1, size.height-3-1);
      }
      else {
        String sText="Ring Singularity!";
        int sW = fm.stringWidth(sText) + 6;
        int sH = fm.getHeight()+2;
        g2.setColor(new Color(207, 207, 255,120));
        g2.fill(new Rectangle2D.Double(0, size.getHeight()-sH, sW, sH));
        g2.setColor(Color.black);
        //g2.draw(new Rectangle2D.Double(0, size.getHeight()-sH, sW, sH));
        g2.drawString(sText, 3-1, size.height-3-1);
      }
    }


    //draw the Black hole info info in the lower right
    g2.setFont(f1);
    FontMetrics fm = g2.getFontMetrics();
    String sText;
    if(Math.abs(orbit.ic.getA())<0.0001){
      sText="Black hole: nonspinning";
    }
    else if(Math.abs(orbit.ic.getA()-1)<0.0001){
      sText="Black hole: spinning counterclockwise - extremal";
    }
    else if(Math.abs(orbit.ic.getA()-(-1))<0.0001){
      sText="Black hole: spinning clockwise - extremal";
    }
    else if(orbit.ic.getA()<0){
      sText="Black hole: spinning clockwise";
    }
    else {
      sText="Black hole: spinning counterclockwise";
    }
    int sW = fm.stringWidth(sText) + 6;
    int sH = fm.getHeight()+2;
    g2.setColor(new Color(255, 255, 255, 120));
    g2.fill(new Rectangle2D.Double(size.getWidth()-sW, size.getHeight()-sH,sW, sH));
    g2.setColor(Color.black);
    g2.drawString(sText, size.width-sW+3-1, size.height-3-1);




    //draw the initial ring with the starting position of the orbiter
    if(showRing){
      //draw the launching ring
      double r = orbit.getIC().getR();
      double angle = orbit.getRingAngle();
      double rPl=orbit.rTorPlot(r);
      double yPl=rPl*Math.sin(angle);
      double xPl=rPl*Math.cos(angle);
      g2.setColor(new Color(40,40,240,40));
      g2.setStroke(new BasicStroke(4f));
      g2.draw(new Ellipse2D.Double(xToPix(-rPl),yToPix(rPl),xToPix(rPl)-xToPix(-rPl),yToPix(-rPl)-yToPix(rPl)));

      g2.setStroke(new BasicStroke(1f));
      //draws the launching position traveling with the ring
      g2.setColor(Color.black);
      g2.draw(new Ellipse2D.Double(xToPix(xPl) - 3, yToPix(yPl) - 3, 6, 6));
      g2.setStroke(new BasicStroke(1f));
    }



    //  initial velocity arrow (show only at the begining...)
    if( showInitialArrow ){
      double i1 = xToPix(orbit.rTorPlot(orbit.getIC().getR()));
      double j1 = yToPix(0);

      //draw the velocity vector area with angle ticks
      g2.setColor(new Color(240,40,40,40));
      g2.fill(new Ellipse2D.Double(i1-70,j1-70,140,140));
      double innerRadius = 70 * orbit.getIC().getV0();
      g2.setColor(new Color(240,40,40,60));
      g2.fill(new Ellipse2D.Double(i1-innerRadius,j1-innerRadius,2*innerRadius,2*innerRadius));
      g2.setColor(Color.gray);
      g2.setStroke(new BasicStroke(0.5f));
      for(int i=0; i<36;i++){
        AffineTransform at = g2.getTransform();
        at.rotate(-i*2*Math.PI/36, i1, j1);
        g2.setTransform(at);
        g2.draw(new Line2D.Double(i1+65, j1, i1 + 70, j1));
        at.rotate(+i*2*Math.PI/36, i1, j1);
        g2.setTransform(at);
      }


      g2.setColor(Color.RED);
      g2.setStroke(new BasicStroke(1.5f));
      double theta = orbit.getIC().getTheta0();

      //drawing the arrow with the red head
      AffineTransform at = g2.getTransform();
      at.rotate(-theta, i1, j1);
      g2.setTransform(at);
      double arrowLengthPix = Math.round((float) (70 * orbit.getIC().getV0()+15));
      g2.draw(new Line2D.Double(i1, j1, i1 + arrowLengthPix, j1));
      GeneralPath arrowHead = new GeneralPath();
      g2.draw(new Line2D.Double());
      arrowHead.moveTo((float)(i1 + arrowLengthPix), (float)j1);
      arrowHead.lineTo((float)(i1 + arrowLengthPix - 15), (float)(j1 - 5));
      arrowHead.lineTo((float)(i1 + arrowLengthPix - 15), (float)(j1 + 5));
      arrowHead.closePath();
      g2.fill(arrowHead);
      g2.draw(arrowHead);
      at.rotate(+theta, i1, j1);
      g2.setTransform(at);
      g2.setStroke(new BasicStroke(1.0f));
    }

    //  draws a dot representing initial position of orbiter
    g2.setColor(Color.black);
    g2.fill(new Ellipse2D.Double(xToPix(orbit.rTorPlot(orbit.getIC().getR())) - 2.5, yToPix(0) - 2.5, 5, 5));


  }

//  public void addPropertyChangeListener(PropertyChangeListener listener){
//    support.addPropertyChangeListener(listener);
//  }

  public void setInitialArrowVisible(boolean value){
    showInitialArrow = value;
  }

  public void setRMax(double rMax){
    double exponent = Math.floor(Math.log(rMax) / Math.log(2));
    double gridDivision = Math.pow(2, exponent - 3);
    if (gridDivision < 0.03125) gridDivision = 0.03125;
    Math.abs(orbit.getODESolver().getStepSize());
    //sign used to allow simulation backwards
    setGridDivision(gridDivision);
    setPreferredMinMaxY(-orbit.rTorPlot(rMax),orbit.rTorPlot(rMax));


  }

  /**
   * sets new scale of the Orbit panel so that the velocity arrow is visible...
   * @param rMax
   */
  public void setRMax2(double rMax){
    double rm = rMax + pixToX(80)-pixToX(0);
    double exponent = Math.floor(Math.log(rm) / Math.log(2));
    double gridDivision = Math.pow(2, exponent - 3);
    if (gridDivision < 0.03125) gridDivision = 0.03125;
    Math.abs(orbit.getODESolver().getStepSize());
    //sign used to allow simulation backwards
    setGridDivision(gridDivision);
    setPreferredMinMaxY(-orbit.rTorPlot(rm),orbit.rTorPlot(rm));
  }

  public double getRMax(){
    return (sy2-sy1)/2;
  }

  public void setGridDivision(double gridDivision){
    this.gridDivision=gridDivision;
  }

  public void setShowGrid(boolean v){
    showGrid=v;
  }

  public void setShowTrail(boolean v){
    showTrail=v;
  }

  public void setShowScale(boolean v){
    showScale=v;
  }

  public void setShowRing(boolean v){
    showRing=v;
  }

  public boolean getShowGrid(){
    return showGrid;
  }

  public boolean getShowTrail(){
    return showTrail;
  }

  public boolean getShowScale(){
    return showScale;
  }

  public boolean getShowRing(){
    return showRing;
  }

  public boolean isStopped(){
    return isStopped;
  }

  public void setStopped(boolean isStopped){
    this.isStopped = isStopped;
  }

  public void mousePressed(MouseEvent e) {
    if(isStopped()){
      int i = e.getX();
      int j = e.getY();
      double iHead = xToPix(orbit.rTorPlot(orbit.getIC().getR()));
      double jHead = yToPix(0);
      double arrowLength=70 * orbit.getIC().getV0()+15;
      double theta=orbit.getIC().getTheta0();
      iHead+=arrowLength*Math.cos(theta);
      jHead-=arrowLength*Math.sin(theta);

      if((Math.abs(i-iHead)<=10)&&(Math.abs(j-jHead)<=10) && showInitialArrow) {
        arrowDragged=true;
        setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
      else {
        arrowDragged=false;
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }

      curI = e.getX();
      curJ = e.getY();
      cursorOver=true;
      repaint();
    }
  }


  public void mouseDragged(MouseEvent e) {
    if(isStopped()){
      int i = e.getX();
      int j = e.getY();
      double iTail = xToPix(orbit.rTorPlot(orbit.getIC().getR()));
      double jTail = yToPix(0);

      if(arrowDragged) {
        double theta0=Math.atan2(jTail-j,i-iTail);
        double v0=Math.sqrt((jTail-j)*(jTail-j)+(i-iTail)*(i-iTail))/(70+15);
        if(v0>=1)v0=0.999;

        if(e.isAltDown()){
          orbit.getIC().setV0(v0);
        }
        else{
          orbit.getIC().setTheta0(theta0);
        }

        orbit.getIC().adjustEmLmSign();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        //support.
        firePropertyChange("orbMouseChange",null,null);
      }
      else setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    //do always
    curI = e.getX();
    curJ = e.getY();
    cursorOver=true;
    repaint();
  }

  public void mouseReleased(MouseEvent e) {
    if(isStopped()){
      arrowDragged=false;
    }
  }

  public void mouseEntered(MouseEvent e) {
    if(isStopped()&&  showInitialArrow){
      //support.
      firePropertyChange("orbMouseEntered",null,null);
    }
  }

  public void mouseExited(MouseEvent e) {
    if(isStopped()&&  showInitialArrow){
      //support.
      firePropertyChange("orbMouseExited",null,null);
    }

    //do always
    cursorOver=false;
    repaint();
  }

  public void mouseClicked(MouseEvent e) {

  }

  public void mouseMoved(MouseEvent e) {
    if(isStopped()){
      int i = e.getX();
      int j = e.getY();
      double iHead = xToPix(orbit.rTorPlot(orbit.getIC().getR()));
      double jHead = yToPix(0);
      double arrowLength=70 * orbit.getIC().getV0()+15;
      double theta=orbit.getIC().getTheta0();
      iHead+=arrowLength*Math.cos(theta);
      jHead-=arrowLength*Math.sin(theta);

      if((Math.abs(i-iHead)<=10)&&(Math.abs(j-jHead)<=10)&&  showInitialArrow) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
      else setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    curI = e.getX();
    curJ = e.getY();
    cursorOver=true;
    repaint();
  }








}
