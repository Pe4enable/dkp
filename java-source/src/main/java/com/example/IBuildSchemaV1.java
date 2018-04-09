package com.example;

import com.example.IBuildSchema;
import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * An IOUState schema.
 */
public class IBuildSchemaV1 extends MappedSchema {
    public IBuildSchemaV1() {
        super(IBuildSchema.class, 1, ImmutableList.of(PersistentBuild.class));
    }

    @Entity
    @Table(name = "build_states")
    public static class PersistentBuild extends PersistentState {
        @Column(name = "ahml") private final String ahml;
        @Column(name = "developer") private final String developer;
        @Column(name = "client") private final String client;
        @Column(name = "price") private final double price;
        //@Column(name = "createDate") private final Date createDate;
        @Column(name = "buildUId") private final  UUID buildUID;
        @Column(name = "description") private final String description;
        @Column(name = "address") private final String address;
        @Column(name = "area") private final double area;
        @Column(name = "comment") private final String comment;
        @Column(name = "status") private final String status;

        public PersistentBuild(
                String ahml,
                String developer,
                String client,
                double price,
                UUID buildUId,
              //  Date createDate,
                String description,
                String address,
                double area,
                String comment,
                String status) {
            this.ahml = ahml;
            this.price = price;
            this.developer = developer;
            this.buildUID = buildUId;
            //this.createDate = createDate;
            this.description=description;
            this.address=address;
            this.area=area;
            this.comment=comment;
            this.status=status;
            this.client=client;
        }

        // Default constructor required by hibernate.
        public PersistentBuild() {
            this.ahml = null;
            this.price = 0.0;
            this.developer = null;
           // this.createDate = new Date();
            this.buildUID = null;
            this.comment="";
            this.description="";
            this.area=0.0;
            this.address="";
            this.status="";
            this.client=null;
        }

        public String getAhml() {
            return ahml;
        }

        public String getDeveloper() {
            return developer;
        }

        public String getClient() {return client;}

        public double getPrice() {
            return price;
        }

        public UUID getId() {
            return buildUID;
        }

        public String getDescription() {return description;}
        public String getComment() {return comment;}
        public double getArea() {return area;}
        public String getBuildAddress() {return address;}
        public String getStatus() {return status;}
    }
}