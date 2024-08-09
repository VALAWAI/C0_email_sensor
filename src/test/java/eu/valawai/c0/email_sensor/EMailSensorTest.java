/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;

import eu.valawai.c0.email_sensor.mov.MOVTestResource;
import io.quarkus.logging.Log;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Test the {@link EMailSensor}.
 *
 * @see EMailSensor
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@WithTestResource(value = MOVTestResource.class)
@WithTestResource(value = EMailServerTestResource.class)
public class EMailSensorTest {

	/**
	 * The service to send e-mails.
	 */
	@Inject
	EMailSender emailSender;

	/**
	 * The queue that receive the processes e-mails.
	 */
	@Inject
	EMailQueue queue;

	/**
	 * The seconds between fetching intervals.
	 */
	@ConfigProperty(name = "c0.email_sensor.fetching_interval", defaultValue = "60")
	long fetchingInterval;

	/**
	 * The component used to send the information of the parameters that has to be
	 * changed.
	 */
	@Channel("send_change_parameters")
	@Inject
	Emitter<EMailSensorComponentParametersPayload> changeParametersSender;

	/**
	 * Should sense some e-mails.
	 */
	@Test
	public void shouldSenseEMails() {

		this.queue.clearEMails();
		final var time = Instant.now().getEpochSecond();
		final var payloads = new ArrayList<EMailPayload>();
		final var max = 20;
		for (var i = 0; i < max; i++) {

			final var payload = this.assertSendNextEMail();
			payloads.add(payload);

		}

		final var start = Instant.now().getEpochSecond();
		for (final var payload : payloads) {

			final var email = this.queue.waitUntilNextEMail(Duration.ofSeconds(this.fetchingInterval));
			assertNotNull(email);
			assertTrue(email.received_at >= time);
			payload.received_at = email.received_at;
			payload.mime_type = email.mime_type;
			assertEquals(payload, email);
		}

		assertTrue(this.queue.isEmpty());
		assertTrue(Instant.now().getEpochSecond() - start < this.fetchingInterval * 1.1);

		final var email = this.queue.waitUntilNextEMail(Duration.ofSeconds(this.fetchingInterval));
		assertNull(email);

	}

	/**
	 * Send the next e-mail.
	 *
	 * @return the sent e-mail.
	 */
	protected EMailPayload assertSendNextEMail() {

		final var payload = new EMailPayloadTest().nextModel();
		payload.addresses = payload.addresses.subList(0, 2);
		assertTrue(this.emailSender.send(payload));
		return payload;
	}

	/**
	 * Check change parameters and fetch mail in the new interval.
	 */
	@Test
	public void shouldChangeFetchingIntervalAndSenseEMails() {

		this.assertSetNewInterval(Duration.ofSeconds(1));

		try {

			this.queue.clearEMails();
			final var start = Instant.now().getEpochSecond();
			final var max = 10;
			for (var i = 0; i < max; i++) {

				final var time = Instant.now().getEpochSecond();
				final var payload = this.assertSendNextEMail();
				final var email = this.queue.waitUntilNextEMail(Duration.ofSeconds(30));
				assertNotNull(email);
				assertTrue(email.received_at >= time);
				assertTrue(email.received_at >= start + i);
				payload.received_at = email.received_at;
				payload.mime_type = email.mime_type;
				assertEquals(payload, email);
			}

			assertTrue(this.queue.isEmpty());
			assertTrue(Instant.now().getEpochSecond() - start < 30);

		} finally {

			this.assertSetNewInterval(Duration.ofSeconds(this.fetchingInterval));
		}

	}

	/**
	 * Check that the fetching interval is updated.
	 *
	 * @param newInterval the value for the new interval.
	 */
	protected void assertSetNewInterval(Duration newInterval) {

		final var parameters = new EMailSensorComponentParametersPayload();
		parameters.fetching_interval = (int) newInterval.toSeconds();
		this.changeParametersSender.send(parameters).handle((success, error) -> {

			if (error == null) {

				Log.debugv("Changed parameters to {0}.", parameters);

			} else {

				Log.errorv(error, "Cannot change the parameters to {0}.", parameters);
			}
			return null;
		});

		final var expected = String.valueOf(parameters.fetching_interval);
		for (var i = 0; i < 60 && !expected.equals(System.getProperty("C0_EMAIL_SENSOR_FETCHING_INTERVAL", "")); i++) {

			try {
				Thread.sleep(500);
			} catch (final InterruptedException ignored) {

			}
		}

	}

	/**
	 * Check not change parameters with bad parameters
	 */
	@Test
	public void shouldNotChangeFetchingIntervalWithInvalidMessage() {

		final var expected = System.getProperty("C0_EMAIL_SENSOR_FETCHING_INTERVAL");
		final var parameters = new EMailSensorComponentParametersPayload();
		parameters.fetching_interval = 0;
		final var semaphore = new Semaphore(0);
		final var errors = new ArrayList<Throwable>();
		this.changeParametersSender.send(parameters).handle((success, error) -> {

			if (error != null) {

				errors.add(error);
			}
			semaphore.release();
			return null;
		});

		try {
			semaphore.tryAcquire(30, TimeUnit.SECONDS);
		} catch (final InterruptedException ignored) {
		}

		assertTrue(errors.isEmpty());

		try {
			Thread.sleep(Duration.ofSeconds(10).toMillis());
		} catch (final InterruptedException ignored) {
		}

		assertEquals(expected, System.getProperty("C0_EMAIL_SENSOR_FETCHING_INTERVAL"));
	}

}
