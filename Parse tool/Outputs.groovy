import org.codehaus.groovy.ast.MethodNode

import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression


public class Outputs {
	// which input goes to which outputs
	public static translate(input, function, allInp, allFuncs) {
		def capVar = eleFromList(input[0], allInp)
		def inputCapability = capVar[1]
		def outputs = findOutputs(function, allInp, allFuncs)
		def nonDupes = removeDuplicateElements(outputs)

		return [[inputCapability, input[1]], nonDupes]
	}

	// findOutputs is called recursively on a statement or expression until all
	// `MethodCallExpression`s are found that have a variable from the list
	private static findOutputs(stmt, allInp, allFuncs) {
		def outputs = []

		switch (stmt) {
			// When a method is called, see if the method name is
			// an input
			// if not: call method and parse it to see if inputs
			// appear
			case { it instanceof MethodCallExpression }:
				def rec = stmt.getReceiver().getName()
				def met = stmt.getMethodAsString()
				def ivar = eleFromList(rec, allInp)

				// SmartThings built-ins
				if (met == "setLocationMode" ||
				(rec == "location" && met == "setMode")) {

					outputs << ["locationMode",
						    "location",
						    "setLocationMode"]

				} else if (met == "runIn") {
					def args = stmt.getArguments()[1]
					def funcName= args.getValue()
					def func = allFuncs[funcName]
					outputs += findOutputs(func, allInp, allFuncs)

				} else if (met == "sendNotificationToContacts") {
					outputs << ["sendAction", "send", "sendNotificationToContacts"]
				} else if (met == "sendPush") {
					outputs << ["sendAction", "send", "sendpush"]
				} else if (met == "sendSms") {
					outputs << ["sendAction", "send", "sendSms"]
				} else if (rec != "this" && ivar) {
					outputs << [ivar[1], ivar[0], met]
				} else if (rec == "this") {
					// When `this` is reciever, call function
					// and get outputs
					def f = allFuncs[met]
					outputs += findOutputs(f, allInp, allFuncs)
				}

				break
			case { it instanceof DeclarationExpression }:
				//TODO: Do I need to handle this?
				break

			case { it instanceof VariableExpression }:
				// TODO: Maybe a variable is used, but no method is
				// called on it
				break

			case { it instanceof BinaryExpression }:
				def le = stmt.getLeftExpression()
				def re = stmt.getRightExpression()
				outputs += findOutputs(le, allInp, allFuncs)
				outputs += findOutputs(re, allInp, allFuncs)
				break

			case { it instanceof IfStatement }:
				def is = stmt.getIfBlock()
				def eb = stmt.getElseBlock()
				outputs += findOutputs(is, allInp, allFuncs)
				outputs += findOutputs(eb, allInp, allFuncs)
				break

			case { it instanceof ExpressionStatement }:
				def e = stmt.getExpression()
				outputs += findOutputs(e, allInp, allFuncs)
				break

			case { it instanceof BlockStatement }:
				for (s in stmt.getStatements()) {
					outputs += findOutputs(s, allInp, allFuncs)
				}
				break

			case { it instanceof SwitchStatement }:
				for (c in stmt.getCaseStatements()) {
					outputs += findOutputs(c, allInp, allFuncs)
				}
				break

			case { it instanceof MethodNode }:
				def c = stmt.getCode()
				outputs += findOutputs(c, allInp, allFuncs)
				break

			default:
				//println "Do not understand how to parse: ${statement}"
				break
		}

		return outputs
	}

	// TODO: Might make into map so that I don't have to go through each input
	private static def eleFromList(target, allInp) {
		return allInp.find { it[0] == target }
	}

	// Remove
	private static def removeDuplicateElements(outputs) {
		def nonDuplicates = []
		for (output in outputs) {
			if (!(nonDuplicates.contains(output))) {
				nonDuplicates << output
			}
		}

		return nonDuplicates
	}
}
