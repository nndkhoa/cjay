package com.cloudjay.cjay.api;


import android.content.Context;

import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

@EBean(scope = EBean.Scope.Singleton)
public class NetworkClient {

    @Bean
    RestAdapterProvider provider = new RestAdapterProvider();

    Context context;

    public NetworkClient(Context context) {
        this.context = context;
    }

    public String getToken(String username, String password) throws RetrofitError {

        JsonObject tokenJson;
        try {
            tokenJson = provider.getRestAdapter(context).create(NetworkService.class).getToken(username, password);
        } catch (RetrofitError e) {
            throw e;
        }

        String token = tokenJson.get("token").getAsString();
        return token;
    }

    public User getCurrentUser(Context context) {
        JsonObject result = provider.getRestAdapter(context).create(NetworkService.class).getCurrentUser();

        // WTF is this shit :| Fuck Realm
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        User user = realm.createObject(User.class);
        user.setId(result.get("id").getAsLong());
        user.setFirstName(result.get("first_name").getAsString());
        user.setLastName(result.get("last_name").getAsString());
        user.setUsername(result.get("username").getAsString());
        user.setEmail(result.get("email").getAsString());
        user.setFullName(result.get("full_name").getAsString());
        user.setRole(result.get("role").getAsLong());
        user.setRoleName(result.get("role_name").getAsString());
        user.setDepotCode(result.get("depot_code").getAsString());
        user.setAvatarUrl(result.get("avatar_url").getAsString());
        realm.commitTransaction();

        return user;
    }

    public List<IsoCode> getRepairCodes(Context context, String lastModifiedDate) {

        JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getRepairCodes
                (lastModifiedDate);

        List<IsoCode> items = new ArrayList<IsoCode>();

        // Clear the realm from last time
        Realm.deleteRealmFile(context);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        for (JsonElement e : results) {

            // Create a realm capable object
            IsoCode code = realm.createObject(IsoCode.class);
            code.setId(e.getAsJsonObject().get("id").getAsLong());
            code.setCode(e.getAsJsonObject().get("code").getAsString());
            code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
            items.add(code);
        }
        realm.commitTransaction();

        return items;
    }

    public List<IsoCode> getDamageCodes(Context context, String lastModifiedDate) {
        JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getDamageCodes
                (lastModifiedDate);

        List<IsoCode> items = new ArrayList<IsoCode>();

        // Clear the realm from last time
        Realm.deleteRealmFile(context);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        for (JsonElement e : results) {

            // Create a realm capable object
            IsoCode code = realm.createObject(IsoCode.class);
            code.setId(e.getAsJsonObject().get("id").getAsLong());
            code.setCode(e.getAsJsonObject().get("code").getAsString());
            code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
            items.add(code);
        }
        realm.commitTransaction();

        return items;
    }

    public List<IsoCode> getComponentCodes(Context context, String lastModifiedDate) {
        JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getComponentCodes(lastModifiedDate);

        List<IsoCode> items = new ArrayList<IsoCode>();

        // Clear the realm from last time
        Realm.deleteRealmFile(context);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        for (JsonElement e : results) {

            // Create a realm capable object
            IsoCode code = realm.createObject(IsoCode.class);
            code.setId(e.getAsJsonObject().get("id").getAsLong());
            code.setCode(e.getAsJsonObject().get("code").getAsString());
            code.setFullName(e.getAsJsonObject().get("full_name").getAsString());
            items.add(code);
        }
        realm.commitTransaction();

        return items;
    }

    public List<Operator> getOperators(Context context, String lastModifiedDate) {

        JsonArray results = provider.getRestAdapter(context).create(NetworkService.class).getOperators(lastModifiedDate);
        List<Operator> items = new ArrayList<Operator>();

        // Clear the realm from last time
        Realm.deleteRealmFile(context);

        // Store the retrieved items to the Realm
        Realm realm = Realm.getInstance(context);

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        for (JsonElement e : results) {

            // Create a realm capable object
            Operator code = realm.createObject(Operator.class);
            code.setId(e.getAsJsonObject().get("id").getAsLong());
            code.setOperatorCode(e.getAsJsonObject().get("operator_code").getAsString());
            code.setOperatorName(e.getAsJsonObject().get("operator_name").getAsString());
            items.add(code);
        }
        realm.commitTransaction();

        return items;
    }

    public List<Session> getContainerSessionsByPage(int page, String lastModifiedDate) {
        JsonObject jsonObject = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionsByPage(page, lastModifiedDate);
        JsonArray jsonArray = jsonObject.getAsJsonArray("results");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Session>>() {
        }.getType();
        List<Session> containerSessionsByPage = gson.fromJson(jsonArray.toString(), listType);
        return containerSessionsByPage;
    }

    public Session getContainerSessionById(int id) {
        Session containerSessionById = provider.getRestAdapter(context).create(NetworkService.class).getContainerSessionById(id);
        return containerSessionById;
    }

    public void getAllSession(Context context, String mToken, String fullName, Object o) {
    }

    public void uploadImage(Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.CJAY_TMP_STORAGE).build();
        File image = new File("storage/emulated/0/Pictures/Instagram/IMG_20140906_122413.jpg");
        TypedFile typedFile = new TypedFile("image/png", image);
        restAdapter.create(NetworkService.class).postImageFile("image/jpeg","media", "IMG_20140906_122413.jpg", typedFile, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

                Logger.e(response.getUrl());
                Logger.e(response.getHeaders().toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Logger.e(error.toString());
            }
        });
    }

    public void uploadContainerSession(Context context, Session containerSession){
        provider.getRestAdapter(context).create(NetworkService.class).postContainer("","");
    }
}
