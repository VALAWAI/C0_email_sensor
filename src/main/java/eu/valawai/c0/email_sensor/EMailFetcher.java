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

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

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
	 * The pattern to the folders that can contains the e-mails to be read.
	 */
	@ConfigProperty(name = "mail.inbox.pattern", defaultValue = "INBOX")
	String inboxPattern;

	/**
	 * Fetch for the e-mail that has not been read.
	 *
	 * @return the fetched unread e-mail or {@code null} if cannot fetch the
	 *         e-mials.
	 */
	public List<EMailPayload> fetchUnreadEMails() {

		try {

			Log.debugv("Start fetching unread e-mails.");
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
			final Folder root = store.getDefaultFolder();
			final var folders = root.list(this.inboxPattern);
			final var emails = new ArrayList<EMailPayload>();
			if (this.protocol.toLowerCase().indexOf("pop3") > -1) {

				this.fetchPOP3EMails(emails, folders);

			} else {

				this.fetchIMAPUnseenEMails(emails, folders);
			}

			store.close();

			return emails;

		} catch (final Throwable error) {

			Log.errorv(error, "Cannot fetch the e-mails.");
			return null;
		}

	}

	/**
	 * Fetch all the the e-mail from a POP3 connection.
	 *
	 * @param emails  to add the fetched e-mails.
	 * @param folders to fetch the e-mails.
	 *
	 * @throws MessagingException If can not get the data from the message.
	 * @throws IOException        If it can not read the message content.
	 */
	protected void fetchPOP3EMails(List<EMailPayload> emails, Folder[] folders) throws IOException, MessagingException {

		for (final var inbox : folders) {

			inbox.open(Folder.READ_WRITE);
			final var messages = inbox.getMessages();
			for (final var message : messages) {

				message.setFlag(Flag.DELETED, true);
				final var email = EMailPayload.from(message);
				emails.add(email);

			}
			inbox.close(true);
		}
	}

	/**
	 * Fetch for the e-mail that has not been read.
	 *
	 * @param emails  to add the fetched e-mails.
	 * @param folders to fetch the e-mails.
	 *
	 * @throws MessagingException If can not get the data from the message.
	 * @throws IOException        If it can not read the message content.
	 */
	protected void fetchIMAPUnseenEMails(List<EMailPayload> emails, Folder[] folders)
			throws IOException, MessagingException {

		for (final var inbox : folders) {

			inbox.open(Folder.READ_WRITE);
			final var messages = inbox.getMessages();
			for (final var message : messages) {

				final var flags = message.getFlags();
				if (!flags.contains(Flag.SEEN) && !flags.contains(Flag.DELETED) && !flags.contains(Flag.DRAFT)) {

					message.setFlag(Flag.SEEN, true);
					final var email = EMailPayload.from(message);
					emails.add(email);
				}
			}
			inbox.close(false);
		}
	}

}
