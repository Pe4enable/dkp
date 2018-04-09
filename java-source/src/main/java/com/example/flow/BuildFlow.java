package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
//import com.example.contract.IOUContract;
import com.example.IBuildContract;
import com.example.IBuildState;
//import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
//import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.util.stream.Collectors;

import static com.example.IBuildContract.IBUILD_CONTRACT_ID;
//import static com.example.contract.IOUContract.IOU_CONTRACT_ID;
import static net.corda.core.contracts.ContractsDSL.requireThat;

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
public class BuildFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        //private final int iouValue;
        //private final Party otherParty;

        private final double price;
        private final String description;
        private final Party ahmlParty;
        private final Party clientParty;
        private final String address;
        private final String comment;
        private final double area;
        private final String status;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new Build.");
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

        //public Initiator(int iouValue, Party otherParty) {
        //    this.iouValue = iouValue;
        //    this.otherParty = otherParty;
        //}

        public Initiator(double price, Party ahmlParty, Party clientParty, String description,
                         String address,
                         String comment, double area, String status){//, String description, String address, double area) {
            this.price = price;
            this.ahmlParty = ahmlParty;
            this.clientParty=clientParty;
            this.description= description;
            this.address= address;
            this.comment=comment;
            this.area= area;
            this.status= status;
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
            // Obtain a reference to the notary we want to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

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
            IBuildState buildState = new IBuildState(
                    price,
                    ahmlParty,
                    getServiceHub().getMyInfo().getLegalIdentities().get(0),
                    clientParty,
                    description,
                    address,
                    area,
                    comment,
                    status
            );
            final Command<IBuildContract.Commands.Create> txCommand = new Command<>(
                    new IBuildContract.Commands.Create(),
                    buildState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList()));
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(buildState, IBUILD_CONTRACT_ID)
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
            // Send the state to the counterparty, and receive it back with their signature.
            //FlowSession otherPartySession = initiateFlow(otherParty);
            FlowSession otherPartySession = initiateFlow(ahmlParty);
            FlowSession clientPartySession = initiateFlow(clientParty);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession,clientPartySession), CollectSignaturesFlow.Companion.tracker()));

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
                        require.using("This must be an build transaction.", output instanceof IBuildState);
                        //IOUState iou = (IOUState) output;
                        IBuildState build = (IBuildState) output;
                        // require.using("I won't accept IOUs with a value over 100.", iou.getValue() <= 100);
                        return null;
                    });
                }
            }

            return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
        }
    }
}
