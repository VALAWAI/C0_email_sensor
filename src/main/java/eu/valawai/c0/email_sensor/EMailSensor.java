/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import eu.valawai.c0.email_sensor.mov.LogService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

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
	 * The component to send log messages.
	 */
	@Inject
	LogService log;

	/**
	 * The component to validate a {@code Payload}.
	 */
	@Inject
	Validator validator;

	/**
	 * The channel to notify to the sensed e-mails.
	 */
	@Channel("send_email")
	@Inject
	Emitter<EMailPayload> notifier;

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
	 * Called when a has to change a parameters of the component.
	 *
	 * @see EMailSensorComponentParametersPayload
	 *
	 * @param msg with the parameters to be changed.
	 *
	 * @return the result if the message process.
	 */
	@Incoming("change_parameters")
	public CompletionStage<Void> changeParameters(Message<JsonObject> msg) {

		try {

			final var payload = msg.getPayload();
			final var parameters = payload.mapTo(EMailSensorComponentParametersPayload.class);
			final var violations = this.validator.validate(parameters);
			if (violations.isEmpty()) {

				System.setProperty("C0_EMAIL_SENSOR_FETCHING_INTERVAL", String.valueOf(parameters.fetching_interval));
				this.fetchingInterval = parameters.fetching_interval;
				this.vertx.cancelTimer(this.timerId);
				this.handle(this.timerId);
				this.log.infoWithPayload(parameters, "Changed component parameters.");
				return msg.ack();

			} else {

				this.log.errorWithPayload(payload, "Bad change parameters message, because {0}", violations);
				return msg.nack(new ConstraintViolationException(violations));

			}

		} catch (final Throwable error) {

			Log.errorv(error, "Unexpected change parameters message {0}.", msg.getPayload());
			this.log.errorWithPayload(msg.getPayload(), "Bad change parameters message, because {0}",
					error.getMessage());
			return msg.nack(error);

		}
	}

	/**
	 * The function that fetch the e-mails
	 */
	private void handle(long timerId) {

		this.vertx.executeBlocking(() -> {

			final var messages = this.fetcher.fetchUnreadEMails();
			for (final var message : messages) {

				this.notifier.send(message).handle((success, error) -> {

					if (error == null) {

						this.log.infoWithPayload(message, "Sensed the a new e-mail.");
						Log.debugv("Sent email {0}.", message);

					} else {

						Log.errorv(error, "Cannot send the e-mail {0}.", message);
					}
					return null;
				});

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
