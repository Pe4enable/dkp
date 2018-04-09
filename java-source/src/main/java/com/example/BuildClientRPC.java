package com.example;

//import com.example.state.IOUState;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCClientConfiguration;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.DataFeed;
import net.corda.core.node.services.Vault;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.ExecutionException;

/**
 * Demonstration of using the CordaRPCClient to connect to a Corda Node and
 * steam some State data from the node.
 */
public class BuildClientRPC {
    private static final Logger logger = LoggerFactory.getLogger(BuildClientRPC.class);

    private static void logState(StateAndRef<IBuildState> state) {
        logger.info("{}", state.getState().getData());
    }

    private static void logDealState(StateAndRef<IDealState> state) {
        logger.info("{}", state.getState().getData());
    }

    public static void main(String[] args) throws ActiveMQException, InterruptedException, ExecutionException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: ExampleClientRPC <node address>");
        }

        final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse(args[0]);
        final CordaRPCClient client = new CordaRPCClient(nodeAddress, CordaRPCClientConfiguration.DEFAULT);

        // Can be amended in the com.example.Main file.
        final CordaRPCOps proxy = client.start("user1", "test").getProxy();

        // Grab all existing and future IOU states in the vault.
        final DataFeed<Vault.Page<IBuildState>, Vault.Update<IBuildState>> dataFeed = proxy.vaultTrack(IBuildState.class);
        final Vault.Page<IBuildState> snapshot = dataFeed.getSnapshot();
        final Observable<Vault.Update<IBuildState>> updates = dataFeed.getUpdates();

        final DataFeed<Vault.Page<IDealState>, Vault.Update<IDealState>> dataDealFeed = proxy.vaultTrack(IDealState.class);
        final Vault.Page<IDealState> snapDealshot = dataDealFeed.getSnapshot();
        final Observable<Vault.Update<IDealState>> updatesDeal = dataDealFeed.getUpdates();

        // Log the 'placed' IOUs and listen for new ones.
        snapshot.getStates().forEach(BuildClientRPC::logState);
        updates.toBlocking().subscribe(update -> update.getProduced().forEach(BuildClientRPC::logState));

        snapDealshot.getStates().forEach(BuildClientRPC::logDealState);
        updatesDeal.toBlocking().subscribe(update -> update.getProduced().forEach(BuildClientRPC::logDealState));
    }
}
