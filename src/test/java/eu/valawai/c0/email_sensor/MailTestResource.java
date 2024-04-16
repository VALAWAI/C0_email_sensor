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
public class MailTestResource implements QuarkusTestResourceLifecycleManager {

	/**
	 * The name of the mongo docker image to use.
	 */
	public static final String MAIL_TRAPPER_DOCKER_NAME = "dbck/mailtrap:latest";

	/**
	 * The mongo service container.
	 */
	static GenericContainer<?> mailContainer = new GenericContainer<>(DockerImageName.parse(MAIL_TRAPPER_DOCKER_NAME))
			.withStartupAttempts(1).withEnv("MAILTRAP_USER", "mailtrap").withEnv("MAILTRAP_PASSWORD", "password")
			.withExposedPorts(587, 993).waitingFor(Wait.forListeningPort());

	/**
	 * Start the mocked server.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> start() {

		final var config = new HashMap<String, String>();

		if (Boolean.parseBoolean(System.getProperty("useDevMailtrap"))) {

			config.put("quarkus.mailer.host", "host.docker.internal");
			config.put("mail.host", "host.docker.internal");

		} else {

			mailContainer.start();
			config.put("quarkus.mailer.host", mailContainer.getHost());
			config.put("quarkus.mailer.port", String.valueOf(mailContainer.getMappedPort(587)));
			config.put("mail.host", mailContainer.getHost());
			config.put("mail.port", String.valueOf(mailContainer.getMappedPort(993)));
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
