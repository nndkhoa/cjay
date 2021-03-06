package com.cloudjay.cjay.adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class IsoCodeSyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;

    public IsoCodeSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
//        Logger.i("onPerformSync for account[" + account.name + "]");
//        try {
//            String authToken = "Token " + mAccountManager.blockingGetAuthToken(account, AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS, true);
//
//            //Get iso codes from server
//            List<IsoCode> listRepairsCode = NetworkClient.getInstance().getRepairCodes(getContext(), authToken,account.name, null);
//            Logger.i("data server counts: " + listRepairsCode.size());
//
//            //Get iso codes from client
//            List<IsoCode> localListRepairsCode = new ArrayList<IsoCode>();
//            Cursor cursor = contentProviderClient.query(IsoCode.URI, null, null, null, null);
//
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    localListRepairsCode.add(IsoCode.isoCodeObject(cursor));
//                }
//                cursor.close();
//            }
//            Logger.i("data client counts: " + localListRepairsCode.size());
//
//            //create list missing codes from server to client
//            List<IsoCode> listsToAdd = new ArrayList<IsoCode>();
//            for (IsoCode isoCode : listRepairsCode) {
//                if (!localListRepairsCode.contains(isoCode)) {
//                    listsToAdd.add(isoCode);
//                } else {
//                    Logger.i("already existed");
//                }
//            }
//            Logger.i("list to add counts: " + listsToAdd.size());
//
//            //updating local iso codes
//            if (listsToAdd.size()==0) {
//                Logger.i("up to date");
//            } else {
//                int i = 0;
//                ContentValues addValues[] = new ContentValues[listsToAdd.size()];
//                for (IsoCode isoCode : listsToAdd) {
//                    addValues[i++] = isoCode.getContentValues();
//                }
//                contentProviderClient.bulkInsert(IsoCode.URI, addValues);
//            }
//
//        } catch (OperationCanceledException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (AuthenticatorException e) {
//            e.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
