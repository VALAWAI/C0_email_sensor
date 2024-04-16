/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The component to fetch mails.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ApplicationScoped
public class MailFetcher {

	/**
	 * The protocol to connect to the mail server. It can be pop3 or imap
	 */
	@ConfigProperty(name = "mail.protocol", defaultValue = "pop3")
	String protocol;

	/**
	 * The host of the mail server.
	 */
	@ConfigProperty(name = "mail.host", defaultValue = "localhost")
	String host;

	/**
	 * The port to the mail server.
	 */
	@ConfigProperty(name = "mail.port", defaultValue = "995")
	String port;

	/**
	 * This is {@code true} if the connection is secure.
	 */
	@ConfigProperty(name = "mail.secured", defaultValue = "true")
	String secured;

	/**
	 * The name for the user that can log in into the mail server.
	 */
	@ConfigProperty(name = "mail.username", defaultValue = "mov")
	String user;

	/**
	 * The password for the user that can log in into the mail server.
	 */
	@ConfigProperty(name = "mail.userpassword", defaultValue = "password")
	String password;

	/**
	 * Fetch for the e-mail that has not been read.
	 *
	 * @return the email that has not been read.
	 */
	public List<EMailPayload> fetchUnreadEMails() {

		final var emails = new ArrayList<EMailPayload>();
		try {

			final var properties = new java.util.Properties();
			properties.put("mail.store.protocol", this.protocol);
			if ("pop3".equals(this.protocol)) {

				properties.put("mail.pop3.host", this.host);
				properties.put("mail.pop3.port", this.port);
				properties.put("mail.pop3.starttls.enable", this.secured);

			} else {

				properties.put("mail.imap.host", this.host);
				properties.put("mail.imap.port", this.port);
				properties.put("mail.imap.ssl.enable", this.secured);

			}
			final Session emailSession = Session.getDefaultInstance(properties);
			var storeProtocol = this.protocol;
			if (Boolean.parseBoolean(this.secured)) {

				storeProtocol += "s";
			}
			final Store store = emailSession.getStore(storeProtocol);

			store.connect(this.host, this.user, this.password);
			final Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);
			final Flags seen = new Flags(Flags.Flag.RECENT);
			final FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			final var messages = inbox.search(unseenFlagTerm);
			for (final var message : messages) {

				final var email = new EMailPayload();
				email.addresses = new ArrayList<>();
				this.fillInWithType(email, EMailAddressType.FROM, message.getFrom());
				this.fillInWithType(email, EMailAddressType.TO, message.getRecipients(RecipientType.TO));
				this.fillInWithType(email, EMailAddressType.CC, message.getRecipients(RecipientType.CC));
				this.fillInWithType(email, EMailAddressType.BCC, message.getRecipients(RecipientType.BCC));
				email.subject = message.getSubject();
				email.mime_type = message.getContentType();
				email.content = String.valueOf(message.getContent());
				final var date = message.getReceivedDate();
				if (date != null) {

					email.received_at = date.toInstant().getEpochSecond();
				}
				emails.add(email);

			}
			inbox.close(false);
			store.close();

		} catch (final Throwable error) {

			Log.errorv(error, "Cannot fetch the emails.");

		}
		return emails;

	}

	/**
	 * Add to the e-mail payload the addresses of a type.
	 *
	 * @param payload   to add the addresses.
	 * @param type      of address to fill in.
	 * @param addresses to convert.
	 */
	protected void fillInWithType(EMailPayload payload, EMailAddressType type, Address[] addresses) {

		if (addresses != null) {

			for (final var address : addresses) {

				final var addressPayload = this.toEMailAddressPayload(address);
				if (addressPayload != null) {

					addressPayload.type = type;
					payload.addresses.add(addressPayload);
				}
			}
		}
	}

	/**
	 * Convert an address to a payload.
	 *
	 * @param address to convert.
	 *
	 * @return the payload associated to the address.
	 */
	protected EMailAddressPayload toEMailAddressPayload(Address address) {

		if (address == null) {

			return null;

		} else {

			final var payload = new EMailAddressPayload();
			if (address instanceof InternetAddress internet) {

				payload.name = internet.getPersonal();
				payload.address = internet.getAddress();

			} else {

				final var value = address.toString().trim();
				final var begin = value.indexOf('<');
				if (begin > -1) {

					final var last = value.indexOf('>', begin);
					payload.name = value.substring(0, begin).trim();
					if (value.length() > last) {

						payload.name += " " + value.substring(last + 1).trim();
						payload.name = payload.name.trim();
					}
					payload.address = value.substring(begin + 1, last).trim();

				} else {

					payload.address = value;
				}
			}
			return payload;
		}
	}

}
