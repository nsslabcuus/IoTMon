import org.codehaus.groovy.ast.expr.MethodCallExpression

import Preferences
import Outputs
import Handlers

public class Correlate {
	// sync takes the input objects in the preferences method of the run
	// method and syncs inputs from preferences with the handlers
	public static sync(runMethod, functions) {
		// Preferences object for getting handlers and description
		def prefs = getObjectFromRun(runMethod, "preferences")
		if (!prefs) {
			throw new Exception("No Preferences Method Found")
		}
	
		// Get description from definition function
		def description = getDescription(runMethod)

		def inputs = getInputArray(prefs)

		def translateFunc = { handler ->
			def hfunc = functions[handler[2]]
			Outputs.translate(handler, hfunc, inputs, functions)
		}

		def relations = getHandlers(functions)
		.stream()
		.map(translateFunc)
		.collect()

		return [description, relations]
	}

	private static getDescription(runMethod) {
		def definition = getObjectFromRun(runMethod, "definition")
		def description = getDefKey(definition, "description")
		return description
	}

	private static getInputArray(prefs) {
		def prefsCode = prefs.getArguments()[0].getCode()
		def inputs = Preferences.parse(prefsCode)
		// Insert default inputs that can be used without definition
		inputs << ["app", "capability.app"]
		inputs << ["location", "capability.location"]

		return inputs
	}

	private static getHandlers(functions) {
		// Relate the inputs to the functions found in callbacks of
		//`installed` capabilities
		def instCode = functions["installed"]?.getCode()
		def inputHandlers = Handlers.getFromInstalled(instCode, functions)
		return inputHandlers
	}

	private static getObjectFromRun(runMethod, name) {
		def mt = runMethod.getStatements()
		.stream()
		.filter({ stmt -> 
                        def e = stmt.getExpression()
			return (e instanceof MethodCallExpression) &&
				(e.getMethodAsString() == name)
		})
		.collect()

		return mt[0].getExpression()
	}

	private static getDefKey(definition, definitionKey) {
		def argList = definition.getArguments().getExpressions()[0]

		def matches = argList.getMapEntryExpressions()
		.stream()
		.filter({ e ->
			def key = e.getKeyExpression().getValue()
			return e.getKeyExpression().getValue() == definitionKey
		})
		.collect()
		
		return matches[0].getValueExpression().getValue()
	}
}
