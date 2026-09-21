package com.azneotech.productcatalogservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate customer rating, mirroring FakeStore's {@code rating: {rate, count}}.
 * Column names are explicit because Hibernate would otherwise map these to
 * {@code rate} / {@code count}, and {@code count} is an awkward column name.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Rating {

    @Column(name = "rating_rate")
    private Double rate;

    @Column(name = "rating_count")
    private Integer count;
}
