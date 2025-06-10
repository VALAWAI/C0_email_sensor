/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

import eu.valawai.c0.email_sensor.mov.Payload;

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

	/**
	 * Return the email payload from a Message.
	 *
	 * @param message to get the data.
	 *
	 * @return the payload with the data.
	 *
	 * @throws MessagingException If can not get the data from the message.
	 * @throws IOException        If it can not read the message content.
	 */
	public static EMailPayload from(Message message) throws IOException, MessagingException {

		final var email = new EMailPayload();
		email.addresses = new ArrayList<>();
		fillInWithType(email, EMailAddressType.FROM, message.getFrom());
		fillInWithType(email, EMailAddressType.TO, message.getRecipients(RecipientType.TO));
		fillInWithType(email, EMailAddressType.CC, message.getRecipients(RecipientType.CC));
		fillInWithType(email, EMailAddressType.BCC, message.getRecipients(RecipientType.BCC));
		email.subject = message.getSubject();
		email.mime_type = message.getContentType();
		email.content = String.valueOf(message.getContent()).trim();
		final var date = message.getReceivedDate();
		if (date != null) {

			email.received_at = date.toInstant().getEpochSecond();
		}
		return email;
	}

	/**
	 * Add to the e-mail payload the addresses of a type.
	 *
	 * @param payload   to add the addresses.
	 * @param type      of address to fill in.
	 * @param addresses to convert.
	 */
	private static void fillInWithType(EMailPayload payload, EMailAddressType type, Address[] addresses) {

		if (addresses != null) {

			for (final var address : addresses) {

				final var addressPayload = EMailAddressPayload.from(address);
				if (addressPayload != null) {

					addressPayload.type = type;
					payload.addresses.add(addressPayload);
				}
			}
		}
	}

}
