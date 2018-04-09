package com.example.api;

import com.example.IBuildSchemaV1;
import com.example.IDealState;
import com.example.flow.BookFlow;
import com.example.flow.BuildFlow;
import com.example.IBuildState;
import com.example.flow.DealFlow;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowProgressHandle;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

// This API is accessible from /api/example. All paths specified below are relative to it.
@Path("example")
public class ExampleApi {
    private final CordaRPCOps rpcOps;
    private final CordaX500Name myLegalName;

    private final List<String> serviceNames = ImmutableList.of("Notary", "Network Map Service");

    static private final Logger logger = LoggerFactory.getLogger(ExampleApi.class);

    public ExampleApi(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myLegalName = rpcOps.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodeInfoSnapshot = rpcOps.networkMapSnapshot();
        return ImmutableMap.of("peers", nodeInfoSnapshot
                .stream()
                .map(node -> node.getLegalIdentities().get(0).getName())
                .filter(name -> !name.equals(myLegalName) && !serviceNames.contains(name.getOrganisation()))
                .collect(toList()));
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     *
     * Once the flow finishes it will have written the IOU to ledger. Both the lender and the borrower will be able to
     * see it when calling /api/example/ious on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @GET
    @Path("builds")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<IBuildState>> getBuilds() {
        return rpcOps.vaultQuery(IBuildState.class).getStates();
    }

    @GET
    @Path("deals")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<IDealState>> getDeals() { return rpcOps.vaultQuery(IDealState.class).getStates();
    }

    @PUT
    @Path("create-build")
    public Response createBuild(
            @QueryParam("price") double price,
            @QueryParam("ahmlName") CordaX500Name ahmlName,
            @QueryParam("clientName") CordaX500Name clientName,
            @QueryParam("description") String description,
            @QueryParam("address") String address,
            @QueryParam("comment") String comment,
            @QueryParam("area") double area
    ) throws InterruptedException, ExecutionException {
        if (price <= 0) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'price' must be non-negative.\n").build();
        }
        if (ahmlName == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'ahmlName' missing or has wrong format.\n").build();
        }
        if (clientName == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'clientName' missing or has wrong format.\n").build();
        }

        final Party otherParty = rpcOps.wellKnownPartyFromX500Name(ahmlName);
        if (otherParty == null) {
            return Response.status(BAD_REQUEST).entity("Party named " + ahmlName + "cannot be found.\n").build();
        }

        final Party clientParty = rpcOps.wellKnownPartyFromX500Name(clientName);
        if (clientParty == null) {
            return Response.status(BAD_REQUEST).entity("Party named " + clientName + "cannot be found.\n").build();
        }

        try {
            FlowProgressHandle<SignedTransaction> flowHandle = rpcOps
                    .startTrackedFlowDynamic(
                            BuildFlow.Initiator.class, price, otherParty, clientParty, description,
                            address,
                            comment, area, "created");
            flowHandle.getProgress().subscribe(evt -> System.out.printf(">> %s\n", evt));

            // The line below blocks and waits for the flow to return.
            final SignedTransaction result = flowHandle
                    .getReturnValue()
                    .get();

            final String msg = String.format("Transaction id %s committed to ledger.\n", result.getId());
            return Response.status(CREATED).entity(msg).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Path("create-deal")
    public Response createDeal(
            @QueryParam("ahml") CordaX500Name ahmlName,
            @QueryParam("developer") CordaX500Name developerName,
            @QueryParam("buildId") String buildId
    ) throws InterruptedException, ExecutionException {
        System.out.println(buildId);
        if (buildId == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'price' must be non-negative.\n").build();
        }
        final Party ahmlParty = rpcOps.wellKnownPartyFromX500Name(ahmlName);
        if (ahmlParty == null) {
            return Response.status(BAD_REQUEST).entity("Party named " + ahmlName + "cannot be found.\n").build();
        }

        final Party devParty = rpcOps.wellKnownPartyFromX500Name(developerName);
        if (devParty == null) {
            return Response.status(BAD_REQUEST).entity("Party named " + developerName + "cannot be found.\n").build();
        }
        //if (partyName == null) {
        //    return Response.status(BAD_REQUEST).entity("Query parameter 'partyName' missing or has wrong format.\n").build();
        //}

       // final Party otherParty = rpcOps.wellKnownPartyFromX500Name(partyName);
        //if (otherParty == null) {
        //    return Response.status(BAD_REQUEST).entity("Party named " + partyName + "cannot be found.\n").build();
        //}

        try {
            System.out.println("create tx");
            FlowProgressHandle<SignedTransaction> flowHandle = rpcOps
                    .startTrackedFlowDynamic(
                            DealFlow.Initiator.class,  buildId,ahmlParty,devParty, "created");
            System.out.println("get progress tx");
            flowHandle.getProgress().subscribe(evt -> System.out.printf(">> %s\n", evt));

            System.out.println("sign tx");
            // The line below blocks and waits for the flow to return.
            final SignedTransaction result = flowHandle
                    .getReturnValue()
                    .get();
            System.out.println("signed tx");
            final String msg = String.format("Transaction id %s committed to ledger.\n", result.getId());
            return Response.status(CREATED).entity(msg).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Path("book-build")
    public Response bookBuild(
            @QueryParam("buildId") String buildId
    ) throws InterruptedException, ExecutionException {

        if (buildId == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'buildId' missing or has wrong format.\n").build();
        }

       // final Party otherParty = rpcOps.wellKnownPartyFromX500Name(developer);
       // if (otherParty == null) {
       //     return Response.status(BAD_REQUEST).entity("Party named " + developer + "cannot be found.\n").build();
       // }
        System.out.println("start try");
        try {
            //build.state="booked";
            FlowProgressHandle<SignedTransaction> flowHandle = rpcOps
                    .startTrackedFlowDynamic(
                            BookFlow.Initiator.class, buildId, "booked");

            System.out.println("create flow");
            flowHandle.getProgress().subscribe(evt -> System.out.printf(">> %s\n", evt));
            System.out.println("cheking flow");
            // The line below blocks and waits for the flow to return.
            final SignedTransaction result = flowHandle
                    .getReturnValue()
                    .get();
            System.out.println("get result of flow");
            final String msg = String.format("Transaction id %s committed to ledger.\n", result.getId());
            return Response.status(CREATED).entity(msg).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }
}