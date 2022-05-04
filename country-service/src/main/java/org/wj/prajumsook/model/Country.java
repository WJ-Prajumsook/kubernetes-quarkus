package org.wj.prajumsook.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Country {

  @Id
  @SequenceGenerator(name = "countrySequence", sequenceName = "country_id_seq", allocationSize = 1, initialValue = 100)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "countrySequence")
  private Long id;

  private String name;
  private String code;
  private String capital;
  private String region;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "currency_id", referencedColumnName = "id")
  private Currency currency;
  
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "language_id", referencedColumnName = "id")
  private Language language;
  private String flag;

}
