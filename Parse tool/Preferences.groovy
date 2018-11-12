import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression

import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.BlockStatement


public class Preferences {
	// parse takes in a preferences closure and returns a
	//list of inputs associated with the closure. These can be referenced
	// by the callback bodies
	public static parse(statement) {
		def inputs = []
		switch (statement) {
		case { it instanceof ConstantExpression }:
			inputs += []
			break
		case { it instanceof ArgumentListExpression }:
			for (s in statement.getExpressions()) {
				inputs += parse(s)
			}
			break
		case { it instanceof ClosureExpression }:
			def stmts = statement.getCode().getStatements()
			for (s in stmts) {
				inputs += parse(s)
			}
			break
		case { it instanceof BlockStatement }:
			for (s in statement.getStatements()) {
				inputs += parse(s)
			}
			break
		case {it instanceof ExpressionStatement }:
			inputs += parse(statement.getExpression())
			break
		case { it instanceof MethodCallExpression }:
			switch (statement.getMethodAsString()) {
			case "section":
				def args = statement.getArguments()
				inputs += parse(args)
				break
			case "input":
				def args = statement.getArguments()
				def exps = args.getExpressions()
				// Input expressions can be different depending
				// on how it's used. Input can be used like a
				// section in rare cases
				if (exps.size() == 1) {
					inputs << findInputs(args)
				} else if (exps.size() <= 3) {
					def value = exps[1].getValue()
					def type = exps[2].getValue()
					inputs << [value, type]
				}
				// A size of 4 means that there are
				// more inputs for that function
				if (exps.size() == 4) {
					inputs += parse(exps[3])
				}
				break
			default:
				throw new Exception("MethodCallExpression Broken. No route for:" + statement)
			}
			break
		default:
			throw new Exception("findInputs broken: No route for: " + statement)
		}
		return inputs
	}

	// findInputs is a helper function for parse that
	// takes in a statement and returns an array of variable names and
	// capability types associated with the input statement
	private static findInputs(preferencesExpression) {
		// Get input from a function call to a list of expressions
		def expressions = preferencesExpression.getExpressions()

		// input may be called in one of two ways, either as a map, or
		// a list, so we parse both
		if (expressions[0] instanceof NamedArgumentListExpression) {
			def input = []

			def mapexp = expressions[0].getMapEntryExpressions()
			for (expression in mapexp) {
				def key = expression.getKeyExpression()
				def value = expression.getValueExpression()
				if (key.getValue() == "name") {
					input[0] = value.getValue()
				} else if (key.getValue() == "type") {
					input[1] = value.getValue()
				}
			}

			return input

		} else if (expressions[0] instanceof MapExpression) {
			def name = expressions[1].getValue()
			def type = expressions[2].getValue()
			return [name, type]
		} else {
			def name = expressions[0].getValue()
			def type = expressions[1].getValue()
			return [name, type]
		}
	}

	// parseInputExpression takes in an ExpressionStatement and returns the
	// arguments to the `input` function call
	private static parseInputExpression(exp) {
		def arguments = exp.getExpression().getArguments()
		def name = arguments[0].getValue()
		def capability = arguments[1].getValue()

		return [name, capability]
	}
}
