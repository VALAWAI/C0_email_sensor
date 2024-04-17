/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The component that is used to send the e-mails.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ApplicationScoped
public class EMailSensor {

	/**
	 * The service to fetch e-mails.
	 */
	@Inject
	EMailFetcher fetcher;

	/**
	 * The event bus.
	 */
	@Inject
	Vertx vertx;

	/**
	 * The seconds between fetching intervals.
	 */
	@ConfigProperty(name = "c0.email_sensor.fetching_interval", defaultValue = "60")
	long fetchingInterval;

	/**
	 * The identifier of the last timer task.
	 */
	long timerId = -1;

	/**
	 * Called when the component is started. Thus this start the sensing process.
	 *
	 * @param event that contains the start status.
	 */
	public void handle(@Observes StartupEvent event) {

		this.startTimer();
	}

	/**
	 * Start the timer to fetch new e-mails.
	 */
	private void startTimer() {

		this.timerId = this.vertx.setTimer(Duration.ofSeconds(this.fetchingInterval).toMillis(), this::handle);
		Log.debugv("Starting timer with {0} seconds to fetch new e-mails", this.fetchingInterval);
	}

	/**
	 *
	 */
	private void handle(long timerId) {

		this.vertx.executeBlocking(() -> {

			final var messages = this.fetcher.fetchUnreadEMails();
			Log.debugv("C0_Email_sensor: Fetched the messages {0}", messages);
			for (final var message : messages) {

				// add log sensed message
				// emitter.send(message).

			}
			return null;

		}).subscribe().with(result -> {

			this.startTimer();

		}, error -> {

			Log.errorv("ErrorStarting timer with {0} seconds to fetch new e-mails", this.fetchingInterval);
			this.startTimer();

		});

	}

}
