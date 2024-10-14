/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
*/

package eu.valawai.c0.email_sensor;

import eu.valawai.c0.email_sensor.mov.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * The parameters that configure the C0 EMail sensor component.
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class EMailSensorComponentParametersPayload extends Payload {

	/**
	 * The seconds between fetching e-mails intervals.
	 */
	@NotNull
	@Min(1)
	public Integer fetching_interval;
}
