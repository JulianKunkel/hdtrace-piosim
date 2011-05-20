package de.tests;

import junit.framework.Assert;
import junit.framework.TestSuite;


import org.junit.Test;

import de.hdTraceInput.MathematicalExpression;

public class MathematicalExpressionTest extends TestSuite{
	
	@Test
	public void realValueTest2(){
		MathematicalExpression e = new MathematicalExpression("4.3*2.0");
		String [] variableNames = {"A"};
		double [] values =        {4};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for  4.3 *2.0 " + result);
		
		Assert.assertEquals(result, 8.6, 0.001);
	}
	
	
	@Test
	public void realValueTest(){
		MathematicalExpression e = new MathematicalExpression("A*5.0+4.3*2.0");
		String [] variableNames = {"A"};
		double [] values =        {4};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for 4*5.0 + 4.3 *2.0 " + result);
		
		Assert.assertEquals(result, 28.6, 0.001);
	}
	
	
	@Test
	public void minMaxTest(){
		MathematicalExpression e = new MathematicalExpression("A+B*C,A");
		String [] variableNames = {"B", "A", "C"};
		double [] values =        {2, 1, 4};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for 1+2*(4,1) " + result);
		
		Assert.assertEquals(result, 3.0, 0.001);
	}
	
	
	@Test
	public void pointForMultiplication(){
		MathematicalExpression e = new MathematicalExpression("A+B*C");
		String [] variableNames = {"B", "A", "C"};
		double [] values =        {2, 1, 4};
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for 1+2*4 " + result);
		Assert.assertEquals(result, 9.0, 0.001);
	}
	
	@Test
	public void creation(){
		MathematicalExpression e = new MathematicalExpression("A*B+C");
		String [] variableNames = {"B", "A", "C"};
		double [] values =        {2, 1, 4};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for 1*2+4 " + result);
		Assert.assertEquals(result, 6.0, 0.001);
	}
	
	@Test
	public void creationNested(){
		MathematicalExpression e = new MathematicalExpression("A*(B+C)");
		String [] variableNames = {"B", "A", "C"};
		double [] values =        {2, 3, 4};
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for 3*(2+4)" + result);
		
		Assert.assertEquals(result, 18.0, 0.001);
	}
	
	
	@Test
	public void timelineAdder(){
		MathematicalExpression e = new MathematicalExpression("(+A)+B");
		String [] variableNames = {"A", "A", "B"};
		double [] values =        {1, 3, 2};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for  1+3+2 " + result);
		
		Assert.assertEquals(6.0, result,  0.001);
	}
	
	@Test
	public void timelineMIN(){
		MathematicalExpression e = new MathematicalExpression("(,A)+B");
		String [] variableNames = {"A", "A", "B"};
		double [] values =        {1, 3, 2};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for  1,3+2 " + result);
		
		Assert.assertEquals(3.0, result,  0.001);
	}
	
	@Test
	public void timelineAddMultiplier(){
		MathematicalExpression e = new MathematicalExpression("+(A*B)");
		String [] variableNames = {"A", "B", "A", "B"};
		double [] values =        {1, 2, 2, 2};
		
		System.out.println( e.textualRepresentation());		
		double result = e.computeFunction(values, variableNames);
		System.out.println("Result for  1*2 + 2*2 " + result);
		
		Assert.assertEquals( 6.0, result, 0.001);
	}
	
	
	public MathematicalExpressionTest() {

	}
}
