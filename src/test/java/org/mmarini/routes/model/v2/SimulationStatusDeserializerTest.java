package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class SimulationStatusDeserializerTest implements Constants {

	@Test
	public void departureSiteNotFoundException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SimulationStatusDeserializer.create().parse(getClass().getResource("/test-deserializer-no-departure.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Departure site \"x\" not found"));
	}

	@Test
	public void destinationSiteNotFOundException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SimulationStatusDeserializer.create()
					.parse(getClass().getResource("/test-deserializer-no-destination.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Destination site \"x\" not found"));
	}

	@Test
	public void missingEndNodeException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SimulationStatusDeserializer.create().parse(getClass().getResource("/test-deserializer-no-end.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Missing end node"));
	}

	@Test
	public void missingStartNodeException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SimulationStatusDeserializer.create().parse(getClass().getResource("/test-deserializer-no-start.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Missing start node"));
	}

	@Test
	public void mustBeMore1SiteException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SimulationStatusDeserializer.create().parse(getClass().getResource("/test-deserializer-1-site.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("There must be at least two sites"));
	}

	@Test
	public void parseOldFormat() throws IOException {
		final SimulationStatusDeserializer deser = SimulationStatusDeserializer.create();

		final SimulationStatus result = deser.parse(getClass().getResource("/test-deserializer-old.yaml"));

		assertThat(result, notNullValue());
		assertThat(result.getFrequence(), equalTo(1.3));

		final Set<SiteNode> sites = result.getMap().getSites();
		assertThat(sites, hasSize(2));
		assertThat(sites, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 0))))));
		assertThat(sites, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 100))))));

		final Set<MapNode> nodes = result.getMap().getNodes();
		assertThat(nodes, hasSize(1));
		assertThat(nodes, hasItem(allOf(hasProperty("location", equalTo(new Point2D.Double(0, 50))))));

		final Set<EdgeTraffic> edges = result.getTraffics();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("edge",
								hasProperty("begin",
										hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5")))),
						hasProperty("edge",
								hasProperty("end",
										hasProperty("id", hasToString("bdd6f09a-6468-3e63-ba85-40e2c6057a30")))),
						hasProperty("edge", hasProperty("priority", equalTo(1))),
						hasProperty("edge", hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS))))));

		final SiteNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findFirst()
				.get();
		final SiteNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findFirst()
				.get();
		assertThat(result.getWeight(s1, s2), equalTo(0.8));
		assertThat(result.getWeight(s2, s1), equalTo(0.5));
	}

	@Test
	public void test() throws IOException {
		final SimulationStatus s = SimulationStatusDeserializer.create()
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

		final Set<EdgeTraffic> edges = s.getTraffics();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("edge",
								hasProperty("begin",
										hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5")))),
						hasProperty("edge",
								hasProperty("end",
										hasProperty("id", hasToString("bdd6f09a-6468-3e63-ba85-40e2c6057a30")))),
						hasProperty("edge", hasProperty("priority", equalTo(1))),
						hasProperty("edge", hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS))))));

		final SiteNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findFirst()
				.get();
		final SiteNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findFirst()
				.get();
		assertThat(s.getWeight(s1, s2), equalTo(0.8));
		assertThat(s.getWeight(s2, s1), equalTo(0.5));
	}

}
