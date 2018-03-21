/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.core.json.PaginationJson;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.test.WineRepository;
import org.ligoj.bootstrap.model.test.Wine;
import org.ligoj.bootstrap.resource.system.bench.WineVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Wine Business Layer
 */
@Path("/test/crud")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class CrudTestResource {

	@Autowired
	private PaginationJson paginationJson;

	@Autowired
	private CsvForJpa csvForJpa;

	@Autowired
	private WineRepository repository;

	/**
	 * Ordered columns.
	 */
	public static final Map<String, String> ORM_MAPPING = new HashMap<>();
	static {
		ORM_MAPPING.put("id", "id");
		ORM_MAPPING.put("name", "name");
	}

	/**
	 * Converter from {@link WineVo} to {@link WineVo}
	 */
	public static WineVo toVo(final Wine wineTbl) {
		if (wineTbl == null) {
			return null;
		}
		final WineVo wine = new WineVo();
		wine.setCountry(wineTbl.getWneCnty());
		wine.setDescription(wineTbl.getWneDesc());
		wine.setGrapes(wineTbl.getWneGrpe());
		wine.setId(wineTbl.getId());
		wine.setName(wineTbl.getName());
		wine.setPicture(wineTbl.getWnePict());
		wine.setRegion(wineTbl.getWneRegn());
		wine.setYear(wineTbl.getWneYear());
		return wine;
	}

	/**
	 * Converter from {@link WineVo} to {@link WineVo}
	 */
	public static Wine toEntity(final WineVo wine) {
		final Wine wineTbl = new Wine();
		wineTbl.setWneCnty(wine.getCountry());
		wineTbl.setWneDesc(wine.getDescription());
		wineTbl.setWneGrpe(wine.getGrapes());
		wineTbl.setId(wine.getId());
		wineTbl.setName(wine.getName());
		wineTbl.setWnePict(wine.getPicture());
		wineTbl.setWneRegn(wine.getRegion());
		wineTbl.setWneYear(wine.getYear());
		return wineTbl;
	}

	/**
	 * Retrieve all {@link WineVo} with pagination
	 * 
	 * @param uriInfo
	 *            Request parameters.
	 * @return List of {@link WineVo}.
	 */
	@GET
	public TableItem<WineVo> findAll(@Context final UriInfo uriInfo) {
		final Page<Wine> findAll = repository.findAll(paginationJson.getPageRequest(uriInfo, ORM_MAPPING));

		// apply pagination
		return paginationJson.applyPagination(uriInfo, findAll, CrudTestResource::toVo);
	}

	/**
	 * Retrieve all {@link WineVo} with pagination and explicit query using alias.
	 * 
	 * @param uriInfo
	 *            Request parameters.
	 * @return List of {@link WineVo}.
	 */
	@GET
	@Path("query/alias")
	public TableItem<WineVo> findAllQueryAlias(@Context final UriInfo uriInfo) {
		final Page<Wine> findAll = repository.findAllQueryAlias(paginationJson.getPageRequest(uriInfo, ORM_MAPPING));

		// apply pagination
		return paginationJson.applyPagination(uriInfo, findAll, CrudTestResource::toVo);
	}

	/**
	 * Retrieve an element from its identifier.
	 * 
	 * @param id
	 *            Element's identifier.
	 * @return Found element. May be <tt>null</tt>.
	 */
	@GET
	@Path("{id:\\d+}")
	public WineVo findById(@PathParam("id") final int id) {
		return toVo(repository.findOne(id));
	}

	/**
	 * Retrieve all {@link WineVo} corresponding to the query without pagination
	 * 
	 * @param name
	 *            : query parameter.
	 * @return List of corresponding {@link WineVo}.
	 */
	@GET
	@Path("{query}")
	public List<WineVo> findByName(@PathParam("query") final String name) {
		return repository.findByNameContainingIgnoreCase(name).stream().map(CrudTestResource::toVo).collect(Collectors.toList());
	}

	/**
	 * Create a new wine
	 * 
	 * @param wine
	 *            Vo which will be parsed and persisted
	 * @return identifier of created object.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public int create(final WineVo wine) {
		return toVo(repository.saveAndFlush(toEntity(wine))).getId();
	}

	/**
	 * Update wine from its ID.
	 * 
	 * @param wine
	 *            data that will be updated
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(final WineVo wine) {
		repository.save(toEntity(wine));
	}

	/**
	 * Delete wine from its ID
	 * 
	 * @param id
	 *            : ID of the wine to delete
	 */
	@DELETE
	@Path("{id:\\d+}")
	public void remove(@PathParam("id") final int id) {
		repository.deleteById(id);
	}

	/**
	 * Delete all wines.
	 */
	@DELETE
	public void removeAll() {
		repository.deleteAll();
	}

	/**
	 * Reset the content of base with a fresh new list of entities.
	 */
	@POST
	@Path("reset")
	public void reset() throws IOException {
		repository.deleteAllInBatch();
		csvForJpa.insert("csv/demo", Wine.class);
	}
}
