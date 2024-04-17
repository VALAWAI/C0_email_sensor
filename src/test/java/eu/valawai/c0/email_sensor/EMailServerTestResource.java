/*
  Copyright 2022-2026 VALAWAY

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/
package eu.valawai.c0.email_sensor;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * The test resource to capture the e-mails.
 *
 * @author VALAWAI
 */
public class EMailServerTestResource implements QuarkusTestResourceLifecycleManager {

	/**
	 * The name of the mongo docker image to use.
	 */
	public static final String MAIL_TRAPPER_DOCKER_NAME = "dbck/mailtrap:latest";

	/**
	 * The user name to connect to the mail server.
	 */
	private static final String MAIL_USER_NAME = "mailtrap";

	/**
	 * The user password to connect to the mail server.
	 */
	private static final String MAIL_USER_PASSWORD = "password";

	/**
	 * The mongo service container.
	 */
	static GenericContainer<?> mailContainer = new GenericContainer<>(DockerImageName.parse(MAIL_TRAPPER_DOCKER_NAME))
			.withStartupAttempts(1).withEnv("MAILTRAP_USER", MAIL_USER_NAME)
			.withEnv("MAILTRAP_PASSWORD", MAIL_USER_PASSWORD).withExposedPorts(25, 993)
			.waitingFor(Wait.forListeningPort());

	/**
	 * Start the mocked server.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> start() {

		final var config = new HashMap<String, String>();
		config.put("mail.username", MAIL_USER_NAME);
		config.put("mail.userpassword", MAIL_USER_PASSWORD);

		if (!Boolean.parseBoolean(System.getProperty("useDevMail"))) {

			mailContainer.start();
			final var host = mailContainer.getHost();
			config.put("mail.host", host);
			config.put("mail.port", String.valueOf(mailContainer.getMappedPort(993)));
			config.put("mail.smtp.port", String.valueOf(mailContainer.getMappedPort(25)));
		}

		return config;

	}

	/**
	 * Stop the mocked server.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {

		if (mailContainer.isRunning()) {

			mailContainer.close();

		}
	}

}
