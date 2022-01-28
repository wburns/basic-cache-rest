package org.acme.rest.json;

import org.infinispan.commons.time.TimeService;
import org.infinispan.container.impl.InternalEntryFactory;
import org.infinispan.container.offheap.OffHeapEntryFactoryImpl;
import org.infinispan.container.offheap.OffHeapMemoryAllocator;

public class OurOffHeapEntryFactory extends OffHeapEntryFactoryImpl {
   public OurOffHeapEntryFactory(OffHeapMemoryAllocator allocator, TimeService timeService,
         InternalEntryFactory internalEntryFactory) {
      this.allocator = allocator;
      this.timeService = timeService;
      this.internalEntryFactory = internalEntryFactory;
   }
}
