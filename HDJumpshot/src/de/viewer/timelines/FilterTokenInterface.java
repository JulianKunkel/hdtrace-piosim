package de.viewer.timelines;

import java.util.ArrayList;
import java.util.regex.Pattern;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;

public interface FilterTokenInterface {

	/**
	 * The interface implemented for each filter token.
	 * Defines comparsion with a traceEntry. 
	 * 
	 * @author julian
	 */
		public FilterTokenType getType();			
	

	/**
	 * Type of a token in the filter expression
	 * 
	 * @author julian
	 */
	public enum FilterTokenType{
		ATTRIBUTE_NAME, // usual case, just attributes
		CATEGORY_NAME,  // special, could be a category or sth.
		LESS_THAN,
		LARGER_THAN,
		DOUBLE_VAL,
		EQUALS,
		AND,
		OR,
		HEATMAP,
		COMPOUND;
	}

	/**
	 * Wrapper for all the simple tokens.
	 * @author julian
	 *
	 */
	static public class SimpleFilterToken implements FilterTokenInterface{
		final private FilterTokenType typ;

		static public FilterTokenInterface HEATMAP = new SimpleFilterToken(FilterTokenType.HEATMAP);
		static public FilterTokenInterface OR = new SimpleFilterToken(FilterTokenType.OR);
		static public FilterTokenInterface AND = new SimpleFilterToken(FilterTokenType.AND);
		static public FilterTokenInterface EQUALS = new SimpleFilterToken(FilterTokenType.EQUALS);
		static public FilterTokenInterface LARGER_THAN = new SimpleFilterToken(FilterTokenType.LARGER_THAN);
		static public FilterTokenInterface LESS_THAN = new SimpleFilterToken(FilterTokenType.LESS_THAN);
		
		
		private SimpleFilterToken(FilterTokenType typ) {
			this.typ = typ;
		}
		
		@Override
		public FilterTokenType getType() {		
			return typ;
		}
	}

	static public class StringFilterToken implements FilterTokenInterface{
		final FilterTokenType typ;
		final String 		  value; 
		
		public StringFilterToken(FilterTokenType typ, String value) {
			this.typ = typ;
			this.value = value;
		}
		
		@Override
		public FilterTokenType getType() {
			return typ;
		}
	}
	
	static public class RegexFilterToken implements FilterTokenInterface{
		final FilterTokenType typ;
		final Pattern pattern;
		
		public RegexFilterToken(FilterTokenType typ, String regex) {
			this.typ = typ;			
			this.pattern = Pattern.compile(regex);
		}
		
		@Override
		public FilterTokenType getType() {
			return typ;
		}
	}
	
	static public class DoubleFilterToken implements FilterTokenInterface{
		final double 		  value; 
		
		public DoubleFilterToken(double value) {
			this.value = value;
		}
		
		@Override
		public FilterTokenType getType() {
			return FilterTokenType.DOUBLE_VAL;
		}
	}
	
	static public class FilterExpression implements FilterTokenInterface{
		final ArrayList<FilterTokenInterface> nestedTokens = new ArrayList<FilterTokenInterface>();
		
		@Override
		public FilterTokenType getType() {		
			return FilterTokenType.COMPOUND;
		}
		
		private boolean nextMatches(FilterTokenInterface token, ITracableObject object){			
			switch(token.getType()){
				case CATEGORY_NAME:{
					Pattern p = ((RegexFilterToken) token).pattern;
					
					if(object.getType() == TracableObjectType.EVENT){
						TraceEntry e = (TraceEntry) object;
						if (p.matcher(e.getName()).matches()){
							  return true;
						}
					}else if(object.getType() == TracableObjectType.STATE){
							StateTraceEntry e = (StateTraceEntry) object;
							if (p.matcher(e.getName()).matches()){
								return true;
							}
					}else if(object.getType() == TracableObjectType.STATISTICENTRY){
						StatisticsEntry e = (StatisticsEntry) object;
						if (p.matcher(e.getParentGroupEntry().getGroup().getName()).matches()){
							return true;
						}
					}
					return false;
				}
				case COMPOUND:{
					return ((FilterExpression) token).matches(object); 
				}
				
			}
			return false;
		}
		
