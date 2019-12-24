//
// Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//   END OF TERMS AND CONDITIONS

package org.mmarini.routes.model.v2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mmarini.routes.model.Constants;

/**
 *
 */
public class Simulator implements Constants {
	/**
	 * Select the edge with priority.
	 *
	 * @param edgesInfo the list of edge sorted by simulation time
	 * @return
	 */
	static EdgeTraffic selectByPriority(final List<EdgeTraffic> edgesInfo) {
		// Compute the time limit
		final double timeLimit = edgesInfo.get(0).getTime() + REACTION_TIME;
		// Filter the conflicting edges
		final List<EdgeTraffic> filtered = edgesInfo.stream().filter(ei -> ei.getTime() <= timeLimit)
				.collect(Collectors.toList());
		// Compute the max edge priority
		final int priority = filtered.stream().mapToInt(f -> f.getEdge().getPriority()).max().getAsInt();
		// Filter the max priority edges
		final List<EdgeTraffic> maxPriority = filtered.stream().filter(f -> f.getEdge().getPriority() == priority)
				.collect(Collectors.toList());
		// Select for edge coming from right direction
		final Optional<EdgeTraffic> selected = maxPriority.stream().filter(edge -> maxPriority.stream()
				.filter(f -> f != edge).allMatch(other -> edge.getEdge().cross(other.getEdge()) > 0)).findFirst();
		final EdgeTraffic result = selected.orElseGet(() -> maxPriority.get(0));
		return result;
	}

	private final SimulationStatus status;
	private final double interval;

	/**
	 *
	 * @param status
	 * @param interval
	 */
	protected Simulator(final SimulationStatus status, final double interval) {
		super();
		this.status = status;
		this.interval = interval;
	}

	Simulator computeConnectionMatrix() {
		return this;
	}

	/**
	 *
	 * @return
	 */
	public SimulationStatus simulate() {
		return null;
	}
}
