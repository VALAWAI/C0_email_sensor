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

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Test the {@link MailFetcher}.
 *
 * @see MailFetcher
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@QuarkusTestResource(MailTestResource.class)
public class MailFetcherTest {

	/**
	 * The component to send e-mails.
	 */
	@Inject
	ReactiveMailer mailer;

	/**
	 * The service to test.
	 */
	@Inject
	MailFetcher service;

	/**
	 * Should not obtain the payload address from {@code null}.
	 */
	@Test
	public void shouldEncodeNullAddressToNullPayload() {

		assertNull(new MailFetcher().toEMailAddressPayload(null));
	}

	/**
	 * Should obtain payload from an address.
	 *
	 * @param value of the address.
	 * @param name  expected payload name.
	 * @param email expected payload address.
	 */
	@ParameterizedTest(name = "Should obtain the payload from {0}")
	@CsvSource("""
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
		final var payload = new MailFetcher().toEMailAddressPayload(address);
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
	@CsvSource("""
			bob smith, bob@example.com
			, bob@example.com
			""")
	public void shouldEncodeAddressToPayload(String name, String email) throws UnsupportedEncodingException {

		final var address = new InternetAddress(email, name);
		final var payload = new MailFetcher().toEMailAddressPayload(address);
		assertNotNull(payload);
		assertEquals(name, payload.name);
		assertEquals(email, payload.address);
	}

	/**
	 * Should not obtain mails if is empty.
	 */
	@Test
	public void shouldNotFoundUnreadEMails() {

		final var emails = this.service.fetchUnreadEMails();
		assertNotNull(emails);
		assertTrue(emails.isEmpty());

	}

}
