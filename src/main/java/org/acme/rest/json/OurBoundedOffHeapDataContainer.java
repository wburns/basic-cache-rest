package org.acme.rest.json;

import org.infinispan.commons.time.TimeService;
import org.infinispan.container.offheap.BoundedOffHeapDataContainer;
import org.infinispan.container.offheap.UnpooledOffHeapMemoryAllocator;
import org.infinispan.eviction.EvictionType;
import org.infinispan.eviction.impl.PassivationManager;
import org.infinispan.eviction.impl.PassivationManagerStub;
import org.infinispan.factories.impl.ComponentRef;

public class OurBoundedOffHeapDataContainer extends BoundedOffHeapDataContainer {
   public OurBoundedOffHeapDataContainer(TimeService timeService, long maxSize, EvictionType type) {
      super(maxSize, type);

      allocator = new UnpooledOffHeapMemoryAllocator();
      offHeapEntryFactory = new OurOffHeapEntryFactory(allocator, timeService, new OurInternalEntryFactory(timeService));

      evictionManager = null;
      passivator = new ComponentRef<>() {
         @Override
         public PassivationManager running() {
            return new PassivationManagerStub();
         }

         @Override
         public PassivationManager wired() {
            return new PassivationManagerStub();
         }

         @Override
         public boolean isRunning() {
            return true;
         }

         @Override
         public boolean isWired() {
            return true;
         }

         @Override
         public boolean isAlias() {
            return false;
         }

         @Override
         public String getName() {
            return null;
         }
      };
      orderer = null;
   }
}
