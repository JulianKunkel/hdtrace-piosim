package de.hdTraceInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sun.corba.se.spi.orb.Operation;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.tests.MathematicalExpressionTest;
import de.viewer.timelines.FilterTokenInterface;
import de.viewer.timelines.FilterTokenInterface.DoubleFilterToken;
import de.viewer.timelines.FilterTokenInterface.DurationFilterToken;
import de.viewer.timelines.FilterTokenInterface.FilterExpression;
import de.viewer.timelines.FilterTokenInterface.FilterTokenType;
import de.viewer.timelines.FilterTokenInterface.RegexFilterToken;
import de.viewer.timelines.FilterTokenInterface.SimpleFilterToken;
import de.viewer.timelines.FilterTokenInterface.StringFilterToken;

/**
 * Converts a textual expression into a mathematical expression
 * and apply them
 * 
 * @author julian
 */
public class MathematicalExpression {

	/**
	 * The metrics which are required to compute the function
	 */
	private String [] requiredMetrics;

	private Object [] operants = null;

	/**
	 * check if the expression starts with ( and ends with ), then we can remove those brackets e.g.:
	 * ((A+B)) => remove those brackets
	 * ((A+B)*C) => remove only the outer bracket
	 * @param textualExpression
	 * @return
	 */
	private String removeOuterBrackets(String textualExpression){

		int startBrackets = 0;
		for( int i = 0 ; i < textualExpression.length() / 2 - 1 ; i++ ){
			if (textualExpression.charAt(i) == '('){
				startBrackets++;
				continue;
			}
			// otherwise stop
			break;
		}
		
		if(startBrackets > 0){							
			// scan through the expression to detect whether the actual nesting is less than expected
			int minBracketNesting = startBrackets;
			int currentBracketDepth = startBrackets;
			for( int i = minBracketNesting ; i < textualExpression.length() - 1 - startBrackets; i++ ){
				if (textualExpression.charAt(i) == '('){
					currentBracketDepth++;
				}else if (textualExpression.charAt(i) == ')'){
					currentBracketDepth--;
					
					minBracketNesting = minBracketNesting < currentBracketDepth ? minBracketNesting : currentBracketDepth;
				}
			}
			
			textualExpression = textualExpression.substring(minBracketNesting, textualExpression.length() - minBracketNesting);
		}
		
		return textualExpression;
	}

	/**
	 * Construct a mathematical expression from the textual representation.
	 * Normally disjoint statistics are identified, e.g. U*I can be computed, to multiply or sum over multiple
	 * timelines the * or + operator is written in front of an statistic identifier.
	 * 
	 * Understood operators:
	 * *,+,-,/ as usual, however, they can be used as a unary operator to summarize among multiple timelines e.g. + U summarizes the U values among all timelines.
	 * ^ acts as MAX
	 * , acts as MIN, e.g. U,I is the minimum of U and I, it binds as good as the multiplication => it is best to put brackets around of MIN and MAX operators
	 * () normal bracket rules 
	 * 
	 * <DOUBLE> values
	 * <ID> is an identifier / name for a statistics which gets replaced
	 *  
	 *  @See {@link MathematicalExpressionTest} for some examples how to use this class
	 *  
	 * @param textualExpression
	 */
	public MathematicalExpression(String textualExpression) {
		String nestedData = "";
		int nestingDepth = 0;

		textualExpression = removeOuterBrackets(textualExpression.trim());
						
		// replace whitespace
		char [] array = (textualExpression + " ").toCharArray();

		final ArrayList operants = new ArrayList();
		// store all metrics we need in the HashSet
		final HashSet<String> requiredMetrics = new HashSet<String>();

		for( int i = 0 ; i < array.length - 1 ; i++ ){	
			final char c = array[i];
			final char n = array[i+1];

			if(c == ')'){
				// unstack
				nestingDepth--;
				if(nestingDepth == 0){
					// scanned a new bracket with its content:
					MathematicalExpression nestedMath = new MathematicalExpression(nestedData);
					operants.add(nestedMath);

					// add the required metrics to this set
					for(String e: nestedMath.requiredMetrics){
						requiredMetrics.add(e);	
					}

					nestedData = "";
				}else{
					nestedData = nestedData + ')';
				}
			}else if(c == '('){
				// stack								
				// append nested brackets
				if(nestingDepth > 0){
					nestedData = nestedData + '(';						
				}

				nestingDepth++;
			}else if(nestingDepth > 0){
				// append string
				nestedData = nestedData + c;	
			}else if(c == '*' || c== '+' || c == '^' || c == ',' || c == '-' || c == '/'){				
				operants.add(c);
				
				if(i == 0) // aggregates are always nested
					nestingDepth++;				
			}else if( Character.isLetter(c) || c == '_' || c == '-' ){
				// character					
				// look ahead next char, if char continue otherwise finish
				nestedData = nestedData + c;
				if( ! Character.isLetter( n ) &&  n != '_' && n != '-' ) {
					// finalize this string
					// add this operant to the required set

					// nest multiplication correctly
					if(operants.size() > 0  && (n == '*' || n == '^' || n == ',' || c == '/') && array[i+2] != '('){
						// brackets will be handled automatically correctly, thus no special treatment is needed here.
						nestingDepth++;
					}else{					
						requiredMetrics.add(nestedData);
						operants.add(nestedData);

						nestedData = "";
					}	
				}
			}else if(Character.isDigit(c) || c == '.'){
				if( (! Character.isDigit( n )) && n != '.' ) {					
					nestedData = nestedData + c;

					if(operants.size() > 0 && (n == '*' || n == '^' || n == ',' || c == '/')  && array[i+2] != '('){
						// brackets will be handled automatically correctly, thus no special treatment is needed here.
						nestingDepth++;
					}else{
						// parse double
						operants.add(Double.parseDouble(nestedData));
						nestedData = "";
					}					
				}else{
					nestedData = nestedData + c;
				}
			}
		}

		if(nestingDepth > 0){
			// close the nesting for multiplications			
			MathematicalExpression nestedMath = new MathematicalExpression(nestedData);
			operants.add(nestedMath);

			// add the required metrics to this set
			for(String e: nestedMath.requiredMetrics){
				requiredMetrics.add(e);	
			}
		}

		// store the operants
		this.operants = operants.toArray();

		this.requiredMetrics = requiredMetrics.toArray(new String[0]);
	}

