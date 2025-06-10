/*
  Copyright 2025 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * The configuration to use the POP3 test resource.
 *
 * @see POP3EMailServerTestResource
 *
 * @author UDT-IA, IIIA-CSIC
 */
public class POP3TestProfile implements QuarkusTestProfile {

	/**
	 * Provide the properties to use the POP3 test resource.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getConfigOverrides() {

		return POP3EMailServerTestResource.getConfiguration();
	}
}
