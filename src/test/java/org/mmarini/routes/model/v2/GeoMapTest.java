package org.mmarini.routes.model.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;
import org.mmarini.routes.model.Constants;

public class GeoMapTest implements Constants {

	@Test
	public void removeNodeFromMapNode() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(site, node0);
		final MapEdge edge1 = MapEdge.create(site, node1);
		final GeoMap map = GeoMap.create().add(site).add(node0).add(node1).add(edge0).add(edge1);

		final GeoMap result = map.removeNodeFromMap(node0);
		assertThat(result, notNullValue());
		assertThat(result.getSites(), contains(site));
		assertThat(result.getNodes(), contains(node1));
		assertThat(result.getEdges(), contains(edge1));
	}

	@Test
	public void removeNodeFromMapNone() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(site, node0);
		final MapEdge edge1 = MapEdge.create(site, node1);
		final GeoMap map = GeoMap.create().add(site).add(node0).add(node1).add(edge0).add(edge1);

		final GeoMap result = map.removeNodeFromMap(MapNode.create(1, 2));
		assertThat(result, notNullValue());
		assertThat(result, sameInstance(map));
	}

	@Test
	public void removeNodeFromMapSite() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node0 = MapNode.create(10, 10);
		final MapNode node1 = MapNode.create(10, 20);
		final MapEdge edge0 = MapEdge.create(site, node0);
		final MapEdge edge1 = MapEdge.create(site, node1);
		final GeoMap map = GeoMap.create().add(site).add(node0).add(node1).add(edge0).add(edge1);

		final GeoMap result = map.removeNodeFromMap(site);
		assertThat(result, notNullValue());
		assertThat(result.getSites(), empty());
		assertThat(result.getNodes(), containsInAnyOrder(node0, node1));
		assertThat(result.getEdges(), empty());
	}

	@Test
	public void test() {
		final GeoMap map = GeoMap.create();
		assertThat(map, notNullValue());
		assertThat(map.getSites(), empty());
		assertThat(map.getNodes(), empty());
		assertThat(map.getEdges(), empty());
	}

	@Test
	public void testAddEdge() {
		final MapEdge edge = MapEdge.create(MapNode.create(0, 0), MapNode.create(10, 10));
		final GeoMap map = GeoMap.create().add(edge);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), empty());
		assertThat(map.getNodes(), empty());
		assertThat(map.getEdges(), contains(edge));

		assertThat(map.add(edge), sameInstance(map));
	}

	@Test
	public void testAddNode() {
		final MapNode node = MapNode.create(0, 0);
		final GeoMap map = GeoMap.create().add(node);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), empty());
		assertThat(map.getNodes(), contains(node));
		assertThat(map.getEdges(), empty());

		assertThat(map.add(node), sameInstance(map));
	}

	@Test
	public void testAddSite() {
		final SiteNode site = SiteNode.create(0, 0);
		final GeoMap map = GeoMap.create().add(site);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), contains(site));
		assertThat(map.getNodes(), empty());
		assertThat(map.getEdges(), empty());

		assertThat(map.add(site), sameInstance(map));
	}

	@Test
	public void testCreateGeo() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(site, node);
		final GeoMap map = GeoMap.create().add(site).add(node).add(edge);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), contains(site));
		assertThat(map.getNodes(), contains(node));
		assertThat(map.getEdges(), contains(edge));
	}

	@Test
	public void testRemoveEdge() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(site, node);
		final GeoMap map = GeoMap.create().add(site).add(node).add(edge).remove(edge);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), contains(site));
		assertThat(map.getNodes(), contains(node));
		assertThat(map.getEdges(), empty());

		assertThat(map.remove(edge), sameInstance(map));
	}

	@Test
	public void testRemoveNode() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(site, node);
		final GeoMap map = GeoMap.create().add(site).add(node).add(edge).remove(node);
		assertThat(map, notNullValue());
		assertThat(map.getSites(), contains(site));
		assertThat(map.getNodes(), empty());
		assertThat(map.getEdges(), contains(edge));

		assertThat(map.remove(node), sameInstance(map));
	}

	@Test
	public void testRemoveSite() {
		final SiteNode site = SiteNode.create(0, 0);
		final MapNode node = MapNode.create(10, 10);
		final MapEdge edge = MapEdge.create(site, node);
		final GeoMap map = GeoMap.create().add(site).add(node).add(edge);

		final GeoMap result = map.remove(site);
		assertThat(result, notNullValue());
		assertThat(result.getSites(), empty());
		assertThat(result.getNodes(), contains(node));
		assertThat(result.getEdges(), contains(edge));

		assertThat(result.remove(site), sameInstance(result));
	}
}
