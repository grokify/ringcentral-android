/*
 * Copyright (c) 2015 RingCentral, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.ringcentral.rc_android_sdk.rcsdk.platform;

import com.ringcentral.rc_android_sdk.rcsdk.http.Client;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;



public class Platform {
    /*
    Revoke Session Endpoint
     */
    final String REVOKE_ENDPOINT_URL = "/restapi/oauth/revoke";

   /*
   Authentication  and Refresh Token Endpoint
    */

    final String TOKEN_ENDPOINT_URL = "/restapi/oauth/token";


    protected String appKey;
    protected String appSecret;
    protected Server server;

    protected Auth auth;
    protected Request request;
    protected Client client;

    Object lock = new Object();
    boolean refreshInProgress;
    boolean state;

    protected Queue<Callback> queue = new LinkedBlockingQueue<>();

    /**
     * Creates Platform object
     *
     * @param client
     * @param appKey
     * @param appSecret
     * @param server
     */
    public Platform(Client client, String appKey, String appSecret, Server server) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.server = server;
        this.auth = new Auth();
        this.client = client;
    }

    /**
     * @return Base 64 encoded app credentials
     */
    protected String apiKey() {
        return Credentials.basic(appKey, appSecret);
    }

    /**
     * @return Authorization Header "bearer @accesstoken"
     */
    protected String authHeader() {
        return this.auth.tokenType() + " " + this.auth.access_token;
    }

    /**
     * Checks if the current access token is valid. If the access token is expired, it does token refresh.
     * FIXME This is asynchronous method, so it must accept a callback : Fixed
     */
    protected void ensureAuthentication(Callback callback) throws AuthException {
        if(!loggedIn()){
                refreshInProgress=true;
                refresh(callback);
        }
    }

    /**
     * Sets Request body for content type FORM_TYPE("application/x-www-form-urlencoded")
     * @param body Input body as key:value pairs
     * @return
     */
    protected RequestBody formBody(HashMap<String, String> body) {
        FormEncodingBuilder formBody = new FormEncodingBuilder();
        for (HashMap.Entry<String, String> entry : body.entrySet())
            formBody.add(entry.getKey(), entry.getValue());
        return formBody.build();
    }

    /**
     * Get Auth object
     * @return Auth Object
     */
    public Auth auth() {
        return auth;
    }

    /**
     * Checks if the login is valid
     */
    public boolean loggedIn() {
            return this.auth.accessTokenValid();
    }

    /**
     * Sets Login Credentials for authentication
     *
     * @param userName
     * @param extension
     * @param password
     * @param callback
     */
    public void login(String userName, String extension, String password, Callback callback) throws AuthException {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("username", userName);
        body.put("password", password);
        body.put("extension", extension);
        body.put("grant_type", "password");
        requestToken(TOKEN_ENDPOINT_URL, body, callback);
    }

    /**
     * Sets Request Header
     *
     * @param hm
     * @return fombody
     * FIXME Async :  Fixed
     */
    protected Builder inflateRequest(HashMap<String, String> hm) {
        Builder requestBuilder = new Request.Builder();
        for (Entry<String, String> entry : hm.entrySet())
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        return requestBuilder;
    }

    /**
     * Sets authentication values after successful authentication
     * @param response
     */ //FIXME : Fixed by calling method
    protected void setAuth(Response response) throws IOException {
            this.auth.setData(auth.jsonToHashMap(response));
    }

    /**
     * Creates request object
     * @param endpoint
     * @param body
     * @param callback
     */ //FIXME : CHange name
    protected void requestToken(String endpoint, HashMap<String, String> body, final Callback callback) throws AuthException {
        final String URL = server.value + endpoint;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", apiKey());
        headers.put("Content-Type", ContentTypeSelection.FORM_TYPE_MARKDOWN.value.toString());
        request = inflateRequest(headers).url(URL).post(formBody(body)).build();
        final Callback c = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(request,e);
                //throw new AuthException("Unable to request token.", e); //FIXME Call the callback instead of throwing
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    setAuth(response);
                    callback.onResponse(response);
                } catch (IOException e) {
                    callback.onFailure(response.request(), new IOException("IOException Occured. Failed Logout with error code " + response.code()));
                }
            }
        };
        client.sendRequest(request, c);
    }


    /**
     * Sets new access and refresh tokens
     *
     * @param callback
     * @throws AuthException
     */
    public synchronized void refresh(final Callback callback) throws AuthException {

        synchronized (lock) {
            if (refreshInProgress == false) {
                refreshInProgress = true;
            }
            queue.add(callback);
            if (state == refreshInProgress)
                return;
            else {
                state = refreshInProgress;
            }
        }
            makeRequest(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        for (Callback c : queue) {
                            c.onFailure(request,new IOException());
                            queue.remove(c);
                        }
                        synchronized (lock) {
                            state = false;
                        }
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        for (Callback c : queue) {
                            if (response.isSuccessful()) {
                                c.onResponse(response);
                                queue.remove(c);
                            } else {
                                queue.remove(c);
                                c.onFailure(response.request(),new IOException("IO Exception Occured. Failed Refreshing with Error Code "+response.code()));
                            }
                        }
                        synchronized (lock) {
                            state = false;
                        }
                    }
            });
    }


        /*

        synchronized(lock) {
           callbackueue.push(callback)
           if state == refresh_in_progress:
             return
           else:
             state = refresh_in_progress
        }

           make_request(refresh_params, new Callback() {
             onSuccess() {
               synchronized(lock) {
                 state = free
                 for ( c : callbackqueue ):
                   c.onSuccess()
                   callbackqueue.remove(c)
               }
             }
           });


         */

    protected void makeRequest(Callback callback)  throws AuthException{
         if (!this.auth.refreshTokenValid()) {
            throw new AuthException("Refresh Token has Expired");
         }
         else{
             HashMap<String, String> body = new HashMap<String, String>();
             body.put("grant_type", "refresh_token");
             body.put("refresh_token", this.auth.refreshToken());
             requestToken(TOKEN_ENDPOINT_URL, body, callback);
         }
    }

    /**
     * Revoke current session
     *
     * @param callback
     */
    public void logout(final Callback callback) throws AuthException {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("access_token", this.auth.access_token);
        requestToken(REVOKE_ENDPOINT_URL, body, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    auth.reset(); //FIXME This should go inside the callback:fixed
                    callback.onResponse(response);
                } else {
                    callback.onFailure(response.request(), new IOException("IOException Occured. Failed Logout with error code " + response.code()));
                }
            }
        });

    }


    /**
     * Send API Request
     *
     * @param method
     * @param apiURL
     * @param body
     * @param headerMap
     * @param callback
     */
    public void sendRequest(final String method, final String apiURL, final RequestBody body, final HashMap<String, String> headerMap, final Callback callback) throws AuthException{

        ensureAuthentication(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(request,e);
            }

            @Override
            public void onResponse(Response response)  {
                HashMap<String,String> header=null;
                if(headerMap==null){
                    header = new HashMap<String,String>();
                }
                else
                    header=headerMap;
                final String URL = server.value + apiURL;

                    if (!header.containsKey("Authorization")) {
                        header.put("Authorization", authHeader());
                    }
                    request = client.createRequest(method, URL, body, inflateRequest(header));
                    client.sendRequest(request, callback);
            }
        });
    }

    /**
     * Sets auth data
     * @param response
     * @return
     * @throws IOException
     */

    /**
     * Sets content-type
     * FIXME Change naming:Fixed
     */
    public enum ContentTypeSelection {
        FORM_TYPE_MARKDOWN("application/x-www-form-urlencoded"), JSON_TYPE_MARKDOWN(
                "application/json"), MULTIPART_TYPE_MARKDOWN("multipart/mixed;");
        protected MediaType value;

        private ContentTypeSelection(String contentType) {
            this.value = MediaType.parse(contentType);
        }
    }

    /**
     * RingCentral API Endpoint Server. See
     * "https://developer.ringcentral.com/api-docs/latest/index.html#!#Resources.html" Server Endpoint for more information.
     */
    public enum Server {
        PRODUCTION("https://platform.ringcentral.com"), SANDBOX(
                "https://platform.devtest.ringcentral.com");
        private String value;

        Server(String url) {
            this.value = url;
        }
    }

    //FIXME get, post, put, delete methods are missing : Fixed

    public void get(String apiURL, RequestBody body, HashMap<String, String> headerMap, final Callback callback) throws AuthException {

        sendRequest("get",apiURL,body==null?null:body,headerMap,callback);
    }

    public void post(String apiURL, RequestBody body, HashMap<String, String> headerMap, final Callback callback) throws AuthException {
        sendRequest("post",apiURL,body,headerMap,callback);
    }

    public void put(String apiURL, RequestBody body, HashMap<String, String> headerMap, final Callback callback) throws AuthException {

        sendRequest("put",apiURL,body,headerMap,callback);
    }

    public void delete(String apiURL, RequestBody body, HashMap<String, String> headerMap, final Callback callback) throws AuthException {

        sendRequest("delete",apiURL,body==null?null:body,headerMap,callback);
    }


    public void expire_access(){
        auth().expire_access();
    }

    public String showError(Response response) {
        String message = "";
        if (!response.isSuccessful()) {
            message = "HTTP error code: " + response.code() + "\n";

            try {
                JSONObject data = new JSONObject(response.body().string());

                if (data == null) {
                    message = "Unknown response reason phrase";
                }

                if (data.getString("message") != null)
                    message = message + data.getString("message");

                if (data.getString("error_description") != null)
                    message = message + data.getString("error_description");

                if (data.getString("description") != null)
                    message = message + data.getString("description");


            } catch (JSONException | IOException e) {
                message = message + " and additional error happened during JSON parse " + e.getMessage();
            }
        } else {
            message = "";
        }
        return message;
    }

}