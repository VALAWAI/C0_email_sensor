/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import java.util.ArrayList;

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

}
