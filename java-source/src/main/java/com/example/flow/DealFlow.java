package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.IBuildState;
import com.example.IDealContract;
import com.example.IDealState;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;


import static com.example.IDealContract.IDEAL_CONTRACT_ID;
import static net.corda.core.contracts.ContractsDSL.requireThat;

//import com.example.contract.IOUContract;
//import com.google.common.collect.ImmutableList;
//import net.corda.core.contracts.UniqueIdentifier;
//import static com.example.contract.IOUContract.IOU_CONTRACT_ID;

/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the IOU encapsulated
 * within an [IOUState].
 *
 * In our simple example, the [Acceptor] always accepts a valid IOU.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
public class DealFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        //private final int iouValue;
        //private final Party clientParty;
      //  private final Party bankParty;
          private final Party ahmlParty;

        //private final double price;
        //private final String description;
          private final Party devParty;
        //private final String address;
        //private final String comment;
        //private final double area;
        private final String status;
        static private final Logger logger = LoggerFactory.getLogger(DealFlow.class);
        private final String buildId;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new deal.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step GATHERING_SIGS = new Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
        // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
        // function.
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public Initiator(String buildId,Party ahmlParty, Party devParty,
                          String status){
            this.buildId=buildId;
           // this.developerParty=developerParty;
            this.ahmlParty=ahmlParty;
            this.devParty=devParty;
            this.status=status;
            System.out.println("0");
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("start signed tx");
            // Obtain a reference to the notary we want to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            System.out.println("1");
            // Stage 1.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // Generate an unsigned transaction.
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            //IOUState iouState = new IOUState(iouValue, me, otherParty, new UniqueIdentifier());
            //final Command<IOUContract.Commands.Create> txCommand = new Command<>(
            //        new IOUContract.Commands.Create(),
            //        ImmutableList.of(iouState.getLender().getOwningKey(), iouState.getBorrower().getOwningKey()));
            //final TransactionBuilder txBuilder = new TransactionBuilder(notary)
            //        .addOutputState(iouState, IOU_CONTRACT_ID)
            //        .addCommand(txCommand);


           // System.out.println("2");
           //     Vault.Page<IBuildState> results = getServiceHub().getVaultService().queryBy(IBuildState.class);
//
           //     StateAndRef<IBuildState> oldState=results.getStates().stream()
           //         .filter(build -> build.getState().getData().getBuildUID().getId().equals(UUID.fromString(buildId)))
           //         .findFirst()
           //         .get();
           //     IBuildState build = oldState.getState().getData();
            System.out.println("3");
            //UniqueIdentifier id=UUID.fromString(buildId);
                IDealState dealState = new IDealState(
                        me,
                       // build.getDeveloper(),
                        devParty,
                        ahmlParty,
                        buildId,
                        //build.getBuildUID(),

                        status
                );
            System.out.println("4");
                final Command<IDealContract.Commands.Create> txCommand = new Command<>(
                        new IDealContract.Commands.Create(),
                        dealState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList()));
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        //.addInputState(oldState)
                        .addOutputState(dealState, IDEAL_CONTRACT_ID)
                        .addCommand(txCommand);

                // Stage 2.
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                // Verify that the transaction is valid.
                txBuilder.verify(getServiceHub());

                // Stage 3.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                // Sign the transaction.
                final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

                // Stage 4.
                progressTracker.setCurrentStep(GATHERING_SIGS);
            System.out.println("5");
                // Send the state to the counterparty, and receive it back with their signature.
                //FlowSession otherPartySession = initiateFlow(otherParty);
                FlowSession developerPartySession = initiateFlow(devParty);
                FlowSession ahmlPartySession = initiateFlow(ahmlParty);
                final SignedTransaction fullySignedTx = subFlow(
                        new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(developerPartySession,ahmlPartySession), CollectSignaturesFlow.Companion.tracker()));


            System.out.println("6");
                // Stage 5.
                progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                // Notarise and record the transaction in both parties' vaults.
                return subFlow(new FinalityFlow(fullySignedTx));
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartyFlow;

        public Acceptor(FlowSession otherPartyFlow) {
            this.otherPartyFlow = otherPartyFlow;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an deal transaction.", output instanceof IDealState);
                        //IOUState iou = (IOUState) output;
                        IDealState deal = (IDealState) output;
                        // require.using("I won't accept IOUs with a value over 100.", iou.getValue() <= 100);
                        return null;
                    });
                }
            }

            return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
        }
    }
}