// Imports
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases

import Correlate
import GraphRelation
import GraphWithRelations
import Graph

def main() {
	if (args.size() < 1) {
		println "usage: groovy parse.groovy [name of file]"
		return
	}

	// Get correlations
	def fileData = new File(args[0])
	def correlationsData = getRelations(fileData)

	// Unpack data
	def description = correlationsData[0]
	def relations = correlationsData[1]
	def gr = GraphRelation.create(relations)
	println GraphWithRelations.create(description, gr[0], gr[1])

	//println Graph.create(description, relations)
}

// findFunctions maps MethodNodes to names in a dictionary
def generateFunctionMap(methodArray) {
	def functionMap = [:]
	for (method in methodArray) {
		functionMap[method.getName()] = method
	}

	return functionMap
}

// getRelations takes a file and outputs the relations between them
def getRelations(inputFile) {
	CompilationUnit cu = new CompilationUnit()
	cu.addSource(inputFile)
	cu.compile(Phases.SEMANTIC_ANALYSIS)

	def AST = cu.getAST().getClasses()[0]
	def methods = generateFunctionMap(AST.getMethods())
	def runMethod = methods["run"].getCode()

	def corr = Correlate.sync(runMethod, methods)
	return corr
}

main()
