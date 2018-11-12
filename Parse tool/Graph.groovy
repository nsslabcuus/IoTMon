public class Graph {
	// Template Output
	private static tmpl = "// DESCRIPTION_GOES_HERE \n" +
		"digraph smartthingsgraph {\n" +
		"rankdir=LR;\n" + 
		"fontname=\"sans-serif\";\n" +
		"penwidth=\"0.1\";\n" +
		"edge [fontname=\"sans-serif\",\n" +
		"fontsize=10, \n" +
		"colorscheme=\"blues3\",\n" +
		"color=2,\n" +
		"fontcolor=3];\n" +
		"node [fontname=\"sans-serif\",\n" +
		"fontsize=15,\n" +
		"fillcolor=\"1\",\n" +
		"colorscheme=\"blues4\",\n" +
		"color=\"2\",\n" +
		"fontcolor=\"4\",\n" +
		"style=\"filled\",\n" +
		"penwidth=\"2.0\"];\n" +
		"SUBGRAPHS_GO_HERE" +
		"}";
	
	// creat uses the template dot file and the createSubgraphs function to
	// generate a graph string. This can be compiled into a png.
	public static create(description, relations) {
		def subgraphs = createSubgraphs(relations)
		def output = ""
		output = tmpl.replace("SUBGRAPHS_GO_HERE", subgraphs)
		output = output.replace("DESCRIPTION_GOES_HERE", description)
		return output
	}

	// createSubgraphs uses the correlations to create subgraphs which relate
	// inputs to outputs
	private static createSubgraphs(relations) {
		def subgraphOutput = ""

		relations.indexed().collect({ i, relation ->
			subgraphOutput += createSubgraph(relation, i)
		})

		return subgraphOutput;
	}

	// createSubgraph creates a new subgraph which deals with a new input
	private static createSubgraph(relation, ri) {
		def sgOut = ""

		def inputs = relation[0]
		def outputs = relation[1]
		def input = inputs[0] + ":" + inputs[1]

		sgOut += "subgraph cluster_g${ri} {\n"
		sgOut += "n${ri}0 [label=\"${input}\"];\n"
		
		for (def oi = 1; oi <= outputs.size(); oi++) {
			sgOut += createRelation(outputs[0], ri, oi)
		}

		sgOut += "}\n"

		return sgOut
	}

	// createRelation creates the relation for the subgraph cluster
	private static createRelation(outputs, ri, oi) {
		def relOut = ""

		def output = outputs[0]+ ":" + outputs[2]

		relOut += "n${ri}${oi} [shape=\"box\", label=\"${output}\"];\n"
		relOut += "n${ri}0 -> n${ri}${oi}\n"

		return relOut
	}


}
