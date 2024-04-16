/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

/**
 * Test the {@link EMailAddressPayload}.
 *
 * @see EMailAddressPayload
 *
 * @author VALAWAI
 */
public class EMailAddressPayloadTest extends PayloadTestCase<EMailAddressPayload> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EMailAddressPayload createEmptyModel() {

		return new EMailAddressPayload();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillIn(EMailAddressPayload payload) {

		final var values = EMailAddressType.values();
		payload.type = values[rnd.nextInt(0, values.length)];
		payload.name = "User name " + rnd.nextInt();
		payload.address = "user_" + rnd.nextInt() + "@valawai.eu";
	}

}
