package org.wj.prajumsook.reposigory;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.wj.prajumsook.model.Country;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped  
public class CountryRepository implements PanacheRepository<Country> {

  public Optional<List<Country>> findAllCountries() {
    return Optional.ofNullable(list("order by name"));
  }

  public Optional<Country> findByName(String name) {
    return Optional.ofNullable(find("LOWER(name) = LOWER(?1)", name).firstResult());
  }

  public Optional<Country> findByCode(String code) {
    return Optional.ofNullable(find("LOWER(code) = LOWER(?1)", code).firstResult());
  }

  public Optional<List<Country>> findByRegion(String region) {
    return Optional.ofNullable(find("LOWER(region) = LOWER(?1)", region).list());
  }
  
}