	@Override
	public String toString() {
		return textualRepresentation();
	}

	public String textualRepresentation() {
		StringBuffer buff = new StringBuffer("(");

		for (Object o: operants){
			buff.append(o.toString());
		}

		buff.append(")");
		return buff.toString();
	}

	public String[] getRequiredMetrics() {
		return requiredMetrics;
	}


	
	
	/**
	 * Compute the numeric value of this expression by using the provided variable names and values.
	 * 
	 * @param lastValues
	 * @param variableNames
	 * @return
	 */
	
	public double computeFunction(double [] lastValues, String [] variableNames){

		if(operants[0].getClass() == Character.class){			
			
			// spawn multiple and then perform the operation
			final MathematicalExpression expression = (MathematicalExpression) operants[1];
						
			// determine how many pairs we can form.
			int pairs = 0;
			{
				String oneElem = requiredMetrics[0];
				for(String s: variableNames){
					if (oneElem.matches(s)){
						pairs++;
					}
				}
			}
			
			assert(pairs > 0);
			
			double [] returnValues = new double[pairs];
			
			// the values for the computation
			String [] variableNamesFormed  = new String[requiredMetrics.length];
			double [] valuesFormed = new double[requiredMetrics.length]; 
			
			// current position in variableNames, the next elements are matched until all required metrics are found 
			for(int pair=0; pair < pairs ; pair++){
			
				// we know all variableNames are put after another, e.g. if A and B are needed A,B,A,B or B,A,B,A is in the array. 
				final int posInNames = pair * requiredMetrics.length;
				final int endPos = (pair+1) * requiredMetrics.length;
				
				// localize all variable names
				
				for(int c = posInNames ; c < endPos ; c++){			
					for(int var = 0; var < requiredMetrics.length; var++){										
						if(requiredMetrics[var].equals(variableNames[c])){
							// matches!
							variableNamesFormed[var] = variableNames[c];
							valuesFormed[var]        = lastValues[c];
						}
					}
				}
				
				returnValues[pair] = expression.computeSingleValue(valuesFormed, variableNamesFormed);
			}
			
			// compute aggregated result
				
			double newValue = 0;
			
			final char operation = (Character) operants[0];
			
			if(operation == ','){
				newValue = Double.MAX_VALUE; 
			}else if(operation == '^'){
				newValue = - Double.MAX_VALUE;
			}				
			
			// reduce all the values together
			for(double val: returnValues){
				newValue = reduce(operation, newValue, val);
			}
			
			return newValue;
		}else{
			return computeSingleValue(lastValues, variableNames);
		}
	}

	private double reduce(char operation, double tmpVar, double newValue){
		if(operation == '+'){
			return tmpVar + newValue;					
		}else if(operation == '*'){
			return tmpVar * newValue;		
		}else if(operation == ','){
			return tmpVar < newValue ? tmpVar : newValue; 
		}else if(operation == '^'){
			return tmpVar > newValue ? tmpVar : newValue;
		}		
		return 0.0;
	}
	
		
	/**
	 * Use exactly one variable name and value.
	 * 
	 * @param lastValues
	 * @param variableNames
	 * @return
	 */
	private double computeSingleValue(double [] lastValues, String [] variableNames){
		double computedValue = 0;
		int pos = 0;

		// start with addition operation
		char operation = '+';

		for(pos = 0; pos < operants.length; pos++ ){
			Object o = operants[pos];
			// lookahead one object

			//System.out.println(" " + o);

			double newValue = 0;

			if(o.getClass() == Character.class){
				// remember the operation
				operation = (Character) o;				
				continue;

			}else if(o.getClass() == MathematicalExpression.class){
				// nested expression				
					newValue = ((MathematicalExpression) o).computeFunction(lastValues, variableNames);
			}else if(o.getClass() == String.class){
				// variable
				int count = 0;
				for(int c = 0 ; c < variableNames.length ; c++){
					if(o.equals(variableNames[c])){
						// matches!
						count++;
						newValue = lastValues[c];
					}
				}

				if(count != 1){
					throw new IllegalArgumentException("Error did not find attribute with name " + o.toString() + " exactly once!");
				}

			}else if(o.getClass() == Double.class){
				newValue = (Double) o;
			}

			//System.out.println("Performing " + computedValue + operation + newValue);

			if(operation != ' '){
				// perform the operation with the new value
				if(operation == '+'){
					computedValue = computedValue + newValue;					
				}else if(operation == '*'){
					computedValue = computedValue * newValue;					
				}else if(operation == '/'){
					computedValue = computedValue / newValue;
				}else if(operation == '-'){
					computedValue = computedValue - newValue;
				}else if(operation == ','){
					computedValue = computedValue < newValue ? computedValue : newValue; 
				}else if(operation == '^'){
					computedValue = computedValue > newValue ? computedValue : newValue;
				}
				operation = ' ';
			}
		}

		return computedValue;
	}
}
