package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemMfaDevice;

/**
 * {@link SystemMfaDevice} repository.
 */
public interface SystemMfaDeviceRepository extends RestRepository<SystemMfaDevice, Integer> {

	/**
	 * Return the devices of a user, ordered by name.
	 *
	 * @param user The owner login.
	 * @return The devices of this user.
	 */
	List<SystemMfaDevice> findAllByUserOrderByName(String user);

	/**
	 * Return a device of a user by its identifier.
	 *
	 * @param id   The device identifier.
	 * @param user The owner login.
	 * @return The device, or <code>null</code> when not owned by this user.
	 */
	SystemMfaDevice findByIdAndUser(int id, String user);

	/**
	 * Return a device of a user by its name.
	 *
	 * @param user The owner login.
	 * @param name The device name.
	 * @return The device, or <code>null</code>.
	 */
	SystemMfaDevice findByUserAndName(String user, String name);

	/**
	 * Count the devices of a user.
	 *
	 * @param user The owner login.
	 * @return The number of registered devices.
	 */
	int countByUser(String user);

	/**
	 * Return the oldest device of a user.
	 *
	 * @param user The owner login.
	 * @return The first registered device, or <code>null</code>.
	 */
	SystemMfaDevice findFirstByUserOrderByIdAsc(String user);
}
