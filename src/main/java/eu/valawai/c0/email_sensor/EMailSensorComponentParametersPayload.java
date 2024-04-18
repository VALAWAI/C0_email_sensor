/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
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