		public boolean matches(ITracableObject object) {
			boolean evalResult = true;
			
			// walk through the nestedTokens and proof whether the object matches.			
			for(int i=0; i < nestedTokens.size(); i++){				
				final FilterTokenInterface token = nestedTokens.get(i);
				final FilterTokenInterface lookahead = ( i == nestedTokens.size() -1 ) ? null : nestedTokens.get(i+1);

				switch(token.getType()){
					case CATEGORY_NAME:{
						evalResult = nextMatches(token, object);
						break;
					}case AND:{
						if (lookahead == null)
							throw new IllegalArgumentException("AND should be terminated!");
						
						evalResult = evalResult && nextMatches(lookahead, object);
						
						i++;
						break;
					}case OR:{
						if (lookahead == null)
							throw new IllegalArgumentException("OR should be terminated!");
						
						evalResult = evalResult || nextMatches(lookahead, object);
						
						i++;
						break;
					}case COMPOUND:{
						// only a single object tolerated!
						evalResult = nextMatches(token, object);
						break;
					}
					default:
						return false;
				}
			}
			
			return evalResult;
		}
		
		/**
		 * Tokenize the expression.
		 * This is right now handled in a simple way. The text must end with " "
		 * 
		 * @param text
		 * @throws IllegalArgumentException
		 */
		public FilterExpression(String text) throws IllegalArgumentException{
			assert(text.endsWith(" "));
			
			// parse from left to right until a bracket start is detected:
			String nestedData = "";
			int nestingDepth = 0;
			char [] array = text.toCharArray();
			boolean startSpecial = false;
			
			for( int i =0 ; i < array.length - 1 ; i++ ){	
				final char c = array[i];
				final char n = array[i+1];
				
				if(c == ')'){
					// unstack
					nestingDepth--;
					if(nestingDepth == 0){
						System.out.println(nestedData);
						nestedTokens.add(new FilterExpression(nestedData + " "));
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
				}else if(c == '|'){
					nestedTokens.add(SimpleFilterToken.OR);
				}else if(c == '&'){
					nestedTokens.add(SimpleFilterToken.AND);
				}else if(c == '<'){
					nestedTokens.add(SimpleFilterToken.LESS_THAN);
				}else if(c == '>'){
					nestedTokens.add(SimpleFilterToken.LARGER_THAN);
				}else if(c == '='){
					nestedTokens.add(SimpleFilterToken.EQUALS);
				}else if(c == ' '){
					// do nothing, skip whitespace
				}else if(c == '.' && nestedData.length() == 0){
					if(startSpecial){
						throw new IllegalArgumentException("Error two . are not allowed in one expression to parse " + text.substring(c));
					}
					startSpecial = true;
				}else if( Character.isLetter(c) ){
					// character					
					// look ahead next char, if char continue otherwise finish
					nestedData = nestedData + c;
					if( ! Character.isLetter( n )) {
						// finalize this string
						
						if(nestedData.equals("heatmap:")){
							nestedTokens.add(SimpleFilterToken.HEATMAP);
						}else if(startSpecial){							
							// localize = in the string			
							// the next char should be "="
							if (n != '='){
								throw new IllegalArgumentException("Special token should be followed by an \"=\" " + nestedData + text.substring(c));
							}
							
							i++;
							
							final int startPos = i;							
							
							// skip all chars until | or & or ) is detected
							for( ; i < array.length ; i++){
								final char m = array[i];
								if(m == '|' || m == ')' || m == '&' || m == ' '){
									i--;
									break;
								}
							}
							final String data = text.substring(startPos + 1, i + 1);
							
							if(nestedData.equals("category")){ 
								nestedTokens.add(new RegexFilterToken(FilterTokenType.CATEGORY_NAME, data));
							//}else if(nestedData == "type"){
							//	nestedTokens.add(new RegexFilterToken(FilterTokenType.TYPE_NAME, data));
							}else{
								throw new IllegalArgumentException("Do not understand special token " + nestedData);
							}
							
						}else{
							nestedTokens.add(new StringFilterToken(FilterTokenType.ATTRIBUTE_NAME, nestedData));
						}
						
						startSpecial = false;
						
						nestedData = "";
					}
				}else if(Character.isDigit(c) || c == '.'){
					if( ! Character.isDigit( n ) && n != '.' ) {
						// parse double
						nestedTokens.add(new DoubleFilterToken( Double.parseDouble(nestedData)));						
						nestedData = "";
					}else{
						nestedData = nestedData + c;
					}
				}
			}
		}
	}
	
}
