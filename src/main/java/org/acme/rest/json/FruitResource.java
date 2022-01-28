package org.acme.rest.json;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.security.auth.Subject;
import javax.ws.rs.Path;

import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.context.Flag;
import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.RequestHeader;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.framework.ContentSource;
import org.infinispan.rest.framework.Invocation;
import org.infinispan.rest.framework.LookupResult;
import org.infinispan.rest.framework.Method;
import org.infinispan.rest.framework.ResourceManager;
import org.infinispan.rest.framework.RestRequest;
import org.infinispan.rest.framework.RestResponse;
import org.infinispan.rest.framework.impl.InvocationImpl;
import org.infinispan.rest.framework.impl.ResourceManagerImpl;
import org.infinispan.rest.operations.exceptions.InvalidFlagException;
import org.infinispan.rest.resources.CacheResourceV2;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;

@Path("v2")
public class FruitResource {
    private ResourceManager resourceManager;

    void onStart(@Observes StartupEvent ev) {
        resourceManager = new ResourceManagerImpl();
        InvocationHelper invocationHelper = new OurInvocationHelper(null,
              (RestCacheManager) new OurRestCacheManager(ConcurrentHashMap::new),
              null, null, null, null, null, null);
        CacheResourceV2 cacheResourceV2 = new OurV2Resource(invocationHelper);

        resourceManager.registerResource("/", cacheResourceV2);
    }

    @Path("{var:.*}")
    public void allParams(HttpServerRequest request) {
        OurRequest ourRequest = new OurRequest(request);
        LookupResult result = resourceManager.lookupResource(ourRequest.method(), ourRequest.uri());
        if (result.getStatus() == LookupResult.Status.FOUND) {
            Map<String, String> getVariables = result.getVariables();
            ourRequest.setVariables(getVariables);
            handleInvocation(request, ourRequest, result.getInvocation());
        } else {
            request.response().end("Status was: " + result.getStatus());
        }
    }

    Future<Void> handleInvocation(HttpServerRequest request, OurRequest ourRequest, Invocation invocation) {
        if (invocation.requiresBody()) {
            Future<Buffer> body = request.body();
            if (!body.isComplete()) {
                CompletableFuture<Void> stage = new CompletableFuture<>();
                request.endHandler(__ -> {
                    handleInvocation(request, ourRequest, invocation).onComplete(r -> {
                       if (r.succeeded()) {
                           stage.complete(null);
                       } else {
                           stage.completeExceptionally(r.cause());
                       }
                    });
                });
                return Future.fromCompletionStage(stage);
            }
        }
        CompletionStage<RestResponse> response = invocation.handler().apply(ourRequest);
        return Future.fromCompletionStage(response)
              .flatMap(rr -> {
                  if (rr != null && !(rr instanceof VertxRestResponse)) {
                      throw new IllegalArgumentException("Only VertxRestResponse supported!");
                  }
                  return ((VertxRestResponse) rr).process();
              });
    }

    static class OurV2Resource extends CacheResourceV2 {

        public OurV2Resource(InvocationHelper invocationHelper) {
            super(invocationHelper);
        }

        @Override
        protected InvocationImpl.Builder addOtherOperations(InvocationImpl.Builder builder) {
            return builder;
        }
    }

    static class OurRequest implements RestRequest {

        private static final MediaType DEFAULT_KEY_CONTENT_TYPE = MediaType.fromString("text/plain; charset=utf-8");

        private final HttpServerRequest httpServerRequest;

        private Map<String, String> variables;

        OurRequest(HttpServerRequest httpServerRequest) {
            this.httpServerRequest = httpServerRequest;
        }

        public HttpServerRequest getHttpServerRequest() {
            return httpServerRequest;
        }

        @Override
        public Method method() {
            switch (httpServerRequest.method().name()) {
                case "GET":
                    return Method.GET;
                case "PUT":
                    return Method.PUT;
                case "POST":
                    return Method.POST;
                case "HEAD":
                    return Method.HEAD;
                case "DELETE":
                    return Method.DELETE;
                case "OPTIONS":
                    return Method.OPTIONS;
                default:
                    throw new UnsupportedOperationException("Method was: " + httpServerRequest.method());
            }
        }

        @Override
        public String path() {
            return httpServerRequest.path();
        }

        @Override
        public String uri() {
            return httpServerRequest.uri();
        }

        @Override
        public ContentSource contents() {
            Future<Buffer> bufferFuture = httpServerRequest.body();
            if (bufferFuture.succeeded()) {
                return new ContentSource() {
                    @Override
                    public String asString() {
                        byte[] content = rawContent();
                        return new String(content, 0, content.length, StandardCharsets.UTF_8);
                    }

                    @Override
                    public byte[] rawContent() {
                        return bufferFuture.result().getBytes();
                    }
                };
            }
            throw new UnsupportedOperationException("");
//            Uni<Buffer> bufferFuture = httpServerRequest.body();
//            // TODO: add in a way to retrieve non blocking?
//            Buffer buffer = bufferFuture.await().atMost(Duration.ZERO);
//            return new ContentSource() {
//                @Override
//                public String asString() {
//                    byte[] content = rawContent();
//                    return new String(content, 0, content.length, StandardCharsets.UTF_8);
//                }
//
//                @Override
//                public byte[] rawContent() {
//                    return buffer.getBytes();
//                }
//            };
        }

