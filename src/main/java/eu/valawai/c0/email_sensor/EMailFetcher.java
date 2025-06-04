/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The component to fetch mails.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ApplicationScoped
public class EMailFetcher {

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
	boolean secured;

	/**
	 * The name for the user that can log in into the mail server.
	 */
	@ConfigProperty(name = "mail.user.name", defaultValue = "user")
	String userName;

	/**
	 * The password for the user that can log in into the mail server.
	 */
	@ConfigProperty(name = "mail.user.password", defaultValue = "password")
	String userPassword;

	/**
	 * Fetch for the e-mail that has not been read.
	 *
	 * @return the fetched unread e-mail or {@code null} if cannot fetch the
	 *         e-mials.
	 */
	public List<EMailPayload> fetchUnreadEMails() {

		try {

			final var properties = new java.util.Properties();
			properties.put("mail.store.protocol", this.protocol);
			if (this.secured) {

				if (!this.protocol.endsWith("s")) {

					this.protocol += "s";
				}
			}
			properties.put("mail." + this.protocol + ".host", this.host);
			properties.put("mail." + this.protocol + ".port", this.port);
			properties.put("mail." + this.protocol + ".starttls.enable", this.secured);
			properties.put("mail." + this.protocol + ".ssl.trust", "*");
			final Session emailSession = Session.getDefaultInstance(properties);
			final Store store = emailSession.getStore(this.protocol);

			store.connect(this.host, this.userName, this.userPassword);
			final Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);
			final var newCount = inbox.getNewMessageCount();
			final var emails = new ArrayList<EMailPayload>();
			if (newCount > 0) {

				final var messages = inbox.getMessages();
				for (final var message : messages) {

					final var flags = message.getFlags();
					if (!flags.contains(Flag.SEEN) && !flags.contains(Flag.DRAFT) && !flags.contains(Flag.DELETED)
							&& flags.contains(Flag.RECENT)) {

						message.setFlag(Flag.SEEN, true);
						final var email = new EMailPayload();
						email.addresses = new ArrayList<>();
						this.fillInWithType(email, EMailAddressType.FROM, message.getFrom());
						this.fillInWithType(email, EMailAddressType.TO, message.getRecipients(RecipientType.TO));
						this.fillInWithType(email, EMailAddressType.CC, message.getRecipients(RecipientType.CC));
						this.fillInWithType(email, EMailAddressType.BCC, message.getRecipients(RecipientType.BCC));
						email.subject = message.getSubject();
						email.mime_type = message.getContentType();
						email.content = String.valueOf(message.getContent()).trim();
						final var date = message.getReceivedDate();
						if (date != null) {

							email.received_at = date.toInstant().getEpochSecond();
						}
						emails.add(email);

					}

				}
			}
			inbox.close(false);
			store.close();
			return emails;

		} catch (final Throwable error) {

			Log.errorv(error, "Cannot fetch the e-mails.");
			return null;
		}

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
