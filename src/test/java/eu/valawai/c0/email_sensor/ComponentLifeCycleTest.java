/*
  Copyright 2024 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Test the {@link ComponentLifeCycle}.
 *
 * @see ComponentLifeCycle
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@QuarkusTestResource(MOVTestResource.class)
public class ComponentLifeCycleTest {

	/**
	 * The current version of the component.
	 */
	@ConfigProperty(name = "quarkus.application.version")
	protected String version;

	/**
	 * The service to query for the component info.
	 */
	@Inject
	protected MOVComponentQueryService queryService;

	/**
	 * Check that the component is registered.
	 */
	@Test
	public void shouldComponentBeRegistered() {

		var component = this.queryService.queryComponentInformation();
		for (var i = 0; i < 10 && component == null; i++) {

			try {

				Thread.sleep(500);

			} catch (final InterruptedException ignored) {
			}
			component = this.queryService.queryComponentInformation();
		}
		assertNotNull(component);
		assertEquals(this.version, component.getString("version"));

	}

}
