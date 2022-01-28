package org.acme.rest.json;

import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_JSON;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.infinispan.AdvancedCache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.dataconversion.internal.Json;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.ImmortalCacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.manager.EmbeddedCacheManagerAdmin;
import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.framework.RestRequest;
import org.infinispan.rest.framework.RestResponse;
import org.infinispan.rest.framework.impl.RestResponseBuilder;
import org.infinispan.security.impl.Authorizer;
import org.infinispan.util.concurrent.CompletableFutures;

import io.netty.handler.codec.http.HttpResponseStatus;

public class OurRestCacheManager implements RestCacheManager<WrappedBytes> {
   protected final ConcurrentMap<String, ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>>> caches = new ConcurrentHashMap<>();
   protected final Function<Object, ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>>> function;

   public OurRestCacheManager(Supplier<ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>>> cacheSupplier) {
      this.function = __ -> cacheSupplier.get();
   }

   @Override
   public RestResponseBuilder<?> restResponseBuilder(RestRequest restRequest) {
      if (!(restRequest instanceof FruitResource.OurRequest)) {
         throw new IllegalArgumentException("Only supports OurRequest!");
      }
      return new VertxRestResponse.Builder(((FruitResource.OurRequest) restRequest).getHttpServerRequest().response());
   }

   @Override
   public boolean cacheExists(String name) {
      return caches.containsKey(name);
   }

   @Override
   public Collection<String> getCacheNames() {
      return caches.keySet();
   }

   WrappedBytes toKey(Object key) {
      return new WrappedByteArray(key.toString().getBytes(StandardCharsets.UTF_8));
   }

   ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> getMap(String cacheName) {
      return caches.computeIfAbsent(cacheName, function);
   }

   @Override
   public CompletionStage<CacheEntry<Object, WrappedBytes>> getInternalEntry(String cacheName, Object key, MediaType keyContentType, MediaType mediaType, RestRequest request) {
      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);
      CacheEntry<WrappedBytes, WrappedBytes> ice = map.get(toKey(key));
      return CompletableFuture.completedFuture((CacheEntry) ice);
   }

   @Override
   public CompletionStage<WrappedBytes> remove(String cacheName, Object key, MediaType keyContentType, RestRequest restRequest) {
      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);
      InternalCacheEntry<WrappedBytes, WrappedBytes> prev = map.remove(toKey(key));
      return prev == null ? CompletableFutures.completedNull() : CompletableFuture.completedFuture(prev.getValue());
   }

   @Override
   public CompletionStage<CacheEntry<Object, WrappedBytes>> getPrivilegedInternalEntry(String cacheName,
         MediaType keyContentType, MediaType valueContentType, Object key, boolean skipListener) {
      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);
      InternalCacheEntry<WrappedBytes, WrappedBytes> value = map.get(toKey(key));
      return value == null ? CompletableFutures.completedNull() : CompletableFuture.completedFuture((CacheEntry) value);
   }

   @Override
   public CompletionStage<?> putInCache(String cacheName, MediaType keyContentType, MediaType valueContentType, Object key, byte[] value, Long ttl, Long idleTime, RestRequest request) {
      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);
      WrappedBytes keyBytes = toKey(key);
      InternalCacheEntry<WrappedBytes, WrappedBytes> ice = new ImmortalCacheEntry(keyBytes, new WrappedByteArray(value));
      map.put(toKey(key), ice);
      return CompletableFutures.completedNull();
   }

   @Override
   public MediaType getValueConfiguredFormat(String cacheName, RestRequest restRequest) {
      return MediaType.APPLICATION_OCTET_STREAM;
   }

   @Override
   public String getNodeName() {
      return "ISPN-REST-LOCAL";
   }

   @Override
   public MediaType negotiateValueMediaType(InvocationHelper invocationHelper, RestRequest restRequest) {
      return MediaType.APPLICATION_OCTET_STREAM;
   }

   @Override
   public void stop() {

   }

   @Override
   public CompletionStage<RestResponse> getSize(RestRequest request) {
      String cacheName = request.variables().get("cacheName");
      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);

      return CompletableFuture.completedFuture(restResponseBuilder(request)
            .contentType(APPLICATION_JSON)
            .entity(Json.make(map.size()))
            .build());
   }

   @Override
   public CompletionStage<RestResponse> clearEntireCache(RestRequest request) {
      String cacheName = request.variables().get("cacheName");

      RestResponseBuilder<?> responseBuilder = restResponseBuilder(request);
      responseBuilder.status(HttpResponseStatus.NO_CONTENT.code());

      ConcurrentMap<WrappedBytes, InternalCacheEntry<WrappedBytes, WrappedBytes>> map = getMap(cacheName);
      if (map != null) {
         map.clear();
      }

      return CompletableFuture.completedFuture(responseBuilder.build());
   }

   // TODO: implement next
   @Override
   public CompletionStage<RestResponse> streamKeys(int batch, int limit, RestRequest restRequest) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<RestResponse> streamEntries(int batch, boolean metadata, int limit, boolean negotiate, RestRequest request, InvocationHelper invocationHelper) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<RestResponse> cacheListen(MediaType accept, boolean includeCurrentState, RestRequest request) {
      throw new UnsupportedOperationException();
   }

   // Fully Unsupported Operations
   @Override
   public CompletionStage<RestResponse> getCacheStats(RestRequest request) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<RestResponse> getAllDetails(RestRequest request, InvocationHelper invocationHelper) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<RestResponse> getCacheConfig(RestRequest request, InvocationHelper invocationHelper) {
      throw new UnsupportedOperationException();
   }

   @Override
   public AdvancedCache<Object, WrappedBytes> getCache(String name, RestRequest restRequest) {
      throw new UnsupportedOperationException();
   }

   @Override
   public AdvancedCache<Object, WrappedBytes> getCache(String name, MediaType keyContentType, MediaType valueContentType, RestRequest request) {
      throw new UnsupportedOperationException();
   }

   @Override
   public EmbeddedCacheManager getInstance() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void resetCacheInfo(String cacheName) {
      throw new UnsupportedOperationException();
   }

   @Override
   public EmbeddedCacheManagerAdmin getCacheManagerAdmin(RestRequest restRequest) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Authorizer getAuthorizer() {
      // We should really use Quarkus built in authorization
      throw new UnsupportedOperationException();
   }
}
