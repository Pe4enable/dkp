package com.example;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NetworkParameters;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import net.corda.testing.node.User;

import java.util.Arrays;

import static net.corda.testing.driver.Driver.driver;

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to using deployNodes)
 * Do not use in a production environment.
 *
 * To debug your CorDapp:
 * 
 * 1. Firstly, run the "Run Template CorDapp" run configuration.
 * 2. Wait for all the nodes to start.
 * 3. Note the debug ports which should be output to the console for each node. They typically start at 5006, 5007,
 * 5008. The "Debug CorDapp" configuration runs with port 5007, which should be "NodeB". In any case, double check
 * the console output to be sure.
 * 4. Set your breakpoints in your CorDapp code.
 * 5. Run the "Debug CorDapp" remote debug run configuration.
 */
public class NodeDriver {
    public static void main(String[] args) {
        // No permissions required as we are not invoking flows.
        final User user = new User("user1", "test", ImmutableSet.of("ALL"));

        driver(
                new DriverParameters().
                       withIsDebug(true).
                       withWaitForAllNodesToFinish(true).
                       withExtraCordappPackagesToScan(
                               Arrays.asList(
                                       "com.example")
                       )
                ,
                dsl -> {
                   // CordaFuture<NodeHandle> partyAFuture = dsl.startNode(new NodeParameters()
                   //         .withProvidedName(new CordaX500Name("PartyA", "London", "GB"))
                   //         .withRpcUsers(ImmutableList.of(user)));
                   // CordaFuture<NodeHandle> partyBFuture = dsl.startNode(new NodeParameters()
                   //         .withProvidedName(new CordaX500Name("PartyB", "New York", "US"))
                   //         .withRpcUsers(ImmutableList.of(user)));
                   // CordaFuture<NodeHandle> partyCFuture = dsl.startNode(new NodeParameters()
                   //         .withProvidedName(new CordaX500Name("PartyC", "Paris", "FR"))
                   //         .withRpcUsers(ImmutableList.of(user)));
            CordaFuture<NodeHandle> partyAFuture = dsl.startNode(new NodeParameters()
                    .withProvidedName(new CordaX500Name("AHML", "London", "GB"))
                    .withRpcUsers(ImmutableList.of(user)))
                    ;
            CordaFuture<NodeHandle> partyBFuture = dsl.startNode(new NodeParameters()
                    .withProvidedName(new CordaX500Name("AHMLClient", "New York", "US"))
                    .withRpcUsers(ImmutableList.of(user)));
            CordaFuture<NodeHandle> partyCFuture = dsl.startNode(new NodeParameters()
                    .withProvidedName(new CordaX500Name("BANK", "Paris", "FR"))
                    .withRpcUsers(ImmutableList.of(user)));
            CordaFuture<NodeHandle> partyDFuture = dsl.startNode(new NodeParameters()
                    .withProvidedName(new CordaX500Name("DEVELOPER", "Paris", "FR"))
                    .withRpcUsers(ImmutableList.of(user)));
                    try {
                        dsl.startWebserver(partyAFuture.get());
                        dsl.startWebserver(partyBFuture.get());
                        dsl.startWebserver(partyCFuture.get());
                        dsl.startWebserver(partyDFuture.get());
                    } catch (Throwable e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                    }

                    return null;
                }
        );
    }
}