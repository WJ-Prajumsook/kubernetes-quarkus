package org.wj.prajumsook.countries;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.wj.prajumsook.model.Country;
import org.wj.prajumsook.service.CountryService;

import io.quarkus.security.Authenticated;

@Path("/countries")
@Authenticated
public class CountryResource {

  @Inject
  CountryService countryService;

  
  @GET
  @Path("/tmp")
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    return "Hello from RESTEasy Reactive";
  }

  @Timed(
    name = "CountryResourceProcessed",
    description = "Monitor the time to process countries",
    unit = MetricUnits.MILLISECONDS,
    absolute = true
  )
  @GET
  @Path("/init")
  public void init() {
    countryService.init();
  }

  @Metered(
    name = "Find all countries meter",
    description = "Monitor the rate events occured",
    unit = MetricUnits.MILLISECONDS,
    absolute = true
  )
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Country> findAll() {
    return countryService.findAll();
  }

  @Counted(
    name = "Find by name counter",
    description = "Monitor how many time findByName was called",
    unit = MetricUnits.NONE,
    absolute = true
  )
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/name/{name}")
  public Country findByName(@PathParam("name")String name) {
    return countryService.findByName(name);
  }
  
  @Counted(
    name = "Find by code counter",
    description = "Monitor how many time findByCode was called",
    unit = MetricUnits.NONE,
    absolute = true
  )
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/code/{code}")
  public Country findByCode(@PathParam("code")String code) {
    return countryService.findByCode(code);
  }

  @Counted(
    name = "Find by region counter",
    description = "Monitor how many time findByRegion was called",
    unit = MetricUnits.NONE,
    absolute = true
  )
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/region/{region}")
  public List<Country> findByRegion(@PathParam("region")String region) {
    return countryService.findByRegion(region);
  }

}
