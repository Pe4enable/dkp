package com.example;

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
public class IDealSchemaV1 extends MappedSchema {
    public IDealSchemaV1() {
        super(IDealSchema.class, 1, ImmutableList.of(PersistentBuild.class));
    }

    @Entity
    @Table(name = "deal_states")
    public static class PersistentBuild extends PersistentState {
        @Column(name = "ahml") private final String ahml;
        @Column(name = "developer") private final String developer;
        @Column(name = "bank") private final String bank;
        //@Column(name = "createDate") private final Date createDate;
        //@Column(name = "buildUId") private final  UUID buildUID;
        @Column(name = "buildUId") private final  String buildUID;
        @Column(name = "dealUId") private final UUID dealUID;
        @Column(name = "client") private final String client;
        @Column(name = "status") private final String status;

        public PersistentBuild(
                String ahml,
                String developer,
                String bank,
                String client,
                //UUID buildUId,
                String buildUId,
                UUID dealUId,
              //  Date createDate,
                String status) {
            this.ahml = ahml;
            this.bank = bank;
            this.developer = developer;
            this.buildUID = buildUId;
            //this.createDate = createDate;
            this.client=client;
            this.dealUID=dealUId;
            this.status=status;
        }

        // Default constructor required by hibernate.
        public PersistentBuild() {
            this.ahml = null;
            this.client = null;
            this.developer = null;
            this.bank = null;
           // this.createDate = new Date();
            this.buildUID = null;
            this.dealUID=null;
            this.status="";
        }

        public String getAhml() {
            return ahml;
        }

        public String getDeveloper() {
            return developer;
        }

        public String getClient() {
            return client;
        }

        public UUID getId() {
            return dealUID;
        }

        public String getBank() {return bank;}

        //public UUID getBuildId() {return buildUID;}
        public String getBuildId() {return buildUID;}

        public String getStatus() {return status;}
    }
}