package net.apmoller.crb.telikos.microservices.cache.library.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "booking")
public class Booking implements Persistable, Serializable {


    @Column
    @Id
    private String id;
    @Column
    private String description;
    @Column
    private String price;
    @Column
    private String qtyavailable;


    @Transient
    private boolean newBooking;

    @Override
    @Transient
    public boolean isNew() {
        return this.newBooking || id == null;
    }

    public Booking setAsNew(){
        this.newBooking = true;
        return this;
    }

}
