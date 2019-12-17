package org.mmarini.routes.model.v2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mmarini.routes.model.Constants;
import org.mmarini.routes.model.YamlUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class SimulationStatusSerializerTest implements Constants {

	SimulationStatus create() {
		final SiteNode s1 = SiteNode.create(0, 0);
		final SiteNode s2 = SiteNode.create(0, 100);
		final MapNode n1 = MapNode.create(0, 50);
		final MapEdge e1 = MapEdge.create(s1, s2);
		final GeoMap map = GeoMap.create().add(e1).add(n1).add(s1).add(s2);
		final SimulationStatus result = SimulationStatus.create().setGeoMap(map).addWeight(s1, s2, 1).addWeight(s2, s1,
				0.5);
		return result;
	}

	@Test
	public void testSerializer() {
		final SimulationStatus status = create();
		final JsonNode json = new SimulationStatusSerializer(status).toJson();
		assertThat(json, notNullValue());
		assertTrue(json.isObject());

		final JsonNode def = json.path("default");
		assertTrue(def.isObject());
		assertThat(def.path("frequence").asDouble(), equalTo(status.getFrequence()));

		final JsonNode sites = json.path("sites");
		assertTrue(sites.isObject());
		assertThat(sites.size(), equalTo(2));

		status.getMap().getSites().forEach(s -> {
			final JsonNode s0 = sites.get(s.getId().toString());
			assertThat(s0, notNullValue());
			assertThat(s0.path("x").asDouble(), equalTo(s.getX()));
			assertThat(s0.path("y").asDouble(), equalTo(s.getY()));
		});

		final JsonNode paths = json.path("paths");
		assertTrue(paths.isArray());
		assertThat(paths.size(), equalTo(2));

		status.getMap().getSites().forEach(from -> {
			status.getMap().getSites().forEach(to -> {
				status.getWeight(from, to).ifPresent(w -> {
					assertTrue(YamlUtils.toStream(paths.elements()).anyMatch(path -> {
						return path.path("departure").asText().equals(from.getId().toString())
								&& path.path("destination").asText().equals(to.getId().toString())
								&& path.path("weight").asDouble() == w.doubleValue();
					}));
				});
			});
		});

		final JsonNode nodes = json.path("nodes");
		assertTrue(nodes.isObject());
		assertThat(nodes.size(), equalTo(1));

		status.getMap().getNodes().forEach(s -> {
			final JsonNode s0 = nodes.get(s.getId().toString());
			assertThat(s0, notNullValue());
			assertThat(s0.path("x").asDouble(), equalTo(s.getX()));
			assertThat(s0.path("y").asDouble(), equalTo(s.getY()));
		});

		final JsonNode edges = json.path("edges");
		assertTrue(edges.isArray());
		assertThat(edges.size(), equalTo(1));

		status.getMap().getEdges().forEach(edge -> {
			assertTrue(YamlUtils.toStream(edges.elements()).anyMatch(ed -> {
				return ed.path("start").asText().equals(edge.getBegin().getId().toString())
						&& ed.path("end").asText().equals(edge.getEnd().getId().toString())
						&& ed.path("priority").asInt() == edge.getPriority()
						&& ed.path("speedLimit").asDouble() == edge.getSpeedLimit() * MPS_TO_KMH;
			}));
		});
	}

}
