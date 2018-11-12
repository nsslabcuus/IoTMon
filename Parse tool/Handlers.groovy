import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement

public class Handlers {
	// Gets a list of all function names that are in subscribes
	public static getFromInstalled(instStmt, globalFunc) {
		def handlers = []
		switch (instStmt) {
		case { it instanceof MethodNode }:
			def stmts = instStmt.getCode().getStatements()
			for (def stmt in stmts) {
				handlers += getFromInstalled(stmt, globalFunc)
			}
			break
		case { it instanceof BlockStatement }:
			def stmts = instStmt.getStatements()
			for (def stmt in stmts) {
				handlers += getFromInstalled(stmt, globalFunc)
			}
			break
		case { it instanceof MethodCallExpression }:
			def functionName = instStmt.getMethod().getText()
			switch (functionName) {
			case "subscribe":
				handlers << parseSubscribe(instStmt)
				break
			case "schedule":
				handlers << parseSchedule(instStmt)
				break
			default:
				def func = globalFunc[functionName]
				if (!func) {
					break
				}
				handlers += getFromInstalled(func, globalFunc)
			}
			break
		case { it instanceof ExpressionStatement }:
			def e = instStmt.getExpression()
			handlers += getFromInstalled(e, globalFunc)
			break
		default:
			println instStmt
			break

		}

		return handlers
	}

	// parseSubscribe gets a MethodCallExpression object and checks for the
	// subscribes
	private static parseSubscribe(methodCall) {
		def arguments = methodCall.getArguments()

		def variable = arguments[0].getName()
		def capability
		def callback

		if (arguments.size() == 3) {
			capability = arguments[1].getText()
			callback = arguments[2].getName()
		} else if (arguments.size() == 2) {
			capability = "mode"
			callback = arguments[1].getName()
		}

		return [variable, capability, callback]
	}

	// parseSchedule parses schedules
	private static parseSchedule(methodCall) {
		def arguments = methodCall.getArguments()

		def timeVariable = arguments[0].getText()
		def callback = arguments[1].getText()

		return [timeVariable, "time", callback]
	}
}
