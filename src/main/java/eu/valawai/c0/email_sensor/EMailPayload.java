/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import java.util.List;

/**
 * The payload that contains the information of an e-mail.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class EMailPayload extends Payload {

	/**
	 * The address of the people that has send or receive the e-mail.
	 */
	public List<EMailAddressPayload> addresses;

	/**
	 * The subject of the e-mail.
	 */
	public String subject;

	/**
	 * The MIME type associated to the e-mail content.
	 */
	public String mime_type;

	/**
	 * The content of the e-mail.
	 */
	public String content;

	/**
	 * The epoch time, in seconds, when the email is received.
	 */
	public Long received_at;

}
