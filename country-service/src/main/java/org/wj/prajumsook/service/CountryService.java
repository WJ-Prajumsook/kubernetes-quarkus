package org.wj.prajumsook.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.wj.prajumsook.model.Country;
import org.wj.prajumsook.reposigory.CountryRepository;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CountryService {

  private List<Country> countries = new ArrayList<>();

  @Inject
  CountryRepository reposigory;

  @Inject
  @Metric(
    name = "PersistData",
    description = "Persist data histogram"
  )
  Histogram histogram;

  @Transactional
  public void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("country.json");
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
      countries = mapper.readValue(jsonNode.toString(), new TypeReference<List<Country>>() {});
      
      countries.forEach(c -> reposigory.persist(c));
      log.info(countries.size() + " loaded to database.");
      
      histogram.update(countries.size());
    } catch(Exception ex) {
      log.error(ex.getMessage());
      ex.printStackTrace();
      throw new WebApplicationException("Error reading file from resource.");
    }
  }

  public List<Country> findAll() {
    Optional<List<Country>> opts = reposigory.findAllCountries();
    return opts.orElseThrow(() -> new WebApplicationException("Error find all countries.", 404));
    //return countries;
  }

  public Country findByName(String name) {
    Optional<Country> country = reposigory.findByName(name); 
      //countries.stream()
      //.filter(c -> c.getName().equalsIgnoreCase(name))
      //.findFirst();

    return country.orElseThrow(() -> new WebApplicationException("Country name '" + name + "' not found.", 404));
  }

  public Country findByCode(String code) {
    Optional<Country> country = reposigory.findByCode(code); 
      //countries.stream()
      //.filter(c -> c.getCode().equalsIgnoreCase(code))
      //.findFirst();

    return country.orElseThrow(() -> new WebApplicationException("Country code '" + code + "' not found.", 404));
  }

  public List<Country> findByRegion(String region) {
    /*return countries.stream()
      .filter(c -> c.getRegion().equalsIgnoreCase(region))
      .map(
        c -> new Country()
          .setName(c.getName())
          .setCode(c.getCode())
          .setRegion(c.getRegion())
          .setCurrency(c.getCurrency())
          .setLanguage(c.getLanguage())
          .setFlag(c.getFlag())
      ).collect(Collectors.toList());*/
    Optional<List<Country>> opts = reposigory.findByRegion(region);
    return opts.orElseThrow(() -> new WebApplicationException("Error find country by region.", 404));
  }
}
