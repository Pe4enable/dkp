package com.example;

import com.example.flow.BuildFlow;
import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
//import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class IBuildFlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;

    @Before
    public void setup() {
        network = new MockNetwork(
                Arrays.asList("com.example")
        );

        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            node.registerInitiatedFlow(BuildFlow.Acceptor.class);

        }
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void flowRejectsInvalidIOUs() throws Exception {
        // The IOUContract specifies that IOUs cannot have negative values.
       // BuildFlow.Initiator flow = new BuildFlow.Initiator(-1, b.getInfo().getLegalIdentities().get(0),"", "","",0,"");
       // CordaFuture<SignedTransaction> future = a.startFlow(flow);
       // network.runNetwork();
//
       // // The IOUContract specifies that IOUs cannot have negative values.
       // exception.expectCause(instanceOf(TransactionVerificationException.class));
       // future.get();
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheInitiator() throws Exception {
       //BuildFlow.Initiator flow = new BuildFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0),"", "","",0,"");
       //CordaFuture<SignedTransaction> future = a.startFlow(flow);
       //network.runNetwork();

       //SignedTransaction signedTx = future.get();
       //signedTx.verifySignaturesExcept(b.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheAcceptor() throws Exception {
       // BuildFlow.Initiator flow = new BuildFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0),"", "","",0,"");
       // CordaFuture<SignedTransaction> future = a.startFlow(flow);
       // network.runNetwork();
//
       // SignedTransaction signedTx = future.get();
       // signedTx.verifySignaturesExcept(a.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void flowRecordsATransactionInBothPartiesTransactionStorages() throws Exception {
      // BuildFlow.Initiator flow = new BuildFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0),"", "","",0,"");
      // CordaFuture<SignedTransaction> future = a.startFlow(flow);
      // network.runNetwork();
      // SignedTransaction signedTx = future.get();

      // // We check the recorded transaction in both vaults.
      // for (StartedMockNode node : ImmutableList.of(a, b)) {
      //     assertEquals(signedTx, node.getServices().getValidatedTransactions().getTransaction(signedTx.getId()));
        }
   // }

   // @Test
   // public void recordedTransactionHasNoInputsAndASingleOutputTheInputIOU() throws Exception {
   //     Integer iouValue = 1;
   //     ExampleFlow.Initiator flow = new ExampleFlow.Initiator(iouValue, b.getInfo().getLegalIdentities().get(0),"");
   //     CordaFuture<SignedTransaction> future = a.startFlow(flow);
   //     network.runNetwork();
   //     SignedTransaction signedTx = future.get();
//
   //     // We check the recorded transaction in both vaults.
   //     for (StartedMockNode node : ImmutableList.of(a, b)) {
   //         SignedTransaction recordedTx = node.getServices().getValidatedTransactions().getTransaction(signedTx.getId());
   //         List<TransactionState<ContractState>> txOutputs = recordedTx.getTx().getOutputs();
   //         assert (txOutputs.size() == 1);
//
   //         IOUState recordedState = (IOUState) txOutputs.get(0).getData();
   //         assertEquals(recordedState.getValue(), iouValue);
   //         assertEquals(recordedState.getLender(), a.getInfo().getLegalIdentities().get(0));
   //         assertEquals(recordedState.getBorrower(), b.getInfo().getLegalIdentities().get(0));
   //     }
   // }

   // @Test
   // public void flowRecordsTheCorrectIOUInBothPartiesVaults() throws Exception {
   //     Integer iouValue = 1;
   //     ExampleFlow.Initiator flow = new ExampleFlow.Initiator(1, b.getInfo().getLegalIdentities().get(0),"");
   //     CordaFuture<SignedTransaction> future = a.startFlow(flow);
   //     network.runNetwork();
   //     future.get();
//
   //     // We check the recorded IOU in both vaults.
   //     for (StartedMockNode node : ImmutableList.of(a, b)) {
   //         node.transaction(() -> {
   //             List<StateAndRef<IOUState>> ious = node.getServices().getVaultService().queryBy(IOUState.class).getStates();
   //             assertEquals(1, ious.size());
   //             IOUState recordedState = ious.get(0).getState().getData();
   //             assertEquals(recordedState.getValue(), iouValue);
   //             assertEquals(recordedState.getLender(), a.getInfo().getLegalIdentities().get(0));
   //             assertEquals(recordedState.getBorrower(), b.getInfo().getLegalIdentities().get(0));
   //             return null;
   //         });
   //     }
   // }
}