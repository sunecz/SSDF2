package sune.util.ssdf2.function;

import java.util.Arrays;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import sune.util.ssdf2.SSDFunctionImpl;

public class Fcalc implements SSDFunctionImpl {
	
	public static final boolean CONTENT_SIMPLE = true;
	
	final double _value(Object expr) {
		if((expr == null
				|| !(expr instanceof String))
				|| ((String) expr).isEmpty())
			return Double.NaN;
		String exprStr = (String) expr;
		return SimpleMath.calc(exprStr);
	}
	
	@Override
	public Object[] invoke(Object... args) {
		if(args == null || args.length == 0) {
			throw new IllegalArgumentException(
				"Function[calc]: Cannot calculate no objects!");
		}
		return Arrays.stream(args, 0, args.length)
					 .map(this::_value)
					 .toArray(Object[]::new);
	}
	
	static final class SimpleMath {
		
		// String constant holding value of constant pi
		static final String STRING_PI = Double.toString(Math.PI);
		// String constant holding value of constant e
		static final String STRING_E  = Double.toString(Math.E);
		
		static enum Operator {
			ADD(1, '+') {
				
				@Override
				public double process(double a, double b) {
					return a + b;
				}
			},
			SUBSTRACT(1, '-') {
				
				@Override
				public double process(double a, double b) {
					return a - b;
				}
			},
			MULTIPLY(2, '*') {
				
				@Override
				public double process(double a, double b) {
					return a * b;
				}
			},
			DIVIDE(2, '/') {
				
				@Override
				public double process(double a, double b) {
					return a / b;
				}
			},
			EXPONENT(3, '^') {
				
				@Override
				public double process(double a, double b) {
					// x^y = exp(y*log(x))
					return Math.exp(b * Math.log(a));
				}
			},
			UNKNOWN(0, '\u0000');
			
			public final int precedence;
			public final char sign;
			private Operator(int precedence, char sign) {
				this.precedence = precedence;
				this.sign		= sign;
			}
			
			public double process(double a, double b) {
				return Double.NaN;
			}
			
			@Override
			public String toString() {
				return Character.toString(sign);
			}
		}
		
		static enum TokenType {
			NUMBER,
			FUNCTION,
			FUNCTION_ARG_SEP,
			OPERATOR,
			LEFT_BRACKET,
			RIGHT_BRACKET;
		}
		
		static final class Token {
			
			public final TokenType type;
			public final Object value;
			
			public Token(TokenType type, Object value) {
				this.type  = type;
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value == null ? "null" : value.toString();
			}
		}
		
		static final class ExpressionTokenizer {
			
			private final StringBuilder builder;
			private final char[] chars;
			private final int length;
			private int index;
			
			public ExpressionTokenizer(String expression) {
				this.builder = new StringBuilder();
				this.chars   = expression.toCharArray();
				this.length  = chars.length;
				this.index   = 0;
			}
			
			public Token next() {
				char c = chars[index++];
				// Type: FUNCTION ARGUMENT SEPARATOR
				if(c == ',') return new Token(TokenType.FUNCTION_ARG_SEP, Character.toString(c));
				// Type: LEFT BRACKET
				if(c == '(') return new Token(TokenType.LEFT_BRACKET, Character.toString(c));
				// Type: RIGHT BRACKET
				if(c == ')') return new Token(TokenType.RIGHT_BRACKET, Character.toString(c));
				// Type: OPERATOR
				Operator o;
				if((o = operator(c)) != Operator.UNKNOWN)
					return new Token(TokenType.OPERATOR, o);
				// Type: NUMBER TOKEN
				if(Character.isDigit(c)) {
					builder.setLength(0);
					builder.append(c);
					int i = index;
					for(; i < length; ++i) {
						c = chars[i];
						if(Character.isDigit(c) || c == '.') {
							builder.append(c);
						} else break;
					}
					index = i;
					return new Token(TokenType.NUMBER, builder.toString());
				}
				// Type: FUNCTION TOKEN
				String func;
				MathFunction mathf;
				for(int i = 0, l = FUNCTIONS.length; i < l; ++i) {
					mathf = FUNCTIONS[i];
					func  = mathf.name;
					if(func.charAt(0) == c) {
						boolean found = true;
						int n = index;
						for(int k = 1, m = func.length(); k < m; ++k, ++n) {
							if(n == length || func.charAt(k) != chars[n]) {
								found = false;
								break;
							}
						}
						if(found) {
							index = n;
							return new Token(TokenType.FUNCTION, mathf);
						}
					}
				}
				return null;
			}
			
