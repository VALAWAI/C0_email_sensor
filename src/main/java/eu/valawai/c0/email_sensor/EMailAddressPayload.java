/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import eu.valawai.c0.email_sensor.mov.Payload;

/**
 * Describe the address associated to an e-mail.
 *
 * @see EMailPayload
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class EMailAddressPayload extends Payload {

	/**
	 * The type of address.
	 */
	public EMailAddressType type;

	/**
	 * The name of the user.
	 */
	public String name;

	/**
	 * The e-mail address.
	 */
	public String address;

	/**
	 * Encode this address.
	 *
	 * @return the string representation of the address.
	 *
	 * @see #decode
	 */
	public String encode() {

		if (this.name == null) {

			return this.address;

		} else {

			return this.name + " <" + this.address + ">";
		}
	}

	/**
	 * Decode an address.
	 *
	 * @param who encoded address to decode.
	 *
	 * @return the address defined on the string.
	 *
	 * @see #encode
	 */
	public static EMailAddressPayload decode(String who) {

		if (who == null) {

			return null;

		} else {

			final var payload = new EMailAddressPayload();
			final var value = who.trim();
			final var begin = value.indexOf('<');
			if (begin > -1) {

				final var last = value.indexOf('>', begin);
				payload.name = value.substring(0, begin).trim();
				if (value.length() > last + 1) {

					payload.name += " " + value.substring(last + 1).trim();
				}
				payload.name = payload.name.trim();
				if (payload.name.length() == 0) {

					payload.name = null;
				}
				payload.address = value.substring(begin + 1, last).trim();

			} else {

				payload.address = value;
			}
			return payload;

		}
	}

	/**
	 * Convert an address to a payload.
	 *
	 * @param address to convert.
	 *
	 * @return the payload associated to the address.
	 */
	public static EMailAddressPayload from(Address address) {

		if (address == null) {

			return null;

		} else if (address instanceof final InternetAddress internet) {

			final var payload = new EMailAddressPayload();
			payload.name = internet.getPersonal();
			payload.address = internet.getAddress();
			return payload;

		} else {

			final var value = address.toString().trim();
			return EMailAddressPayload.decode(value);
		}
	}

}
