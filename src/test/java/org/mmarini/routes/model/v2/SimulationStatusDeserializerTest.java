package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class SimulationStatusDeserializerTest implements Constants {

	@Test
	public void test() throws IOException {
		final SimulationStatus s = new SimulationStatusDeserializer()
				.parse(getClass().getResource("/test-deserializer.yaml"));
		assertThat(s, notNullValue());
		assertThat(s.getFrequence(), equalTo(1.3));

		final Set<SiteNode> sites = s.getMap().getSites();
		assertThat(sites, hasSize(2));
		assertThat(sites, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 0))))));
		assertThat(sites, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 100))))));

		final Set<MapNode> nodes = s.getMap().getNodes();
		assertThat(nodes, hasSize(1));
		assertThat(nodes, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 50))))));

		final Set<MapEdge> edges = s.getMap().getEdges();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("begin", hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5"))),
						hasProperty("end", hasProperty("id", hasToString("bdd6f09a-6468-3e63-ba85-40e2c6057a30"))),
						hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS)))));

		final SiteNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findFirst()
				.get();
		final SiteNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findFirst()
				.get();
		assertThat(s.getWeight(s1, s2), equalTo(Optional.<Double>of(0.8)));
		assertThat(s.getWeight(s2, s1), equalTo(Optional.<Double>of(0.5)));
	}

}
