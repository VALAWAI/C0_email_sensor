/*
  Copyright 2022-2026 VALAWAI

  Use of this source code is governed by GNU General Public License version 3
  license that can be found in the LICENSE file or at
  https://opensource.org/license/gpl-3-0/
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
