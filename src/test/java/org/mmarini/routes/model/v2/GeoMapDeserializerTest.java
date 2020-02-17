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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class GeoMapDeserializerTest implements Constants {

	@Test
	public void departureSiteNotFoundException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			GeoMapDeserializer.create().parse(getClass().getResource("/test-deserializer-no-departure.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Departure site \"x\" not found"));
	}

	@Test
	public void destinationSiteNotFoundException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			GeoMapDeserializer.create().parse(getClass().getResource("/test-deserializer-no-destination.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Destination site \"x\" not found"));
	}

	@Test
	public void missingEndNodeException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			GeoMapDeserializer.create().parse(getClass().getResource("/test-deserializer-no-end.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Missing end node"));
	}

	@Test
	public void missingStartNodeException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			GeoMapDeserializer.create().parse(getClass().getResource("/test-deserializer-no-start.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Missing start node"));
	}

	@Test
	public void oarseOneSite() throws IOException {

		final GeoMap result = GeoMapDeserializer.create()
				.parse(getClass().getResource("/test-deserializer-1-site.yaml"));

		assertThat(result, notNullValue());
		assertThat(result.getFrequence(), equalTo(1.3));

		final Set<MapNode> sites = result.getSites();
		assertThat(sites, hasSize(1));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));

		final Set<MapNode> nodes = result.getNodes();
		assertThat(nodes, hasSize(2));
		assertThat(nodes, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 50)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));

		final Set<MapEdge> edges = result.getEdges();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("begin", hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5"))),
						hasProperty("end", hasProperty("id", hasToString("816a229d-3398-3ccf-adaa-e1b119fc369b"))),
						hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS)))));

		assertThat(result.getWeights().size(), equalTo(0));
	}

	@Test
	public void parse() throws IOException {
		final URL resource = getClass().getResource("/test-deserializer.yaml");

		final GeoMap s = GeoMapDeserializer.create().parse(resource);

		assertThat(s, notNullValue());
		assertThat(s.getFrequence(), equalTo(1.3));

		final Set<MapNode> sites = s.getSites();
		assertThat(sites, hasSize(2));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapNode> nodes = s.getNodes();
		assertThat(nodes, hasSize(3));
		assertThat(nodes, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 50)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapEdge> edges = s.getEdges();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("begin", hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5"))),
						hasProperty("end", hasProperty("id", hasToString("816a229d-3398-3ccf-adaa-e1b119fc369b"))),
						hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS)))));

		final MapNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findFirst()
				.get();
		final MapNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findFirst()
				.get();
		assertThat(s.getWeight(s1, s2), equalTo(0.8));
		assertThat(s.getWeight(s2, s1), equalTo(0.5));
	}

	@Test
	public void parseFile() throws IOException {
		final GeoMap s = GeoMapDeserializer.create().parse(new File("src/test/resources/test-deserializer.yaml"));

		assertThat(s, notNullValue());
		assertThat(s.getFrequence(), equalTo(1.3));

		final Set<MapNode> sites = s.getSites();
		assertThat(sites, hasSize(2));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapNode> nodes = s.getNodes();
		assertThat(nodes, hasSize(3));
		assertThat(nodes, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 50)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapEdge> edges = s.getEdges();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("begin", hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5"))),
						hasProperty("end", hasProperty("id", hasToString("816a229d-3398-3ccf-adaa-e1b119fc369b"))),
						hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS)))));

		final MapNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findFirst()
				.get();
		final MapNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findFirst()
				.get();
		assertThat(s.getWeight(s1, s2), equalTo(0.8));
		assertThat(s.getWeight(s2, s1), equalTo(0.5));
	}

	@Test
	public void parseOldFormat() throws IOException {
		final GeoMapDeserializer deser = GeoMapDeserializer.create();

		final GeoMap result = deser.parse(getClass().getResource("/test-deserializer-old.yaml"));

		assertThat(result, notNullValue());
		assertThat(result.getFrequence(), equalTo(1.3));

		final Set<MapNode> sites = result.getSites();
		assertThat(sites, hasSize(2));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapNode> nodes = result.getNodes();
		assertThat(nodes, hasSize(3));
		assertThat(nodes, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 50)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 0)))));
		assertThat(sites, hasItem(hasProperty("location", equalTo(new Point2D.Double(0, 100)))));

		final Set<MapEdge> edges = result.getEdges();
		assertThat(edges, hasSize(1));
		assertThat(edges,
				hasItem(allOf(
						hasProperty("begin", hasProperty("id", hasToString("4ae71336-e44b-39bf-b9d2-752e234818a5"))),
						hasProperty("end", hasProperty("id", hasToString("816a229d-3398-3ccf-adaa-e1b119fc369b"))),
						hasProperty("priority", equalTo(1)), hasProperty("speedLimit", equalTo(100.0 * KMH_TO_MPS)))));

		final MapNode s1 = sites.stream()
				.filter(site -> site.getId().toString().equals("4ae71336-e44b-39bf-b9d2-752e234818a5")).findAny().get();
		final MapNode s2 = sites.stream()
				.filter(site -> site.getId().toString().equals("bdd6f09a-6468-3e63-ba85-40e2c6057a30")).findAny().get();
		assertThat(result.getWeight(s1, s2), equalTo(0.8));
		assertThat(result.getWeight(s2, s1), equalTo(0.5));
	}

	@Test
	public void wrongVersionException() throws IOException {
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			GeoMapDeserializer.create().parse(getClass().getResource("/test-deserializer-bad-version.yaml"));
		});
		assertThat(exception.getMessage(), equalTo("Version must be 1"));
	}

}
