package org.acme.rest.json;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.WrappedBytes;
import org.infinispan.rest.CacheControl;
import org.infinispan.rest.DateUtils;
import org.infinispan.rest.ResponseHeader;
import org.infinispan.rest.framework.RestResponse;
import org.infinispan.rest.framework.impl.RestResponseBuilder;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.http.HttpServerResponse;

public class VertxRestResponse implements RestResponse {
   private final VertxRestResponse.Builder builder;

   private VertxRestResponse(VertxRestResponse.Builder builder) {
      this.builder = builder;
   }

   @Override
   public int getStatus() {
      return builder.getStatus();
   }

   @Override
   public Object getEntity() {
      return builder.getEntity();
   }

   public Uni<Void> process() {
      Object entity = getEntity();
      if (entity != null) {
         if (entity instanceof WrappedBytes) {
            // TODO: this is assuming no offsets
            return builder.serverResponse.end(Buffer.buffer(((WrappedBytes) entity).getBytes()));
         } else if (entity instanceof String) {
            return builder.serverResponse.end((String) entity);
         }
      }
      return builder.serverResponse.end();
   }

   public static class Builder implements RestResponseBuilder<VertxRestResponse.Builder> {

      private final HttpServerResponse serverResponse;
      private Object entity;

      public Builder(HttpServerResponse serverResponse) {
         this.serverResponse = serverResponse.setStatusCode(HttpResponseStatus.OK.code());
      }

      @Override
      public VertxRestResponse build() {
         return new VertxRestResponse(this);
      }

      @Override
      public VertxRestResponse.Builder status(int status) {
         serverResponse.setStatusCode(status);
         return this;
      }

      @Override
      public VertxRestResponse.Builder entity(Object entity) {
         this.entity = entity;
         return this;
      }

      @Override
      public VertxRestResponse.Builder cacheControl(CacheControl cacheControl) {
         return header(ResponseHeader.CACHE_CONTROL_HEADER.getValue(), cacheControl.toString());
      }

      @Override
      public VertxRestResponse.Builder header(String name, Object value) {
         Objects.requireNonNull(name);
         serverResponse.headers().add(name, value.toString());
         return this;
      }

      @Override
      public VertxRestResponse.Builder contentType(MediaType type) {
         return contentType(type.toString());
      }

      @Override
      public VertxRestResponse.Builder contentType(String type) {
         return header(ResponseHeader.CONTENT_TYPE_HEADER.getValue(), type);
      }

      @Override
      public VertxRestResponse.Builder contentLength(long length) {
         return header(ResponseHeader.CONTENT_LENGTH_HEADER.getValue(), Long.toString(length));
      }

      @Override
      public VertxRestResponse.Builder expires(Date expires) {
         return setDateHeader(ResponseHeader.EXPIRES_HEADER.getValue(), expires.getTime());
      }

      @Override
      public VertxRestResponse.Builder lastModified(Long epoch) {
         return setDateHeader(ResponseHeader.LAST_MODIFIED_HEADER.getValue(), epoch);
      }

      @Override
      public VertxRestResponse.Builder addProcessedDate(Date d) {
         return setDateHeader(ResponseHeader.DATE_HEADER.getValue(), d.getTime());
      }

      @Override
      public VertxRestResponse.Builder location(String location) {
         return header(ResponseHeader.LOCATION.getValue(), location);
      }

      @Override
      public VertxRestResponse.Builder eTag(String tag) {
         return header(ResponseHeader.ETAG_HEADER.getValue(), tag);
      }

      @Override
      public int getStatus() {
         return serverResponse.getStatusCode();
      }

      @Override
      public Object getEntity() {
         return entity;
      }

      @Override
      public Object getHeader(String header) {
         return serverResponse.headers();
      }

      @Override
      public VertxRestResponse.Builder timeToLive(long timeToLive) {
         if (timeToLive > -1)
            return header(ResponseHeader.TIME_TO_LIVE_HEADER.getValue(), TimeUnit.MILLISECONDS.toSeconds(timeToLive));
         return this;
      }

      @Override
      public VertxRestResponse.Builder maxIdle(long maxIdle) {
         if (maxIdle > -1)
            return header(ResponseHeader.MAX_IDLE_TIME_HEADER.getValue(), TimeUnit.MILLISECONDS.toSeconds(maxIdle));
         return this;
      }

      @Override
      public VertxRestResponse.Builder created(long created) {
         if (created > -1)
            return header(ResponseHeader.CREATED_HEADER.getValue(), TimeUnit.MILLISECONDS.toSeconds(created));
         return this;
      }

      @Override
      public VertxRestResponse.Builder lastUsed(long lastUsed) {
         if (lastUsed > -1)
            return header(ResponseHeader.LAST_USED_HEADER.getValue(), TimeUnit.MILLISECONDS.toSeconds(lastUsed));
         return this;
      }

      private VertxRestResponse.Builder setDateHeader(String name, long epoch) {
         String value = DateUtils.toRFC1123(epoch);
         return header(name, value);
      }
   }
}
