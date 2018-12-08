import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.MonetaryFormat;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;

/**
 * <p>Downloads the block given a block hash from the remote or localhost node and prints it out.</p>
 * <p>When downloading from localhost, run bitcoind locally: bitcoind -testnet -daemon.
 * After bitcoind is up and running, use command: org.bitcoinj.examples.FetchBlock --localhost &lt;blockHash&gt; </p>
 * <p>Otherwise, use command: org.bitcoinj.examples.FetchBlock &lt;blockHash&gt;, this command will download blocks from a peer generated by DNS seeds.</p>
 */

public class Program {
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();

        // Connect to testnet and find a peer
        System.out.println("Connecting to node");
        final NetworkParameters params = RegTestParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        PeerAddress addr = new PeerAddress(params, InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, (byte)31, (byte)243}));
        peerGroup.addAddress(addr);
        peerGroup.start();
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        // Retrieve a block through a peer
        Sha256Hash blockHash = Sha256Hash.wrap(args[1]);
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        peerGroup.stopAsync();
    }
}