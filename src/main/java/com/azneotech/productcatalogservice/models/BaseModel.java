package com.azneotech.productcatalogservice.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

/**
 * Identity and audit fields shared by every model in the product-catalog.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel {

    @Id
    private Long id;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date lastUpdatedAt;

    private State state;

    public BaseModel() {
        this.state = State.ACTIVE;
    }

}
