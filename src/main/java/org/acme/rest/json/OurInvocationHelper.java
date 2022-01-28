package org.acme.rest.json;

import java.util.concurrent.Executor;

import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.counter.impl.manager.EmbeddedCounterManager;
import org.infinispan.marshall.core.EncoderRegistry;
import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.RestServer;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.configuration.RestServerConfiguration;
import org.infinispan.server.core.ServerManagement;

public class OurInvocationHelper extends InvocationHelper {
   public OurInvocationHelper(RestServer protocolServer, RestCacheManager<Object> restCacheManager,
         EmbeddedCounterManager counterManager, RestServerConfiguration configuration, ServerManagement server,
         Executor executor, EncoderRegistry encoderRegistry, ParserRegistry parserRegistry) {
      super(protocolServer, restCacheManager, counterManager, configuration, server, executor, encoderRegistry,
            parserRegistry);
   }

   @Override
   protected void checkServerStatus() {
      // Do nothing
   }
}