			public boolean hasNext() {
				return index < length;
			}
		}
		
		static final class CalculationTokenizer {
			
			private final String[] parts;
			private final int length;
			private int index;
			
			public CalculationTokenizer(String postfix) {
				this.parts  = postfix.split(" ");
				this.length = parts.length;
				this.index  = 0;
			}
			
			public Token next() {
				String part  = parts[index++];
				char[] chars = part.toCharArray();
				// Type: OPERATOR
				Operator o;
				if((o = operator(chars[0])) != Operator.UNKNOWN)
					return new Token(TokenType.OPERATOR, o);
				// Type: NUMBER TOKEN
				if(Character.isDigit(chars[0])) {
					boolean digit = true;
					for(int i = 1, l = part.length(), c; i < l; ++i) {
						c = chars[i];
						if(!Character.isDigit(c) && c != '.') {
							digit = false;
							break;
						}
					}
					if(digit) {
						return new Token(TokenType.NUMBER, part);
					}
				}
				// Type: FUNCTION TOKEN
				String func;
				MathFunction mathf;
				char first = chars[0];
				for(int i = 0, l = FUNCTIONS.length; i < l; ++i) {
					mathf = FUNCTIONS[i];
					func  = mathf.name;
					if(func.charAt(0) == first) {
						boolean found = true;
						for(int k = 1, n = 1, m = func.length(); k < m; ++k, ++n) {
							if(n == l || func.charAt(k) != chars[n]) {
								found = false;
								break;
							}
						}
						if(found) {
							return new Token(TokenType.FUNCTION, mathf);
						}
					}
				}
				return null;
			}
			
			public boolean hasNext() {
				return index < length;
			}
		}
		
		static interface NumberSupplier {
			
			public double supply();
			public void putBack(double a);
		}
		
		static abstract class MathFunction {
			
			public final String name;
			public MathFunction(String name) {
				this.name = name;
			}
			
			public abstract double process(NumberSupplier ts);
			
			@Override
			public String toString() {
				return name;
			}
		}
		
