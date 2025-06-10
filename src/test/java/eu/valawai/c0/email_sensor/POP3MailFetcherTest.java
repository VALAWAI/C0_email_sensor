/*
  Copyright 2025 UDT-IA, IIIA-CSIC

  Use of this source code is governed by an MIT-style
  license that can be found in the LICENSE file or at
  https://opensource.org/licenses/MIT.
*/

package eu.valawai.c0.email_sensor;

import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Test the {@link EMailFetcher} to use the POP3 to connect to the server.
 *
 * @author UDT-IA, IIIA-CSIC
 */
@QuarkusTest
@WithTestResource(value = POP3EMailServerTestResource.class, scope = TestResourceScope.RESTRICTED_TO_CLASS)
@TestProfile(POP3TestProfile.class)
public class POP3MailFetcherTest extends EMailFetcherTestCase {

}
