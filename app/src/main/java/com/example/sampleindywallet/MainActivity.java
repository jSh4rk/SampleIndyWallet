package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.hyperledger.indy.sdk.did.DidResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SampleWalletTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        try {
            /*
            1. we have to setup the environment
             */
            String applicationEnvironmentPath = getExternalFilesDir(null).getAbsolutePath();
            IndyFacade indyFacade = IndyFacade.createInstance(applicationEnvironmentPath, IndyFacade.Platform.ANDROID, IndyFacade.DEFAULT_POOL_PROTOCOL_VERSION);
            Log.d(TAG, "onCreate: environment set successfully");
            
            /*
            2. we have to create the genesis file. we delete it if it exists
             */
            indyFacade.deleteGenesisTransactions(IndyFacade.DEFAULT_GENESIS_FILE); // delete genesis file if exists
            String ip = "192.168.178.27";
            indyFacade.writeDefaultGenesisTransactions(ip); // create genesis file
            Log.d(TAG, "onCreate: genesis file created successfully");

            /*
             * 3. we have to create a pool. we delete a pool-config if it exists
             */
            indyFacade.deletePool(IndyFacade.DEFAULT_POOL_NAME); // delete pool if exists
            indyFacade.createDefaultPool(); // create pool
            Log.d(TAG, "onCreate: pool created successfully");


            /*
            4. we need to create a wallet. I associate a wallet with a user. we delete a wallet
            if it exists
             */
            String userName = "test"; 
            String userPassword = "123";
            indyFacade.deleteWallet(userName); // delete wallet if it exists
            indyFacade.createWallet(userName, userPassword); // create wallet
            indyFacade.openWallet(userName, userPassword); // open wallet
            Log.d(TAG, "onCreate: wallet opened successfully");
            
            
            /*
            5. now we populate the opened wallet with the steward did
             */
            String seed = "000000000000000000000000Steward1";
            DidResults.CreateAndStoreMyDidResult result = indyFacade.createDID(seed);
            String stewardDID = result.getDid();
            Log.d(TAG, "onCreate: Steward-DID is " + stewardDID);


            /*
            6. now we query the steward did from the ledger. we have to open the created pool first
             */
            indyFacade.openDefaultPool();
            Log.d(TAG, "onCreate: pool opened successfully");
            String verKey = indyFacade.readVerKeyForDidFromLedger(stewardDID);
            Log.d(TAG, "onCreate: read verKey from ledger successfully " + verKey);
            indyFacade.closePool();
            Log.d(TAG, "onCreate: pool closed successfully");


            /*
            7. close wallet
             */
            indyFacade.closeWallet();
            Log.d(TAG, "onCreate: wallet closed successfully");

        } catch (IndyFacade.IndyFacadeException e) {
            Log.e(TAG, "onCreate: There went something wrong with indy", e);
        }

    }
}
