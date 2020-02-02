package debugging;

import org.opensourcephysics.controls.XMLControlElement;

public class SetXMLControlTest {

	public static void main(String[] args) {
		System.out.println("Testing XML Control Element");
		XMLControlElement control= new XMLControlElement();
		String myName="myName";
		System.out.println("Adding a String to Control Element");
		control.setValue("name", myName);
		System.out.println(control.toString());
		System.out.println();
		
		double pi=3.14;
		System.out.println("Adding a number to Control Element");
		control.setValue("pi", pi);
		System.out.println(control.toString());
		System.out.println();
		
		System.out.println("Adding an array to Control Element");
		double[] array= {10.0,20.0,30.0};
		control.setValue("data", array);
		System.out.println(control.toString());
		System.out.println();
		
		System.out.println("Adding a 2D array to Control Element");
		double[][] array2D= {{1,10.0},{2,20.0},{3,30.0}};
		control.setValue("data2", array2D);
		System.out.println(control.toString());
	}

}
