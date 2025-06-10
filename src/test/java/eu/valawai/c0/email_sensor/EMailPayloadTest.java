/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import eu.valawai.c0.email_sensor.mov.PayloadTestCase;

/**
 * Test the {@link EMailPayload}.
 *
 * @see EMailPayload
 *
 * @author VALAWAI
 */
public class EMailPayloadTest extends PayloadTestCase<EMailPayload> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EMailPayload createEmptyModel() {

		return new EMailPayload();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillIn(EMailPayload payload) {

		final var max = rnd.nextInt(2, 7);
		payload.addresses = new ArrayList<>();

		final var builder = new EMailAddressPayloadTest();
		for (var i = 0; i < max; i++) {

			final var address = builder.nextModel();
			if (i == 0) {

				address.type = EMailAddressType.FROM;

			} else if (i == 1) {

				address.type = EMailAddressType.TO;

			} else {

				final var values = EMailAddressType.values();
				while (address.type == EMailAddressType.FROM) {

					address.type = values[rnd.nextInt(0, values.length)];
				}
			}
			payload.addresses.add(address);
		}
		payload.subject = "Subject " + rnd.nextInt(0, 10000);
		payload.mime_type = "text/plain";
		payload.content = "E-Mail content " + rnd.nextInt(0, 123456789) + " lore ipsum lore";
		payload.received_at = rnd.nextLong(0, 200000000);
	}

	/**
	 * Should not obtain the payload address from {@code null}.
	 */
	@Test
	public void shouldEncodeNullAddressToNullPayload() {

		assertNull(EMailAddressPayload.from(null));
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
		final var payload = EMailAddressPayload.from(address);
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
		final var payload = EMailAddressPayload.from(address);
		assertNotNull(payload);
		assertEquals(name, payload.name);
		assertEquals(email, payload.address);
	}

	/**
	 * Check if two payloads are compatible.
	 *
	 * @param source email to compare.
	 * @param target email to compare.
	 *
	 * @return {@coide true} if the payloads are compatible, {@code false}
	 *         otherwise.
	 */
	public static boolean areCompatible(EMailPayload source, EMailPayload target) {

		if (source.content.equals(target.content) && source.subject.equals(target.subject)) {

			final var copy = new ArrayList<>(source.addresses);
			for (final var targetAddress : target.addresses) {

				var found = false;
				final var i = copy.iterator();
				while (i.hasNext()) {

					final var sourceAddress = i.next();
					if (targetAddress.type == sourceAddress.type
							&& targetAddress.address.equals(sourceAddress.address)) {

						i.remove();
						found = true;
						if (targetAddress.name != null) {

							assertEquals(sourceAddress.name, targetAddress.name);
						}
						break;
					}

				}

				if (!found) {
					System.err.println(targetAddress);
					return false;
				}
			}

			for (final var address : copy) {

				assertEquals(EMailAddressType.BCC, address.type);
			}
			return true;

		} else {

			return false;
		}
	}
}