		static final MathFunction[] FUNCTIONS = {
			new MathFunction("sqrt") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.sqrt(ts.supply());
				}
			},
			new MathFunction("sin") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.sin(ts.supply());
				}
			},
			new MathFunction("cos") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.cos(ts.supply());
				}
			},
			new MathFunction("tan") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.tan(ts.supply());
				}
			},
			new MathFunction("log") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.log(ts.supply()) / Math.log(ts.supply());
				}
			},
			new MathFunction("ceil") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.ceil(ts.supply());
				}
			},
			new MathFunction("floor") {
				
				@Override
				public double process(NumberSupplier ts) {
					return Math.floor(ts.supply());
				}
			}
		};
		
		static final Operator operator(char c) {
			if(c == '+') return Operator.ADD;
			if(c == '-') return Operator.SUBSTRACT;
			if(c == '*') return Operator.MULTIPLY;
			if(c == '/') return Operator.DIVIDE;
			if(c == '^') return Operator.EXPONENT;
			return Operator.UNKNOWN;
		}
		
		static final Queue<Token> shuntingYard(ExpressionTokenizer et) {
			Queue<Token> output = new LinkedBlockingQueue<>();
			Stack<Token> stack  = new Stack<>();
			Token token;
			while(et.hasNext()) {
				if((token = et.next()) == null) {
					throw new IllegalStateException(
						"Token is null!");
				}
				switch(token.type) {
					case NUMBER:
						output.add(token);
						break;
					case FUNCTION:
						stack.add(token);
						break;
					case FUNCTION_ARG_SEP:
						while(!stack.isEmpty() && stack.peek().type != TokenType.LEFT_BRACKET)
							output.add(stack.pop());
						break;
					case OPERATOR:
						if(!stack.isEmpty()) {
							Operator o = (Operator) token.value;
							int prec   = o.precedence;
							Token t1;
							while(!stack.isEmpty() && (t1 = stack.peek()).type == TokenType.OPERATOR) {
								if(prec <= ((Operator) t1.value).precedence) {
									output.add(stack.pop());
								}
							}
						}
						stack.add(token);
						break;
					case LEFT_BRACKET:
						stack.add(token);
						break;
					case RIGHT_BRACKET:
						Token t2 = null;
						while(!stack.isEmpty() && (t2 = stack.peek()).type != TokenType.LEFT_BRACKET) {
							output.add(stack.pop());
						}
						if(t2 != null) {
							if(t2.type == TokenType.LEFT_BRACKET)
								stack.pop();
							if(!stack.isEmpty() && (t2 = stack.peek()).type == TokenType.FUNCTION)
								output.add(stack.pop());
						}
						break;
					default:
						throw new IllegalStateException(
							"Encountered unknown token!");
				}
			}
			while(!stack.isEmpty()) {
				token = stack.pop();
				if(token.type == TokenType.LEFT_BRACKET ||
				   token.type == TokenType.RIGHT_BRACKET) {
					throw new IllegalStateException(
						"Brackets mismatched!");
				}
				output.add(token);
			}
			return output;
		}
		
		static final String stringify(Queue<Token> queue) {
			StringBuilder builder = new StringBuilder();
			boolean first 		  = true;
			Token token;
			while((token = queue.poll()) != null) {
				if(first) first = false;
				else 	  builder.append(' ');
				builder.append(token.toString());
			}
			return builder.toString();
		}
		
		static final double calculate(CalculationTokenizer ct) {
			Stack<Double>  stack 	= new Stack<>();
			NumberSupplier supplier = null;
			Token token;
			while(ct.hasNext()) {
				if((token = ct.next()) == null) {
					throw new IllegalStateException(
						"Token is null!");
				}
				switch(token.type) {
					case NUMBER:
						stack.add(Double.parseDouble((String) token.value));
						break;
					case OPERATOR:
						Operator op = (Operator) token.value;
						double num0 = stack.pop();
						double num1 = stack.pop();
						stack.add(op.process(num1, num0));
						break;
					case FUNCTION:
						if((supplier == null)) {
							supplier = new NumberSupplier() {
								
								@Override
								public double supply() {
									return stack.pop();
								}
								
								@Override
								public void putBack(double a) {
									stack.add(a);
								}
							};
						}
						stack.add(((MathFunction) token.value).process(supplier));
						break;
					default:
						break;
				}
			}
			return stack.firstElement();
		}
		
		static final String constRegex(String constName) {
			return "(?<!\\w)" + constName + "(?!\\w)";
		}
		
		/**
		 * Calculates a simple mathematical expression.
		 * <br><br>
		 * Simple mathematical expression is meant to be an expression that does not contain
		 * any variables, thus no values for variables can be calculated. This method is for
		 * calculating expression with numbers-only.
		 * <br><br>
		 * Operations that can be used:<br>
		 * <ul>
		 * 	<li>addition ({@code a+b})</li>
		 * 	<li>substraction ({@code a-b})</li>
		 * 	<li>multiplication ({@code a*b})</li>
		 * 	<li>division ({@code a/b})</li>
		 * 	<li>exponent ({@code a^b})</li>
		 * </ul>
		 * Functions that can be used:<br><br>
		 * <ul>
		 * 	<li>square root ({@code sqrt(a)})</li>
		 * 	<li>sinus ({@code sin(a)})</li>
		 * 	<li>cosinus ({@code cos(a)})</li>
		 * 	<li>tangens ({@code tan(a)})</li>
		 * 	<li>logarithm ({@code log(b,a)}, where b is base)</li>
		 * </ul>
		 * Constants that can be used:<br><br>
		 * <ul>
		 * 	<li>PI ({@code pi})</li>
		 * 	<li>E ({@code e})</li>
		 * </ul>
		 * <b>Note:</b> Using brackets is necessary in some cases! Ensure that all expression
		 * that can be misunderstood are in brackets in that way they are thought to be.<br><br>
		 * @param expression Mathematical expression to be calculated.
		 * @return Calculated result of the given mathematical expression.*/
		public static final double calc(String expression) {
			return calculate(
					new CalculationTokenizer(
						stringify(shuntingYard(
							new ExpressionTokenizer(
								expression.toLowerCase()
										  .replace(constRegex("pi"), STRING_PI)
										  .replace(constRegex("e"),  STRING_E)
										  .replaceAll("\\s+", ""))))));
		}
	}
}