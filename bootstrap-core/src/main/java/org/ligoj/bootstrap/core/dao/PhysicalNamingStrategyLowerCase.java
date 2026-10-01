/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

/**
 * Snake case strategy inherited from Hibernate: camel case names are converted to lower case words joined by
 * <code>_</code>, such as <code>firstName</code> to <code>first_name</code>. Quoted identifiers are kept as is.
 */
public class PhysicalNamingStrategyLowerCase extends org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl {
}
