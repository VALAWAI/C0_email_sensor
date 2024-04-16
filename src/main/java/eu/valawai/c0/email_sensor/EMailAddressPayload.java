/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

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

}