        @Override
        public Map<String, List<String>> parameters() {
            List<Map.Entry<String, String>> listEntries = httpServerRequest.params().entries();
            return listEntries.stream()
                  .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }

        @Override
        public String getParameter(String name) {
            return httpServerRequest.getParam(name);
        }

        @Override
        public Map<String, String> variables() {
            return variables;
        }

        @Override
        public String getAction() {
            List<String> actions = parameters().get("action");
            if (actions != null) {
                return actions.iterator().next();
            }
            return null;
        }

        @Override
        public MediaType contentType() {
            String contentTypeHeader = getContentTypeHeader();
            if (contentTypeHeader == null) return MediaType.MATCH_ALL;
            return MediaType.fromString(contentTypeHeader);
        }

        @Override
        public MediaType keyContentType() {
            String header = httpServerRequest.headers().get(RequestHeader.KEY_CONTENT_TYPE_HEADER.getValue());
            if (header == null) return DEFAULT_KEY_CONTENT_TYPE;
            return MediaType.fromString(header);
        }

        @Override
        public String getAcceptHeader() {
            String accept = httpServerRequest.headers().get(HttpHeaderNames.ACCEPT);
            return accept == null ? MediaType.MATCH_ALL_TYPE : accept;
        }

        @Override
        public String getAuthorizationHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.AUTHORIZATION);
        }

        @Override
        public String getCacheControlHeader() {
            String value = httpServerRequest.headers().get(HttpHeaderNames.CACHE_CONTROL);
            if (value == null) return "";
            return value;
        }

        @Override
        public String getContentTypeHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        }

        @Override
        public String getEtagIfMatchHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.IF_MATCH);
        }

        @Override
        public String getIfModifiedSinceHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        }

        @Override
        public String getEtagIfNoneMatchHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.IF_NONE_MATCH);
        }

        @Override
        public String getIfUnmodifiedSinceHeader() {
            return httpServerRequest.headers().get(HttpHeaderNames.IF_UNMODIFIED_SINCE);
        }

        private Long getHeaderAsLong(String header) {
            String headerValue = httpServerRequest.headers().get(header);
            if (headerValue == null) return null;
            try {
                return Long.valueOf(headerValue);
            } catch (NumberFormatException e) {
//                logger.warnInvalidNumber(header, headerValue);
                return null;
            }
        }

        @Override
        public Long getMaxIdleTimeSecondsHeader() {
            return getHeaderAsLong(RequestHeader.MAX_TIME_IDLE_HEADER.getValue());
        }


        @Override
        public Long getTimeToLiveSecondsHeader() {
            return getHeaderAsLong(RequestHeader.TTL_SECONDS_HEADER.getValue());
        }

        @Override
        public EnumSet<CacheContainerAdmin.AdminFlag> getAdminFlags() {
            String requestFlags = httpServerRequest.headers().get(RequestHeader.FLAGS_HEADER.getValue());
            if (requestFlags == null || requestFlags.isEmpty()) return null;
            try {
                return CacheContainerAdmin.AdminFlag.fromString(requestFlags);
            } catch (IllegalArgumentException e) {
                throw new InvalidFlagException(e);
            }
        }

        @Override
        public Flag[] getFlags() {
            try {
                String flags = httpServerRequest.headers().get(RequestHeader.FLAGS_HEADER.getValue());
                if (flags == null || flags.isEmpty()) {
                    return null;
                }
                return Arrays.stream(flags.split(",")).filter(s -> !s.isEmpty()).map(Flag::valueOf).toArray(Flag[]::new);
            } catch (IllegalArgumentException e) {
                throw new InvalidFlagException(e);
            }
        }

        @Override
        public Long getCreatedHeader() {
            return getHeaderAsLong(RequestHeader.CREATED_HEADER.getValue());
        }

        @Override
        public Long getLastUsedHeader() {
            return getHeaderAsLong(RequestHeader.LAST_USED_HEADER.getValue());
        }

        @Override
        public Subject getSubject() {
            return null;
        }

        @Override
        public void setSubject(Subject subject) {

        }

        @Override
        public void setVariables(Map<String, String> variables) {
            this.variables = variables;
        }

        @Override
        public void setAction(String action) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String header(String name) {
            return httpServerRequest.headers().get(name);
        }

        @Override
        public List<String> headers(String name) {
            return httpServerRequest.headers().getAll(name);
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return (InetSocketAddress) httpServerRequest.remoteAddress();
        }
    }
}