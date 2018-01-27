package org.ligoj.bootstrap.resource.system.bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.ligoj.bootstrap.core.json.TableItem;

/**
 * Very simple JSon data binding services. Note that inheritance and raw (declared as Object) checks are not yet there.
 * No transaction or applied JSR-303 aspect interfering.
 * 
 * @author Fabrice Daugan
 * 
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/system/json-test")
public class JsonBenchResource {

	/**
	 * Return a generic item containing keyed values as list.
	 */
	@GET
	@Path("/pagine")
	public TableItem<String> pagine() {
		final TableItem<String> tableItem = new TableItem<>();
		tableItem.setRecordsFiltered(5);
		tableItem.setRecordsTotal(6);
		tableItem.setDraw("69T");
		tableItem.setData(Arrays.asList(stringsArray()));
		return tableItem;
	}

	/**
	 * Return a list of keyed values.
	 */
	@GET
	@Path("/list-generic")
	public List<KeyValueHolder<String, Integer>> keyeds() {
		final List<KeyValueHolder<String, Integer>> result = new ArrayList<>();
		result.add(new KeyValueHolder<>("key1", 1));
		result.add(new KeyValueHolder<>("key2", 2));
		return result;
	}

	/**
	 * Return a simple list of String.
	 */
	@GET
	@Path("/list-string")
	public List<String> strings() {
		return Arrays.asList(stringsArray());
	}

	/**
	 * Return a simple array of String.
	 */
	@GET
	@Path("/array-string")
	public String[] stringsArray() {
		return new String[] { "value1", "value2" };
	}

	/**
	 * Return a generic typed value.
	 */
	@GET
	@Path("/generic")
	public KeyValueHolder<String, Integer> keyed() {
		return new KeyValueHolder<>("key3", 3);
	}

	/**
	 * Return a simple bean.
	 */
	@GET
	@Path("/bean")
	public SimpleJsonBean bean() {
		final SimpleJsonBean simpleBean = new SimpleJsonBean();
		simpleBean.setKey("keyb");
		simpleBean.setValue(-1);
		return simpleBean;
	}

	/**
	 * Return a simple map.
	 */
	@GET
	@Path("/map")
	public Map<Integer, String> map() {
		final Map<Integer, String> result = new HashMap<>();
		result.put(5, "value5");
		result.put(6, "value6");
		return result;
	}

	/**
	 * A {@link GenericEntity} list with generic content.
	 */
	@GET
	@Path("/generic-entity-generic")
	public GenericEntity<List<KeyValueHolder<String, Integer>>> genericEntityGeneric() {
		final List<KeyValueHolder<String, Integer>> result = new LinkedList<>();
		result.add(new KeyValueHolder<>("key12", 12));
		result.add(new KeyValueHolder<>("key13", 13));
		return new GenericEntity<>(result, List.class);
	}

	/**
	 * A {@link GenericEntity} list with string content and using {@link Response} building.
	 */
	@GET
	@Path("/generic-entity-string")
	public Response genericEntityOfString() {
		return Response.ok(new GenericEntity<>(Arrays.asList(stringsArray()), List.class)).build();
	}

	/**
	 * A {@link Date} result.
	 */
	@GET
	@Path("/date")
	public Date date() {
		return new Date(50);
	}

	/**
	 * A {@link DateTime} result.
	 */
	@GET
	@Path("/datetime")
	public DateTime datetime() {
		return new DateTime(60);
	}

	/**
	 * A {@link GenericEntity} list with string content.
	 */
	@GET
	@Path("/bean-benchmark/{nb}")
	public JsonBenchBean beansBenchmark(@PathParam("nb") final int nb) {
		final JsonBenchBean beansBenchmark = new JsonBenchBean();

		// Build the list
		final List<SimpleJsonBean> beans = new ArrayList<>(nb);
		for (int i = nb; i-- > 0;) {
			final SimpleJsonBean bean = new SimpleJsonBean();
			bean.setKey("count" + i);
			bean.setValue(i);
			beans.add(bean);
		}

		// Attach to the root element
		beansBenchmark.setBeans(beans);
		return beansBenchmark;
	}

	@lombok.Data
	@lombok.AllArgsConstructor
	public static class KeyValueHolder<T, U> {

		private T key;

		private U value;

	}
}
