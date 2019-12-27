package com.example.sampleindywallet;

import android.system.ErrnoException;
import android.system.Os;

import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;
import org.hyperledger.indy.sdk.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.concurrent.ExecutionException;


public class IndyFacade {

    public static class IndyFacadeException extends Exception {


        public IndyFacadeException() {
            super();
        }


        public IndyFacadeException(String msg) {
            super(msg);
        }


        public IndyFacadeException(Throwable cause) {
            super(cause);
        }


        public IndyFacadeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }


    public enum Platform {
        ANDROID("android"),
        WINDOWS("windows");
        private String name;
        Platform(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    private static IndyFacade singleton;

    public static final String DEFAULT_POOL_NAME = "myIndyPool";
    public static final String DEFAULT_POOL_DIRECTORY = "pool";
    public static final String DEFAULT_WALLET_DIRECTORY = "wallet";
    public static final String DEFAULT_GENESIS_FILE = "genesis.txn";
    public static final int DEFAULT_POOL_PROTOCOL_VERSION = 2;
    private static final String DEFAULT_INDY_CLIENT_DIRECTORY = ".indy_client";
    private final String environmentPath;
    private final String indyClientPath;

    private Wallet wallet;
    private String walletName;


    private Pool pool;


    private IndyFacade(String environmentPath) {
        this.environmentPath = environmentPath;
        indyClientPath = environmentPath + "/" + DEFAULT_INDY_CLIENT_DIRECTORY;
    }


    public static IndyFacade createInstance(String environmentPath, Platform platform, int protocolVersion) throws IndyFacadeException {
        if (singleton == null) {
            singleton = new IndyFacade(environmentPath);
            if (platform == Platform.WINDOWS) {
                singleton.setEnvironmentAndLoadLibindyWindows();
            } else {
                singleton.setEnvironmentAndLoadLibindyAndroid();
            }
            singleton.setProtocolVersion(protocolVersion);
        }
        return singleton;

    }


    private void setEnvironmentAndLoadLibindyWindows() {
        File file = new File(indyClientPath);
        if (!file.exists()) {
            file.mkdir();
        }
        System.loadLibrary("indy");

    }


    private void setEnvironmentAndLoadLibindyAndroid() throws IndyFacadeException {

        try {
            Os.setenv("EXTERNAL_STORAGE", environmentPath, true);
            File file = new
                    File(indyClientPath);
            if (!file.exists()) {
                file.mkdir();
            }
            System.loadLibrary("indy");
        } catch (ErrnoException e) {
            throw new IndyFacadeException("could not set android environment", e);
        }

    }


    public void writeDefaultGenesisTransactions(String poolIPAdress) throws IndyFacadeException {
        String[] genesisTransactions = getDefaultGenesisTxn(poolIPAdress);
        writeGenesisTransactions(genesisTransactions, DEFAULT_GENESIS_FILE);
    }



    public void writeGenesisTransactions(String[] genesisContent, String genesisFileName) throws IndyFacadeException {
        try {
            File genesisFile = new File(indyClientPath + "/" + genesisFileName);
            FileWriter fw = new FileWriter(genesisFile);
            for (String s : genesisContent) {
                fw.write(s);
                fw.write("\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new IndyFacadeException("could not write default genesis transactions", e);
        }
    }


    public void deleteGenesisTransactions(String genesisFileName) throws IndyFacadeException {
        File genesisFile = new File(indyClientPath + "/" + genesisFileName);
        if(genesisFile.exists()) {
            if (!genesisFile.delete()) {
                throw new IndyFacadeException("could not delete Genesis-File");
            }
        }
    }









    public boolean isPoolCreated(String poolName) {
        File file = new File(indyClientPath + "/" + DEFAULT_POOL_DIRECTORY + "/" + poolName);
        return file.exists();
    }





    private String[] getDefaultGenesisTxn(String poolIPAddress) {
        String[] s = new String[]{String.format(
                "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node1\",\"blskey\":\"4N8aUNHSgjQVgkpm8nhNEfDf6txHznoYREg9kirmJrkivgL4oSEimFF6nsQ6M41QvhM2Z33nves5vfSn9n1UwNFJBYtWVnHYMATn76vLuL3zU88KyeAYcHfsih3He6UHcXDxcaecHVz6jhCYz1P2UZn2bDVruL5wXpehgBfBaLKm3Ba\",\"blskey_pop\":\"RahHYiCvoNCtPTrVtP7nMC5eTYrsUA8WjXbdhNc8debh1agE9bGiJxWBXYNFbnJXoXhWFMvyqhqhRoq737YQemH5ik9oL7R4NTTCz2LEZhkgLJzB3QRQqJyBNyv7acbdHrAT8nQ9UkLbaVL9NBpnWXBTw4LEMePaSHEw66RzPNdAX1\",\"client_ip\":\"%s\",\"client_port\":9702,\"node_ip\":\"%s\",\"node_port\":9701,\"services\":[\"VALIDATOR\"]},\"dest\":\"Gw6pDLhcBcoQesN72qfotTgFa7cbuqZpkX3Xo6pLhPhv\"},\"metadata\":{\"from\":\"Th7MpTaRZVRYnPiabds81Y\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":1,\"txnId\":\"fea82e10e894419fe2bea7d96296a6d46f50f93f9eeda954ec461b2ed2950b62\"},\"ver\":\"1\"}",
                poolIPAddress, poolIPAddress),
                String.format(
                        "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node2\",\"blskey\":\"37rAPpXVoxzKhz7d9gkUe52XuXryuLXoM6P6LbWDB7LSbG62Lsb33sfG7zqS8TK1MXwuCHj1FKNzVpsnafmqLG1vXN88rt38mNFs9TENzm4QHdBzsvCuoBnPH7rpYYDo9DZNJePaDvRvqJKByCabubJz3XXKbEeshzpz4Ma5QYpJqjk\",\"blskey_pop\":\"Qr658mWZ2YC8JXGXwMDQTzuZCWF7NK9EwxphGmcBvCh6ybUuLxbG65nsX4JvD4SPNtkJ2w9ug1yLTj6fgmuDg41TgECXjLCij3RMsV8CwewBVgVN67wsA45DFWvqvLtu4rjNnE9JbdFTc1Z4WCPA3Xan44K1HoHAq9EVeaRYs8zoF5\",\"client_ip\":\"%s\",\"client_port\":9704,\"node_ip\":\"%s\",\"node_port\":9703,\"services\":[\"VALIDATOR\"]},\"dest\":\"8ECVSk179mjsjKRLWiQtssMLgp6EPhWXtaYyStWPSGAb\"},\"metadata\":{\"from\":\"EbP4aYNeTHL6q385GuVpRV\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":2,\"txnId\":\"1ac8aece2a18ced660fef8694b61aac3af08ba875ce3026a160acbc3a3af35fc\"},\"ver\":\"1\"}\n",
                        poolIPAddress, poolIPAddress),
                String.format(
                        "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node3\",\"blskey\":\"3WFpdbg7C5cnLYZwFZevJqhubkFALBfCBBok15GdrKMUhUjGsk3jV6QKj6MZgEubF7oqCafxNdkm7eswgA4sdKTRc82tLGzZBd6vNqU8dupzup6uYUf32KTHTPQbuUM8Yk4QFXjEf2Usu2TJcNkdgpyeUSX42u5LqdDDpNSWUK5deC5\",\"blskey_pop\":\"QwDeb2CkNSx6r8QC8vGQK3GRv7Yndn84TGNijX8YXHPiagXajyfTjoR87rXUu4G4QLk2cF8NNyqWiYMus1623dELWwx57rLCFqGh7N4ZRbGDRP4fnVcaKg1BcUxQ866Ven4gw8y4N56S5HzxXNBZtLYmhGHvDtk6PFkFwCvxYrNYjh\",\"client_ip\":\"%s\",\"client_port\":9706,\"node_ip\":\"%s\",\"node_port\":9705,\"services\":[\"VALIDATOR\"]},\"dest\":\"DKVxG2fXXTU8yT5N7hGEbXB3dfdAnYv1JczDUHpmDxya\"},\"metadata\":{\"from\":\"4cU41vWW82ArfxJxHkzXPG\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":3,\"txnId\":\"7e9f355dffa78ed24668f0e0e369fd8c224076571c51e2ea8be5f26479edebe4\"},\"ver\":\"1\"}\n",
                        poolIPAddress, poolIPAddress),
                String.format(
                        "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node4\",\"blskey\":\"2zN3bHM1m4rLz54MJHYSwvqzPchYp8jkHswveCLAEJVcX6Mm1wHQD1SkPYMzUDTZvWvhuE6VNAkK3KxVeEmsanSmvjVkReDeBEMxeDaayjcZjFGPydyey1qxBHmTvAnBKoPydvuTAqx5f7YNNRAdeLmUi99gERUU7TD8KfAa6MpQ9bw\",\"blskey_pop\":\"RPLagxaR5xdimFzwmzYnz4ZhWtYQEj8iR5ZU53T2gitPCyCHQneUn2Huc4oeLd2B2HzkGnjAff4hWTJT6C7qHYB1Mv2wU5iHHGFWkhnTX9WsEAbunJCV2qcaXScKj4tTfvdDKfLiVuU2av6hbsMztirRze7LvYBkRHV3tGwyCptsrP\",\"client_ip\":\"%s\",\"client_port\":9708,\"node_ip\":\"%s\",\"node_port\":9707,\"services\":[\"VALIDATOR\"]},\"dest\":\"4PS3EDQ3dW1tci1Bp6543CfuuebjFrg36kLAUcskGfaA\"},\"metadata\":{\"from\":\"TWwCRQRZ2ZHMJFn9TzLp7W\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":4,\"txnId\":\"aa5e817d7cc626170eca175822029339a444eb0ee8f0bd20d3b0b76e566fb008\"},\"ver\":\"1\"}",
                        poolIPAddress, poolIPAddress)};
        return s;
    }


    private void setProtocolVersion(int protocolVersion) throws IndyFacadeException {
        try {
            Pool.setProtocolVersion(protocolVersion).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not protocol version", e);
        }

    }


    public void createWallet(String walletName, String walletPassword) throws IndyFacadeException {
        try {
            JSONObject walletConfig = new JSONObject();
            JSONObject walletCred = new JSONObject();
            walletConfig.put("id", walletName);
            walletCred.put("key", walletPassword);
            Wallet.createWallet(walletConfig.toString(), walletCred.toString()).get();
        } catch (InterruptedException | ExecutionException | IndyException | JSONException e) {
            throw new IndyFacadeException("could not create Wallet", e);
        }
    }





    public void createPool(String poolName, String genesisTransactionsFileName) throws IndyFacadeException {
        try {
            PoolJSONParameters.CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new PoolJSONParameters.CreatePoolLedgerConfigJSONParameter(
                    indyClientPath + "/" + genesisTransactionsFileName);
            Pool.createPoolLedgerConfig(poolName, createPoolLedgerConfigJSONParameter.toJson()).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not create pool", e);
        }
    }


    public void deletePool(String poolName) throws IndyFacadeException {
        File pool = new File(indyClientPath + "/" + DEFAULT_POOL_DIRECTORY + "/" + poolName);
        if(pool.exists()) {
            for (File f : pool.listFiles()) {
                if (!f.delete()) {
                    throw new IndyFacadeException("could not delete Pool");
                }
            }
            try {
                FileUtils.forceDelete(pool);
            } catch (IOException e) {
                throw new IndyFacadeException("could not delete pool", e);
            }
        }
    }


    public void createDefaultPool() throws IndyFacadeException {
        createPool(IndyFacade.DEFAULT_POOL_NAME, DEFAULT_GENESIS_FILE);

    }


    public void openPool(String poolName, String poolConfig) throws IndyFacadeException {
        try {
            pool = Pool.openPoolLedger(poolName, poolConfig).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not open pool", e);
        }
    }









    public void openDefaultPool() throws IndyFacadeException {
        openPool(IndyFacade.DEFAULT_POOL_NAME, null);
    }


    public void closePool() throws IndyFacadeException {
        try {
            pool.closePoolLedger().get();
            pool = null;
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not close pool", e);
        }
    }




    public void openWallet(String walletName, String walletPassword) throws IndyFacadeException {
        try {
            JSONObject walletConfig = new JSONObject();
            JSONObject walletCred = new JSONObject();
            walletConfig.put("id", walletName);
            walletCred.put("key", walletPassword);
            this.walletName = walletName;
            wallet = Wallet.openWallet(walletConfig.toString(), walletCred.toString()).get();
        } catch (InterruptedException | ExecutionException | IndyException | JSONException e) {
            throw new IndyFacadeException("could not open wallet", e);
        }

    }



    public void closeWallet() throws IndyFacadeException {

        try {
            wallet.closeWallet().get();
            wallet = null;
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not close wallet", e);
        }

    }




    public void deleteWallet(String walletName) throws IndyFacadeException {
        try {
            File f = new File(indyClientPath + "/" + DEFAULT_WALLET_DIRECTORY + "/" + walletName);
            if (f.exists()) {
                FileUtils.deleteDirectory(f);
            }
        } catch (IOException e) {
            throw new IndyFacadeException("could not delete wallet", e);
        }
    }




    public CreateAndStoreMyDidResult createDID(String seed) throws IndyFacadeException {

        try {
            CreateAndStoreMyDidJSONParameter stewardDIDParameter = new CreateAndStoreMyDidJSONParameter(null, seed,
                    null, null);
            return Did.createAndStoreMyDid(wallet, stewardDIDParameter.toString()).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not create did", e);
        }
    }


    public String readVerKeyForDidFromLedger(String did) throws IndyFacadeException {
        try {
            String key = Did.keyForDid(pool, wallet, did).get();
            return key;
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new IndyFacadeException("could not read verkey for did from ledger", e);
        }
    }


















}
