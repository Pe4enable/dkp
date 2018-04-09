package com.example;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.List;

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
public class IBuildState implements LinearState, QueryableState {
    private final double price;
    private final Party ahml;
    private final Party developer;
    private final Party client;
    //private final Date createDate;
    private final UniqueIdentifier buildUID;
    private final String description;
    private final String address;
    private final double area;
    private final String comment;
    private final String status;

    /**
     * @param price the value of the IOU.
     * @param ahml the party issuing the IOU.
     * @param developer the party receiving and approving the IOU.
     * @param description the party receiving and approving the IOU.
     * @param description
     * @param address
     * @param area
     * @param comment
     * @param status
     * @param client
     */

    public IBuildState(double price,
                       Party ahml,
                       Party developer,
                       Party client,
                       String description,
                       String address,
                       double area,
                       String comment,
                       String status)
    {
        this.price = price;
        this.ahml = ahml;
        this.description = description;
        this.developer = developer;
        this.buildUID = new UniqueIdentifier();
        this.address=address;
        this.area=area;
        this.comment=comment;
        this.status=status;
        this.client=client;
        //this.createDate= new Date();
    }

    @ConstructorForDeserialization
    public IBuildState(double price,
                       Party ahml,
                       Party developer,
                       Party client,
                       UniqueIdentifier buildUID,
                       String description,
                       String address,
                       double area,
                       String comment,
                       String status)
    {
        this.price = price;
        this.ahml = ahml;
        this.description = description;
        this.developer = developer;
        this.buildUID = buildUID;
        this.address=address;
        this.area=area;
        this.comment=comment;
        this.status=status;
        this.client=client;
        //this.createDate= new Date();
    }

    public double getPrice() { return price; }
    public Party getAhml() { return ahml; }
    public Party getDeveloper() { return developer; }
    public Party getClient() { return client; }

    public String getDescription() {return description;}
    public String getComment() {return comment;}
    public double getArea() {return area;}
    public String getBuildAddress() {return address;}
    public String getStatus() {return status;}

    public UniqueIdentifier getBuildUID() { return buildUID; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return buildUID;
    }

    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList( ahml, developer, client);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IBuildSchemaV1) {
            return new IBuildSchemaV1.PersistentBuild(
                    this.ahml.getName().toString(),
                    this.developer.getName().toString(),
                    this.client.getName().toString(),
                    this.price,
                    this.buildUID.getId(),
                   // this.createDate,
                    this.description,
                    this.address,
                    this.area,
                    this.comment,
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
                "%s(price=%s, ahml=%s, developer=%s, buildId=%s, description=%s)", getClass().getSimpleName(),
                price, ahml,  developer, buildUID, description);
    }
}