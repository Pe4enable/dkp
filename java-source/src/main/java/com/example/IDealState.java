package com.example;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
public class IDealState implements LinearState, QueryableState {
    private final Party client;
    private final Party bank;
    private final Party developer;
    private final Party ahml;
    //private final Date createDate;
   // private final UniqueIdentifier buildUID;
    private final String buildUID;
    private final UniqueIdentifier dealUID;
    private final String status;

    /**
     * @param client the value of the IOU.
     * @param developer the party issuing the IOU.

     * @param buildUID the party receiving and approving the IOU.
     * @param status
     */

    public IDealState(Party client,
                      Party developer,
                      Party ahml,
                      //UniqueIdentifier buildUID,
                      String buildUID,
                      String status)
    {
        this.client = client;
        //this.ahml = ahml;
        this.dealUID = new UniqueIdentifier();
        this.buildUID=buildUID;
        this.status=status;
        this.developer=developer;
        this.bank=null;
        this.ahml=ahml;
        //this.createDate= new Date();
    }

    @ConstructorForDeserialization
    public IDealState(Party client,
                      Party bank,
                      Party ahml,
                      Party developer,
                      //UniqueIdentifier buildUID,
                      String buildUID,
                      UniqueIdentifier dealUID,
                      String status)
    {
        this.client = client;
        this.ahml = ahml;
        this.bank = bank;
        this.developer = developer;
        this.buildUID = buildUID;
        this.dealUID=dealUID;
        this.status=status;
        //this.createDate= new Date();
    }

    public Party getClient() { return client; }
    public Party getAhml() { return ahml; }
    public Party getDeveloper() { return developer; }
    public Party getBank() {return bank;}
    public String getStatus() {return status;}
    //public UniqueIdentifier getBuildUID() { return buildUID; }
    public String getBuildUID() { return buildUID; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return dealUID;
    }

    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList( ahml, developer, bank, client);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IDealSchemaV1) {
            return new IDealSchemaV1.PersistentBuild(
                    this.ahml.getName().toString(),
                    this.developer.getName().toString(),
                    this.bank.getName().toString(),
                    this.client.getName().toString(),
                    //this.buildUID.getId(),
                    this.buildUID,
                    this.dealUID.getId(),
                   // this.createDate,
                    this.status);
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new IBuildSchemaV1());
    }

    @Override
    public String toString() {
        return String.format(
                "%s( ahml=%s, developer=%s, buildId=%s)", getClass().getSimpleName(),
                 ahml,  developer, buildUID);
    }
}