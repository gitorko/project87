package com.demo.project87.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "ticket")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 45)
    private String seatNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @Size(max = 45)
    private String bookedBy;
    @Size(max = 45)
    private String lockedBy;
    //Never expose TTL over json.
    @JsonIgnore
    private LocalDateTime lockExpiry;
    @Column(nullable = false)
    @Builder.Default
    private Boolean booked = false;
    @Column(nullable = false)
    @Builder.Default
    private Boolean entered = false;
    private String entryToken;
    private Double price;
    @Lob
    @Type(type = "org.hibernate.type.ImageType")
    @JsonIgnore
    @ToString.Exclude
    private byte[] qrCode;
    @Version
    private int version;

}
