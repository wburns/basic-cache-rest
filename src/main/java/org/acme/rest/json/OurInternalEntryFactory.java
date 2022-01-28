package org.acme.rest.json;

import org.infinispan.commons.time.TimeService;
import org.infinispan.container.impl.InternalEntryFactoryImpl;

public class OurInternalEntryFactory extends InternalEntryFactoryImpl {
   public OurInternalEntryFactory(TimeService timeService) {
      this.timeService = timeService;
   }
}
