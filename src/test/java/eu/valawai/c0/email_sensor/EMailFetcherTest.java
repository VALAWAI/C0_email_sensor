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

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.Json;
import jakarta.inject.Inject;

/**
 * Test the {@link EMailFetcher}.
 *
 * @see EMailFetcher
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@QuarkusTestResource(EMailServerTestResource.class)
public class EMailFetcherTest {

	/**
	 * The service to test.
	 */
	@Inject
	EMailFetcher service;

	/**
	 * The service to send e-mails.
	 */
	@Inject
	EMailSender sender;

	/**
	 * Should not obtain the payload address from {@code null}.
	 */
	@Test
	public void shouldEncodeNullAddressToNullPayload() {

		assertNull(new EMailFetcher().toEMailAddressPayload(null));
	}

	/**
	 * Should obtain payload from an address.
	 *
	 * @param value of the address.
	 * @param name  expected payload name.
	 * @param email expected payload address.
	 */
	@ParameterizedTest(name = "Should obtain the payload from {0}")
	@CsvSource(textBlock = """
			bob smith <bob@example.com>, bob smith, bob@example.com
			bob smith <bob@example.com> (Bobby), bob smith (Bobby), bob@example.com
			<bob@example.com> (Bobby), (Bobby), bob@example.com
			bob@example.com, , bob@example.com
			""")
	public void shouldEncodeAddressToPayload(String value, String name, String email) {

		final var address = new Address() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getType() {

				return null;
			}

			@Override
			public String toString() {

				return value;
			}

			@Override
			public boolean equals(Object address) {

				return false;
			}

		};
		final var payload = new EMailFetcher().toEMailAddressPayload(address);
		assertNotNull(payload);
		assertEquals(name, payload.name);
		assertEquals(email, payload.address);
	}

	/**
	 * Should obtain payload from an Internet address.
	 *
	 * @param name  expected payload name.
	 * @param email expected payload address.
	 *
	 * @throws UnsupportedEncodingException If the address is not valid.
	 */
	@ParameterizedTest(name = "Should obtain the payload from {0}")
	@CsvSource(textBlock = """
			bob smith, bob@example.com
			, bob@example.com
			""")
	public void shouldEncodeAddressToPayload(String name, String email) throws UnsupportedEncodingException {

		final var address = new InternetAddress(email, name);
		final var payload = new EMailFetcher().toEMailAddressPayload(address);
		assertNotNull(payload);
		assertEquals(name, payload.name);
		assertEquals(email, payload.address);
	}

	/**
	 * Should not obtain unread e-mails.
	 */
	@Test
	public void shouldNotFoundUnreadEMails() {

		final var emails = this.service.fetchUnreadEMails();
		assertNotNull(emails);
		assertTrue(emails.isEmpty());

	}

	/**
	 * Should obtain some unreal e-mails.
	 */
	@Test
	public void shouldFoundUnreadEMails() {

		final var rnd = new Random(0);
		final var expected = new ArrayList<EMailPayload>();
		for (var i = 0; i < 10; i++) {

			final var payload = new EMailPayload();
			payload.subject = "Subject of the e-mail " + rnd.nextInt(0, 1000);
			payload.mime_type = "text/plain";
			payload.content = "E-mail content " + rnd.nextInt(0, 100000);

			payload.addresses = new ArrayList<>();
			final var from = new EMailAddressPayload();
			from.type = EMailAddressType.FROM;
			from.name = "Sender user " + i;
			from.address = "sender_" + i + "@valawai.eu";
			payload.addresses.add(from);

			for (var j = 0; j < 3; j++) {

				final var to = new EMailAddressPayload();
				to.type = EMailAddressType.TO;
				to.name = "Test user " + j;
				to.address = "user_" + j + "@valawai.eu";
				payload.addresses.add(to);

				final var cc = new EMailAddressPayload();
				cc.type = EMailAddressType.CC;
				cc.name = "Test cc user " + j;
				cc.address = "user_cc_" + j + "@valawai.eu";
				payload.addresses.add(cc);

				final var bcc = new EMailAddressPayload();
				bcc.type = EMailAddressType.BCC;
				bcc.name = "Test bcc user " + j;
				bcc.address = "user_bcc_" + j + "@valawai.eu";
				payload.addresses.add(bcc);

			}

			assertTrue(this.sender.send(payload));
			payload.received_at = Instant.now().getEpochSecond();
			expected.add(payload);
		}

		final var emails = this.service.fetchUnreadEMails();
		assertNotNull(emails);
		assertTrue(expected.size() <= emails.size(), "Not received all the expected e-mails");
		for (final var payload : expected) {

			final var iter = emails.iterator();
			while (iter.hasNext()) {

				final var email = iter.next();
				if (email.content.equals(payload.content) && email.subject.equals(payload.subject)) {

					final var copy = new ArrayList<>(email.addresses);
					copy.removeAll(payload.addresses);
					if (copy.isEmpty()) {

						assertTrue(Math.abs(payload.received_at - email.received_at) < 3,
								"Unexpected received time stamp.");
						iter.remove();

					} else {

						System.err.println(Json.encodePrettily(copy));

					}
				}

			}
		}
		assertTrue(emails.isEmpty(), "Unexpected received e-mails");

		final var nomoreUnreadEmails = this.service.fetchUnreadEMails();
		assertNotNull(nomoreUnreadEmails);
		assertTrue(nomoreUnreadEmails.isEmpty());

	}

}
