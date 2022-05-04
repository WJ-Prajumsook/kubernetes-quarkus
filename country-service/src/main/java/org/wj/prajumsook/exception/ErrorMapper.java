package org.wj.prajumsook.exception;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class ErrorMapper implements ExceptionMapper<Exception> {

  @Metric(
    name = "ErrorMapperCounter",
    description = "Number of times the country service errormapper is invoked."
  )
  Counter errormapperCounter;  

  @Override
  public Response toResponse(Exception ex) {
    int statusCode = 500;

    if(ex instanceof WebApplicationException) {
      statusCode = ((WebApplicationException)ex).getResponse().getStatus();
    }
    JsonObjectBuilder builder = Json.createObjectBuilder()
      .add("exceptionType", ex.getClass().getName())
      .add("code", statusCode);
    if(ex.getMessage() != null) {
      builder.add("error", ex.getMessage());
    }

    log.error(builder.build().toString());
    errormapperCounter.inc();

    return Response.status(statusCode)
      .entity(builder.build())
      .build();
  }
}
