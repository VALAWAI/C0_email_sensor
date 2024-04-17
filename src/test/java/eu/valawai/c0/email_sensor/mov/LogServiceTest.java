/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor.mov;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;

/**
 * Test the log service.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@QuarkusTestResource(MOVTestResource.class)
public class LogServiceTest {

	/**
	 * The service to test.
	 */
	@Inject
	LogService service;

	/**
	 * Test debug message.
	 */
	@Test
	public void shouldSendDebugMessage() {

		final var value = UUID.randomUUID().toString();
		final var message = "Test " + value + " 2";
		this.service.debug("Test {0} {1}", value, 2);
		this.checkLogStored(LogLevel.DEBUG, null, message, Duration.ofSeconds(30));

	}

	/**
	 * Check the log message is stored.
	 *
	 * @param level    of the log message.
	 * @param message  of teh log message.
	 * @param payload  of the log message.
	 * @param duration maximum time to wait the log is stored.
	 */
	protected void checkLogStored(LogLevel level, String message, Object payload, Duration duration) {

		final var deadline = System.currentTimeMillis() + duration.toMillis();
		while (System.currentTimeMillis() < deadline) {

			final var content = given().queryParam("order", "-timestamp").queryParam("limit", "50").when()
					.get("/v1/logs").then().statusCode(200).extract().asString();
			final var page = Json.decodeValue(content, JsonObject.class);
			final var logs = page.getJsonArray("logs");
			if (logs != null) {

				final var max = logs.size();
				for (var i = 0; i < max; i++) {

					final var log = logs.getJsonObject(i);
					if (log != null) {

						if (level == null || level.name().equals(log.getString("level"))) {

							if (message == null || message.equals(log.getString("message"))) {

								if (payload == null) {

									return;

								} else {

									final var found = log.getString("payload");
									if (found != null) {

										final var expected = Json.decodeValue(Json.encode(payload), JsonObject.class);
										final var decoded = Json.decodeValue(found, JsonObject.class);
										if (expected.equals(decoded)) {

											return;
										}
									}
								}
							}

						}

					}
				}
			}
		}

		fail("The log is not stored.");

	}

}
