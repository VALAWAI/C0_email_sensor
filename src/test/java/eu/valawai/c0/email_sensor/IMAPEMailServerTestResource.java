/*
  Copyright 2022-2026 VALAWAI

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
public class IMAPEMailServerTestResource implements QuarkusTestResourceLifecycleManager {

	/**
	 * The name of the mongo docker image to use.
	 */
	public static final String MAIL_TRAPPER_DOCKER_NAME = "dbck/mailtrap:latest";

	/**
	 * The mongo service container.
	 */
	@SuppressWarnings("resource")
	static GenericContainer<?> mailContainer = new GenericContainer<>(DockerImageName.parse(MAIL_TRAPPER_DOCKER_NAME))
			.withStartupAttempts(1).withEnv("MAILTRAP_USER", "user").withEnv("MAILTRAP_PASSWORD", "password")
			.withExposedPorts(25, 993).waitingFor(Wait.forListeningPorts(25, 993));

	/**
	 * Start the mocked server.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> start() {

		final var config = new HashMap<String, String>();
		config.put("mail.protocol", "imaps");
		config.put("mail.secured", "true");
		config.put("mail.user.name", "user");
		config.put("mail.user.password", "password");
		config.put("mail.host", "host.docker.internal");

		if (Boolean.parseBoolean(System.getProperty("useDevMail"))) {

			config.put("mail.port", "1993");
			config.put("mail.smtp.port", "1025");

		} else {

			if (!mailContainer.isRunning()) {

				mailContainer.start();
			}
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
