import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class WalletE {
    public WalletE(){

        final String publicAddress = "0x574Ca674011B15854cA62449Dd8ca3BB68ED6674";
        String privateKey = "287c49337ad1c20b4a3c88d6b7293d13d16ee256010a690fdde53139a836677c";

        Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/fa403f0b3bd04027866a2f8c4eaca953"));
        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = web3
                    .ethGetBalance("0x574Ca674011B15854cA62449Dd8ca3BB68ED6674", DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger wei = ethGetBalance.getBalance();

        Credentials credentials = Credentials.create(privateKey);
        Snooze contract = Snooze.load("0x93F6B9c4EE1d48F61584e754eA65A5Cd70Ebc1C1", web3, credentials, new BigInteger("1000000000"), new BigInteger("21530"));

        try {
            contract.snooze(new BigInteger("1")).send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Ma\' nigga!");
    }
}
