package org.acme.rest.json;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.manager.EmbeddedCacheManagerAdmin;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.framework.RestRequest;
import org.infinispan.security.impl.Authorizer;

public class OurRestCacheManager implements RestCacheManager {
   protected final ConcurrentMap<String, AdvancedCache<WrappedBytes, WrappedBytes>> caches = new ConcurrentHashMap<>();
   protected final Supplier<AdvancedCache<WrappedBytes, WrappedBytes>> cacheSupport;

   public OurRestCacheManager(Supplier<AdvancedCache<WrappedBytes, WrappedBytes>> cacheSupport) {
      this.cacheSupport = cacheSupport;
   }

   @Override
   public AdvancedCache getCache(String name, MediaType keyContentType, MediaType valueContentType, RestRequest request) {
      return getCache(name, request);
   }

   @Override
   public AdvancedCache getCache(String name, RestRequest restRequest) {
      return caches.computeIfAbsent(name, __ -> cacheSupport.get());
   }

   @Override
   public boolean cacheExists(String name) {
      return caches.containsKey(name);
   }

   @Override
   public Collection<String> getCacheNames() {
      return caches.keySet();
   }

   @Override
   public CompletionStage<CacheEntry> getInternalEntry(String cacheName, Object key, MediaType keyContentType, MediaType mediaType, RestRequest request) {
      AdvancedCache<WrappedBytes, WrappedBytes> map = caches.get(cacheName);
      return CompletableFuture.completedFuture();
   }

   @Override
   public CompletionStage remove(String cacheName, Object key, MediaType keyContentType, RestRequest restRequest) {
      return null;
   }

   @Override
   public MediaType getValueConfiguredFormat(String cacheName, RestRequest restRequest) {
      return null;
   }

   @Override
   public String getNodeName() {
      return null;
   }

   @Override
   public String getServerAddress() {
      return null;
   }

   @Override
   public String getPrimaryOwner(String cacheName, Object key, RestRequest restRequest) {
      return null;
   }

   @Override
   public String getBackupOwners(String cacheName, Object key, RestRequest restRequest) {
      return null;
   }

   @Override
   public EmbeddedCacheManager getInstance() {
      return null;
   }

   @Override
   public Authorizer getAuthorizer() {
      return null;
   }

   @Override
   public EmbeddedCacheManagerAdmin getCacheManagerAdmin(RestRequest restRequest) {
      return null;
   }

   @Override
   public CompletionStage<CacheEntry> getPrivilegedInternalEntry(AdvancedCache cache, Object key, boolean skipListener) {
      return null;
   }

   @Override
   public boolean isCacheQueryable(Cache cache) {
      return false;
   }
}
