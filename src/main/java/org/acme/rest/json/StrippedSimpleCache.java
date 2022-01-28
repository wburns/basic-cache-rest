package org.acme.rest.json;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.security.auth.Subject;

import org.infinispan.AdvancedCache;
import org.infinispan.LockedStream;
import org.infinispan.batch.BatchContainer;
import org.infinispan.cache.impl.AbstractSimpleCacheImpl;
import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.commons.time.TimeService;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.impl.InternalDataContainer;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.metadata.Metadata;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;
import org.infinispan.partitionhandling.AvailabilityMode;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.util.concurrent.AggregateCompletionStage;

public class StrippedSimpleCache extends AbstractSimpleCacheImpl<WrappedBytes, WrappedBytes> {

   @Override
   public Configuration getCacheConfiguration() {
      return null;
   }

   @Override
   public EmbeddedCacheManager getCacheManager() {
      return null;
   }

   @Override
   public AdvancedCache<WrappedBytes, WrappedBytes> getAdvancedCache() {
      return this;
   }

   @Override
   public ComponentStatus getStatus() {
      return ComponentStatus.RUNNING;
   }

   @Override
   public void clear() {
      dataContainer.clear();
   }

   @Override
   public void start() {

   }

   @Override
   public void stop() {

   }

   @Override
   public AdvancedCache<WrappedBytes, WrappedBytes> withSubject(Subject subject) {
      return null;
   }

   @Override
   public ComponentRegistry getComponentRegistry() {
      return null;
   }

   @Override
   public AvailabilityMode getAvailability() {
      return AvailabilityMode.AVAILABLE;
   }

   @Override
   public void setAvailability(AvailabilityMode availabilityMode) {

   }

   @Override
   protected void wireDependency(Object object) {

   }

   @Override
   public String getName() {
      return null;
   }

   @Override
   public String getVersion() {
      return null;
   }

   @Override
   protected void notifyCacheEntryCreated(WrappedBytes key, WrappedBytes value, Metadata metadata, boolean pre) { }

   @Override
   protected void notifyCacheEntryVisited(WrappedBytes key, WrappedBytes value, boolean pre) { }

   @Override
   protected void notifyCacheEntryModified(WrappedBytes key, WrappedBytes value, Metadata metadata,
         WrappedBytes previousValue, Metadata previousMetadata, boolean pre) { }

   @Override
   protected void notifyCacheEntryExpired(WrappedBytes key, WrappedBytes value, Metadata metadata) { }

   @Override
   protected void notifyCacheEntryRemoved(WrappedBytes key, WrappedBytes previousValue, Metadata previousMetadata,
         boolean pre) { }

   @Override
   protected AggregateCompletionStage<Void> prepareMultipleNotifications(Class<? extends Annotation> annotationClass) {
      return null;
   }

   @Override
   protected void awaitNotifications(AggregateCompletionStage<Void> aggregate) { }

   @Override
   protected void notifyCacheEntryVisited(AggregateCompletionStage<Void> aggregate, WrappedBytes key,
         WrappedBytes value) { }

   @Override
   protected void notifyCacheEntryModified(AggregateCompletionStage<Void> aggregate, WrappedBytes key,
         WrappedBytes value, Metadata metadata, WrappedBytes previousValue, Metadata previousMetadata, boolean pre) { }

   @Override
   public void evict(WrappedBytes key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public AdvancedCache<WrappedBytes, WrappedBytes> lockAs(Object lockOwner) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean lock(WrappedBytes... keys) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean lock(Collection<? extends WrappedBytes> keys) {
      throw new UnsupportedOperationException();
   }

   @Override
   public RpcManager getRpcManager() {
      throw new UnsupportedOperationException();
   }

   @Override
   public BatchContainer getBatchContainer() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Map<WrappedBytes, WrappedBytes> getGroup(String groupName) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeGroup(String groupName) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<Boolean> touch(Object key, boolean touchEvenIfExpired) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<Boolean> touch(Object key, int segment, boolean touchEvenIfExpired) {
      throw new UnsupportedOperationException();
   }

   @Override
   public LockedStream<WrappedBytes, WrappedBytes> lockedStream() {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletableFuture<Boolean> removeLifespanExpired(WrappedBytes key, WrappedBytes value, Long lifespan) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletableFuture<Boolean> removeMaxIdleExpired(WrappedBytes key, WrappedBytes value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean startBatch() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void endBatch(boolean successful) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <C> CompletionStage<Void> addListenerAsync(Object listener, CacheEventFilter<? super WrappedBytes, ? super WrappedBytes> filter, CacheEventConverter<? super WrappedBytes, ? super WrappedBytes, C> converter) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <C> CompletionStage<Void> addFilteredListenerAsync(Object listener, CacheEventFilter<? super WrappedBytes, ? super WrappedBytes> filter, CacheEventConverter<? super WrappedBytes, ? super WrappedBytes, C> converter, Set<Class<? extends Annotation>> filterAnnotations) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <C> CompletionStage<Void> addStorageFormatFilteredListenerAsync(Object listener, CacheEventFilter<? super WrappedBytes, ? super WrappedBytes> filter, CacheEventConverter<? super WrappedBytes, ? super WrappedBytes, C> converter, Set<Class<? extends Annotation>> filterAnnotations) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<Void> addListenerAsync(Object listener) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CompletionStage<Void> removeListenerAsync(Object listener) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<Object> getListeners() {
      throw new UnsupportedOperationException();
   }
}
