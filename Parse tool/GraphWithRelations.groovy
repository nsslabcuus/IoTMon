public class GraphWithRelations{
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
	public static create(description, relations, map) {
		def subgraphs = createSubgraph(relations, map)
		def output = ""
		output = tmpl.replace("SUBGRAPHS_GO_HERE", subgraphs)
		output = output.replace("DESCRIPTION_GOES_HERE", description)
		return output
	}

	// createSubgraphs uses the correlations to create subgraphs which relate
	// inputs to outputs
	private static createSubgraph(relations, map) {
		def subgraphOutput = ""

		subgraphOutput += "subgraph cluster_g0 {\n"
		map.indexed().collect({ i, mapObj ->
			def outStr = mapObj[0] + ":" + mapObj[2]
			subgraphOutput += "o${i} [label=\"${outStr}\"]\n"
		})

		relations.indexed().collect({ i, relation ->
			def inpStr = relation[0][0] + ":" + relation[0][1]
			subgraphOutput +=  "n${i} [label=\"${inpStr}\"]\n"
			relation[1].collect({ outputI ->
				subgraphOutput += "n${i} -> o${outputI}\n"
			})
		})

		subgraphOutput += "}\n"

		return subgraphOutput;
	}
}
