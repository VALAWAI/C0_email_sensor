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
public class POP3EMailServerTestResource implements QuarkusTestResourceLifecycleManager {

	/**
	 * The name of the mongo docker image to use.
	 */
	public static final String MAIL_PIT_DOCKER_NAME = "axllent/mailpit:latest";

	/**
	 * The mongo service container.
	 */
	@SuppressWarnings("resource")
	static GenericContainer<?> mailContainer = new GenericContainer<>(DockerImageName.parse(MAIL_PIT_DOCKER_NAME))
			.withStartupAttempts(1).withEnv("MP_POP3_AUTH", "user:password").withEnv("MP_SMTP_AUTH", "user:password")
			.withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "true").withExposedPorts(1025, 1110)
			.waitingFor(Wait.forListeningPorts(1025, 1110));

	/**
	 * Start the mocked server.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> start() {

		return getConfiguration();

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

	/**
	 * Return the configuration properties of the resource.
	 *
	 * @return the configuration properties.
	 */
	public static Map<String, String> getConfiguration() {

		if (!mailContainer.isRunning()) {

			mailContainer.start();
		}

		final var config = new HashMap<String, String>();
		config.put("mail.protocol", "pop3");
		config.put("mail.secured", "false");
		config.put("mail.user.name", "user");
		config.put("mail.user.password", "password");
		config.put("mail.host", "host.docker.internal");
		config.put("mail.port", String.valueOf(mailContainer.getMappedPort(1110)));
		config.put("mail.smtp.port", String.valueOf(mailContainer.getMappedPort(1025)));
		return config;

	}

}
