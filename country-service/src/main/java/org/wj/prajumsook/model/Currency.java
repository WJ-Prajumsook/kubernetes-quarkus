package org.wj.prajumsook.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "currency")
public class Currency {

  @Id
  @SequenceGenerator(name = "currencySequence", sequenceName = "currency_id_seq", allocationSize = 1, initialValue = 10)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currencySequence")
  @Column(name = "id")
  private Long id;

  @Getter @Setter
  private String code;
  
  @Getter @Setter
  private String name;
  @Getter @Setter
  private String symbol;

  @OneToOne(mappedBy = "currency")
  private Country country;

}
