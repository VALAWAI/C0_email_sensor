/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The service to send e-mails.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@ApplicationScoped
public class EMailSender {

	/**
	 * The port for the smtp.
	 */
	@ConfigProperty(name = "mail.host", defaultValue = "host.docker.internal")
	String host;

	/**
	 * The port for the smtp.
	 */
	@ConfigProperty(name = "mail.smtp.port", defaultValue = "25")
	String port;

	/**
	 * The user name to access to e-mail server.
	 */
	@ConfigProperty(name = "mail.username", defaultValue = "user")
	String username;

	/**
	 * The password to access to e-mail server.
	 */
	@ConfigProperty(name = "mail.password", defaultValue = "password")
	String password;

	/**
	 * Send an e-mail.
	 *
	 * @param payload email to send.
	 *
	 * @return {@code true} if has been send.
	 */
	public boolean send(EMailPayload payload) {

		try {

			final var props = new java.util.Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", this.host);
			props.put("mail.smtp.port", this.port);
			props.put("mail.smtp.ssl.trust", "*");
			final var session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(EMailSender.this.username, EMailSender.this.password);
				}
			});
			final var msg = new MimeMessage(session);
			for (final var address : payload.addresses) {

				final var who = address.encode();
				if (address.type == EMailAddressType.FROM) {

					msg.setFrom(who);

				} else {

					final var type = switch (address.type) {
					case TO -> Message.RecipientType.TO;
					case CC -> Message.RecipientType.CC;
					default -> Message.RecipientType.BCC;
					};
					msg.addRecipients(type, who);
				}
			}
			msg.setSubject(payload.subject);
			msg.setText(payload.content, "UTF-8");
			Transport.send(msg);
			return true;

		} catch (final Throwable error) {

			Log.errorv(error, "Cannot send 0}", payload);
			return false;
		}
	}

}
