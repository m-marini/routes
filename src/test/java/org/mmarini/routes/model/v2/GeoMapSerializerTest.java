package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class GeoMapSerializerTest implements Constants {

	@Test
	public void testSerializer() {
		final MapNode s0 = MapNode.create(0, 0);
		final MapNode s1 = MapNode.create(0, 100);
		final MapNode n3 = MapNode.create(0, 200);
		final MapEdge e1 = MapEdge.create(s0, n3);
		final Map<Tuple2<MapNode, MapNode>, Double> weights = GeoMap.buildWeights(Set.of(s0, s1), (a, b) -> 0.8);
		final GeoMap map = GeoMap.create(Set.of(e1), weights);

		final JsonNode json = new GeoMapSerializer(map).toJson();
		assertThat(json, notNullValue());
		assertTrue(json.isObject());

		assertThat(json.path("version").asDouble(), equalTo(map.getFrequence()));
		assertThat(json.path("frequence").asDouble(), equalTo(map.getFrequence()));

		final JsonNode sites = json.path("sites");
		assertTrue(sites.isObject());
		assertThat(sites.size(), equalTo(2));

		map.getSites().forEach(s -> {
			final JsonNode sx = sites.get(s.getId().toString());
			assertThat(sx, notNullValue());
			assertThat(sx.path("x").asDouble(), equalTo(s.getX()));
			assertThat(sx.path("y").asDouble(), equalTo(s.getY()));
		});

		final JsonNode paths = json.path("paths");
		assertTrue(paths.isArray());
		assertThat(paths.size(), equalTo(2));

		YamlUtils.toStream(paths.elements()).forEach(node -> {
			final String depId = node.path("departure").asText();
			final Optional<MapNode> dep = map.getSites().parallelStream().filter(n -> {
				return n.getId().toString().equals(depId);
			}).findAny();
			assertTrue(dep.isPresent());

			final String destId = node.path("destination").asText();
			final Optional<MapNode> dest = map.getSites().parallelStream().filter(n -> {
				return n.getId().toString().equals(destId);
			}).findAny();
			assertTrue(dest.isPresent());

			final double w = node.path("weight").asDouble();
			assertThat(w, equalTo(map.getWeight(dep.get(), dest.get())));
		});

		final JsonNode nodes = json.path("nodes");

		assertTrue(nodes.isObject());
		assertThat(nodes.size(), equalTo(1));

		final JsonNode sx = YamlUtils.toList(nodes.elements()).get(0);
		assertNotNull(sx);
		assertThat(sx.path("x").asDouble(), equalTo(0.0));
		assertThat(sx.path("y").asDouble(), equalTo(200.0));

		final JsonNode edges = json.path("edges");
		assertTrue(edges.isArray());
		assertThat(edges.size(), equalTo(1));

		map.getEdges().forEach(edge -> {
			assertTrue(YamlUtils.toStream(edges.elements()).anyMatch(ed -> {
				return ed.path("start").asText().equals(edge.getBegin().getId().toString())
						&& ed.path("end").asText().equals(edge.getEnd().getId().toString())
						&& ed.path("priority").asInt() == edge.getPriority()
						&& ed.path("speedLimit").asDouble() == edge.getSpeedLimit() * MPS_TO_KMH;
			}));
		});
	}

}
