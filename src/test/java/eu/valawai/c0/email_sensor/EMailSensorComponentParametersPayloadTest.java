/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import eu.valawai.c0.email_sensor.mov.PayloadTestCase;

/**
 * Test the {@link EMailSensorComponentParametersPayload}.
 *
 * @see EMailSensorComponentParametersPayload
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class EMailSensorComponentParametersPayloadTest extends PayloadTestCase<EMailSensorComponentParametersPayload> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EMailSensorComponentParametersPayload createEmptyModel() {

		return new EMailSensorComponentParametersPayload();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillIn(EMailSensorComponentParametersPayload model) {

		model.fetching_interval = rnd.nextInt(1, 3600);
	}

}
