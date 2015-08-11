package com.ringcentral.rcandroidsdk.rcsdk.platform;

import com.pubnub.api.PubnubError;
import com.ringcentral.rcandroidsdk.rcsdk.http.RCResponse;
import com.ringcentral.rcandroidsdk.rcsdk.subscription.Subscription;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by andrew.pang on 8/10/15.
 */
public class Helpers extends Platform {
    /**
     * @param appKey
     * @param appSecret
     * @param server    Pass in either "SANDBOX" or "PRODUCTION"
     */
    public Helpers(String appKey, String appSecret, String server) {
        super(appKey, appSecret, server);
    }

    /**
     * GET Account Info API call
     *
     * @param c
     */
    public void accountInfo(Callback c){
        try {
            this.isAuthorized();
            HashMap<String, String> headers = new HashMap<>();
            headers.put("url", "/restapi/v1.0/account/~");
            this.get(headers, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GET Call Log API call
     *
     * @param c
     */
    public void callLog(Callback c){
        try{
           this.isAuthorized();
        HashMap<String, String> callLogHeaders = new HashMap<>();
        callLogHeaders.put("url", "/restapi/v1.0/account/~/call-log");
        this.get(callLogHeaders, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GET Message Store API call
     *
     * @param c
     */
    public void messageStore(Callback c){
        try{
            this.isAuthorized();
        HashMap<String, String> messageStoreHeaders = new HashMap<>();
        messageStoreHeaders.put("url", "/restapi/v1.0/account/~/extension/~/message-store");
        this.get(messageStoreHeaders, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RingOut API call using POST request
     * @param to Phone number calling to
     * @param from Phone number calling from
     * @param callerId Phone number used for caller ID
     * @param hasPrompt "True" or "False" states whether a prompt plays before call
     * @param c
     */
    public void ringOut(String to, String from, String callerId, String hasPrompt, Callback c){
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
//        body.put("body", "{\n" +
//                "  \"to\": {\"phoneNumber\": \"" + to
//                + "\"},\n" +
//                "  \"from\": {\"phoneNumber\": \"" + from
//                + "\"},\n" +
//                "  \"callerId\": {\"phoneNumber\": \"" + callerId
//                + "\"},\n" +
//                "  \"playPrompt\": " + hasPrompt
//                + "\n" + "}");
        body.put("\"to\"", "{\"phoneNumber\": \"" + to + "\"}");
        body.put("\"from\"", "{\"phoneNumber\": \"" + from + "\"}");
        body.put("\"callerId\"", "{\"phoneNumber\": \"" + callerId + "\"}");
        body.put("\"playPrompt\"", hasPrompt);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("url", "/restapi/v1.0/account/~/extension/~/ringout");
        headers.put("Content-Type", "application/json");
        this.post(body, headers, c);
    }

    /**
     * SMS API call using POST request
     *
     * @param to Phone number sending SMS to
     * @param from Phone number sending SMS from
     * @param message SMS text message body
     * @param c
     */
    public void sendSMS(String to, String from, String message, Callback c){
//        HashMap<String, String> body = new HashMap<>();
//        body.put("body", "{\n" +
//                "  \"to\": [{\"phoneNumber\": \"" + to + "\"}],\n" +
//                "  \"from\": {\"phoneNumber\": \"" + from + "\"},\n" +
//                "  \"text\": \"" + message + "\"\n" + "}");
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("\"to\"", "[{\"phoneNumber\": \"" + to + "\"}]");
        body.put("\"from\"", "{\"phoneNumber\": \"" + from + "\"}");
        body.put("\"text\"", "\"" + message + "\"");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("url", "/restapi/v1.0/account/~/extension/~/sms");
        headers.put("Content-Type", "application/json");
        this.post(body, headers, c);
    }

    //UNDER CONSTRUCTION
    public void postSubscription(){
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
//        body.put("body", "{\n" +
//                "  \"eventFilters\": [ \n" +
//                "    \"/restapi/v1.0/account/~/extension/~/presence\", \n" +
//                "    \"/restapi/v1.0/account/~/extension/~/message-store\" \n" +
//                "  ], \n" +
//                "  \"deliveryMode\": { \n" +
//                "    \"transportType\": \"PubNub\", \n" +
//                "    \"encryption\": \"false\" \n" +
//                "  } \n" +
//                "}");
        body.put("\"eventFilters\"", "[ \n" +
                "    \"/restapi/v1.0/account/~/extension/~/presence\", \n" +
                "    \"/restapi/v1.0/account/~/extension/~/message-store\" \n" +
                "  ]");
        body.put("\"deliveryMode\"", "{\"transportType\": \"PubNub\",\"encryption\": \"false\"}" );
        HashMap<String, String> headers = new HashMap<>();
        headers.put("url", "/restapi/v1.0/subscription");
        headers.put("Content-Type", "application/json");
        this.post(body, headers,
                new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);
                        RCResponse rcResponse = new RCResponse(response);
                        try {
                            JSONObject responseJson = new JSONObject(rcResponse.getBody());
                            subscription = new Subscription(responseJson);
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void renewSubscription(){
//        HashMap<String, String> body = new HashMap<>();
//        body.put("body", "{\n" +
//                "  \"eventFilters\": [ \n" +
//                "    \"/restapi/v1.0/account/~/extension/~/presence\", \n" +
//                "    \"/restapi/v1.0/account/~/extension/~/message-store\" \n" +
//                "  ], \n" +
//                "}");
//        HashMap<String, String> headers = new HashMap<>();
//        headers.put("url", "/restapi/v1.0/subscription" + subscription.subscriptionId);
//        headers.put("Content-Type", "application/json");
//        this.put(body, headers,
//                new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful())
//                            throw new IOException("Unexpected code " + response);
//                        RCResponse rcResponse = new RCResponse(response);
//                        try {
//                            JSONObject responseJson = new JSONObject(rcResponse.getBody());
//                            subscription.updateSubscription(responseJson);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
    }

    public void subscribe(){
        HashMap<String, String> options = new HashMap<>();
        options.put("address", subscription.address);
        subscription.subscribe(options,
                new com.pubnub.api.Callback() {

                    @Override
                    public void connectCallback(String channel, Object message) {
                        System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
                                + " : " + message.getClass() + " : "
                                + message.toString());
                    }

                    @Override
                    public void disconnectCallback(String channel, Object message) {
//                        System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
//                                + " : " + message.getClass() + " : "
//                                + message.toString());
                        String decryptedString = subscription.notify(message.toString(), subscription.encryptionKey);
                        System.out.print(decryptedString);
                    }

                    @Override
                    public void reconnectCallback(String channel, Object message) {
                        System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                + " : " + message.getClass() + " : "

                                + message.toString());
                    }

                    @Override
                    public void successCallback(String channel, Object message) {
//                                            System.out.println("SUBSCRIBE : " + channel + " : "
//                                                    + message.getClass() + " : " + message.toString());
                        //System.out.print(message.toString());
                        String decryptedString = subscription.notify(message.toString(), subscription.encryptionKey);
                        System.out.print(decryptedString);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                + " : " + error.toString());
                    }

                });
    }

}