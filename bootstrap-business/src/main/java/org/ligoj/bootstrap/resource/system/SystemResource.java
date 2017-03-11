package org.ligoj.bootstrap.resource.system;

import java.io.File;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import org.ligoj.bootstrap.core.DateUtils;

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
		final SystemVo settings = new SystemVo();

		/**
		 * Date management
		 */
		final DateVo dateVo = new DateVo();
		settings.setDate(dateVo);
		dateVo.setDate(DateUtils.newCalendar().getTime());
		dateVo.setDefaultTimeZone(TimeZone.getDefault().getID());
		dateVo.setTimeZone(DateUtils.getApplicationTimeZone().getID());
		dateVo.setOriginalDefaultTimeZone(DateUtils.ORIGINAL_DEFAULT_TIMEZONE.getID());

		/**
		 * Memory management
		 */
		final MemoryVo memoryVo = new MemoryVo();
		settings.setMemory(memoryVo);
		memoryVo.setFreeMemory(Runtime.getRuntime().freeMemory());
		memoryVo.setMaxMemory(Runtime.getRuntime().maxMemory());
		memoryVo.setTotalMemory(Runtime.getRuntime().totalMemory());

		/**
		 * CPU management
		 */
		final CpuVo cpuVo = new CpuVo();
		settings.setCpu(cpuVo);
		cpuVo.setTotal(Runtime.getRuntime().availableProcessors());

		/**
		 * FILE management
		 */
		settings.setFiles(Arrays.stream(File.listRoots()).map(root -> {
			final FileVo fileVo = new FileVo();
			fileVo.setAbsolutePath(root.getAbsolutePath());
			fileVo.setTotalSpace(root.getTotalSpace());
			fileVo.setFreeSpace(root.getFreeSpace());
			fileVo.setUsableSpace(root.getUsableSpace());
			return fileVo;
		}).collect(Collectors.toList()));
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
