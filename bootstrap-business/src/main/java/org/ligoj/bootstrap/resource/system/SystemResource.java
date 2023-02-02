/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import java.io.File;
import java.util.Arrays;
import java.util.TimeZone;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.ligoj.bootstrap.core.DateUtils;
import org.springframework.stereotype.Service;

/**
 * Manage global configuration, {@link TimeZone},... configurations.
 */
@Service
@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

	/**
	 * Return the system details.
	 * 
	 * @return the system details.
	 */
	@GET
	public SystemVo getConfiguration() {
		final var settings = new SystemVo();

		/*
		 * Date management
		 */
		final var dateVo = new DateVo();
		settings.setDate(dateVo);
		dateVo.setDate(DateUtils.newCalendar().getTime());
		dateVo.setDefaultTimeZone(TimeZone.getDefault().getID());
		dateVo.setTimeZone(DateUtils.getApplicationTimeZone().getID());
		dateVo.setOriginalDefaultTimeZone(DateUtils.ORIGINAL_DEFAULT_TIMEZONE.getID());

		/*
		 * Memory management
		 */
		final var memoryVo = new MemoryVo();
		settings.setMemory(memoryVo);
		memoryVo.setFreeMemory(Runtime.getRuntime().freeMemory());
		memoryVo.setMaxMemory(Runtime.getRuntime().maxMemory());
		memoryVo.setTotalMemory(Runtime.getRuntime().totalMemory());

		/*
		 * CPU management
		 */
		final var cpuVo = new CpuVo();
		settings.setCpu(cpuVo);
		cpuVo.setTotal(Runtime.getRuntime().availableProcessors());

		/*
		 * FILE management
		 */
		settings.setFiles(Arrays.stream(File.listRoots()).map(root -> {
			final var fileVo = new FileVo();
			fileVo.setAbsolutePath(root.getAbsolutePath());
			fileVo.setTotalSpace(root.getTotalSpace());
			fileVo.setFreeSpace(root.getFreeSpace());
			fileVo.setUsableSpace(root.getUsableSpace());
			return fileVo;
		}).toList());
		return settings;
	}

	/**
	 * Update the application time zone.
	 * 
	 * @param id
	 *            The new {@link TimeZone} identifier.
	 */
	@PUT
	@Path("timezone/application")
	public void setApplicationTimeZone(final String id) {
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone(id));
	}

	/**
	 * Update the default time zone.
	 * 
	 * @param id
	 *            The new default {@link TimeZone} identifier.
	 */
	@PUT
	@Path("timezone/default")
	public void setTimeZone(final String id) {
		TimeZone.setDefault(TimeZone.getTimeZone(id));
	}

}
