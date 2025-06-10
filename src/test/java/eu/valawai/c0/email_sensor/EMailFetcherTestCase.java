/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

/**
 * Test the {@link EMailFetcher}.
 *
 * @see EMailFetcher
 *
 * @author UDT-IA, IIIA-CSIC
 */
public abstract class EMailFetcherTestCase {

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
			payload.content = "E-mail content " + UUID.randomUUID().toString();

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
				if (EMailPayloadTest.areCompatible(payload, email)) {

					if (email.received_at != null) {

						assertTrue(Math.abs(payload.received_at - email.received_at) < 3,
								"Unexpected received time stamp.");
					}
					iter.remove();
				}
			}
		}
		assertEquals(0, emails.size(), "Not received all the expected e-mails");

		final var nomoreUnreadEmails = this.service.fetchUnreadEMails();
		assertNotNull(nomoreUnreadEmails);
		assertTrue(nomoreUnreadEmails.isEmpty());

	}

}
