public class GraphRelation {
	public static create(correlation) {
		def map = []
		
		correlation.indexed().collect { hi, handler ->
			handler[1].indexed().collect { oi, output ->
				if (!(output in map)) {
					map << output
					def mapIndex = map.size()-1
					correlation[hi][1][oi] = mapIndex
				} else {
					correlation[hi][1][oi] = map.indexOf(output)
				}
			}
		}

		return [correlation, map]
	}
}
