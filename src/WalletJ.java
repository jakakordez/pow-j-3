import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.net.InetAddress;

public class WalletJ {
    final NetworkParameters params;
    public WalletJ() throws Exception{
        BriefLogFormatter.init();

        // Connect to testnet and find a peer
        System.out.println("Connecting to node");
        params = RegTestParams.get();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        PeerAddress addr = new PeerAddress(params, InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, (byte)31, (byte)243}));
        peerGroup.addAddress(addr);
        peerGroup.start();
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);
        peer.addOnTransactionBroadcastListener(new OnTransactionBroadcastListener() {
            public void onTransaction(Peer peer, Transaction transaction) {
                System.out.println("Transaction received");
            }
        });
        System.out.println("Listening");
        // Retrieve a block through a peer
        /*Sha256Hash blockHash = Sha256Hash.wrap(args[1]);
            Future<Block> future = peer.getBlock(blockHash);
            System.out.println("Waiting for node to send us the requested block: " + blockHash);
            Block block = future.get();
            System.out.println(block);
            peerGroup.stopAsync();*/

        final WalletAppKit kit = new WalletAppKit(params, new File("."), "") {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());
            }
        };

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        // Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            public void onCoinsReceived(org.bitcoinj.wallet.Wallet w, final Transaction tx, Coin prevBalance, Coin newBalance) {
                // Runs in the dedicated "user thread".
                //
                // The transaction "tx" can either be pending, or included into a block (we didn't see the broadcast).
                Coin value = tx.getValueSentToMe(w);
                System.out.println("Received tx for " + value.toFriendlyString() + ": " + tx);
                System.out.println("Transaction will be forwarded after it confirms.");
                // Wait until it's made it into the block chain (may run immediately if it's already there).
                //
                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
                // to be double spent, no harm done. WalletJ.allowSpendingUnconfirmedTransactions() would have to
                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
                // case of waiting for a block.
                Futures.addCallback(tx.getConfidence().getDepthFuture(6), new FutureCallback<TransactionConfidence>() {
                    public void onSuccess(TransactionConfidence result) {
                        // "result" here is the same as "tx" above, but we use it anyway for clarity.
                        try {
                            System.out.println("Transactions confirmed in new block.");
                            forwardCoins(result, kit, tx);
                            System.out.println("Wallet success: " + kit.wallet().getBalance());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    public void onFailure(Throwable t) {}
                });
            }
        });

        if (kit.wallet() != null) {
            Wallet wallet = kit.wallet();
            System.out.println("Kit Wallet Balance: " + wallet.getBalance());
            System.out.println("Kit Wallet Address: " + wallet.currentReceiveAddress());

        }
        while(true);
    }

    void forwardCoins(TransactionConfidence tx, WalletAppKit kit, Transaction transaction) {
        Transaction overridingTransaction;
        System.out.println("Transcation type: " + tx.getConfidenceType().toString());

        Coin value = transaction.getValueSentToMe(kit.wallet());
        System.out.println("Forwarding " + value.toFriendlyString() + " BTC");

        System.out.println("Wallet balance " + kit.wallet().getBalance());

// Now send the coins back! Send with a small fee attached to ensure rapid confirmation.
        final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
        try {
            Address address = new Address(params, "mgpr1udbnBLXW7FUntEvDQ4CgLvkP57jya");
            final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), address, amountToSend);
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        }
// Register a callback that is invoked when the transaction has propagated across the network.
// This shows a second style of registering ListenableFuture callbacks, it works when you don't
// need access to the object the future returns.
        /*sendResult.broadcastComplete.addListener(new Runnable() {
            public void run() {
                // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
            }
        });*/
    }
}
