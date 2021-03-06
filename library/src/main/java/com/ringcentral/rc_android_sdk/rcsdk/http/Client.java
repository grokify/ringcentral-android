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
package com.ringcentral.rc_android_sdk.rcsdk.http;

import android.os.AsyncTask;

<<<<<<< HEAD
import com.ringcentral.rc_android_sdk.rcsdk.http.APICallback;
//import com.squareup.okhttp.Callback;
=======
import com.squareup.okhttp.Callback;
>>>>>>> f35ebd5902482eda5b90874d7e689071c019ffdc
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by vyshakh.babji on 11/9/15.
 */
public class Client {

    public OkHttpClient client;

    public Client() {
        client = new OkHttpClient();
    }

    /**
     * Makes a OKHttp  call
     *
     * @param request
     */
<<<<<<< HEAD
    public void sendRequest(final Request request,  final APICallback callback) {
=======
    //FIXME Name should be sendRequest
    //FIXME Take a look at reference -- this method should do a different thing

    public void sendRequest(final Request request,  final Callback callback) {
>>>>>>> f35ebd5902482eda5b90874d7e689071c019ffdc
        try {
            new AsyncTask<String, Integer, Void>() {
                @Override
                protected Void doInBackground(String... params) {
<<<<<<< HEAD
                        APICallback c = new APICallback() {

                            public void onAPIFailure(Request request, IOException e) {
                                callback.onAPIFailure(request, e);
=======
                        Callback c = new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                callback.onFailure(request, e);
>>>>>>> f35ebd5902482eda5b90874d7e689071c019ffdc
                            }


                            public void onAPIResponse(APIResponse response) throws IOException {
                                if(response.ok())
                                   callback.onAPIResponse(response);
                                else
                                   callback.onAPIFailure(response.request(), new IOException("IOException Occured. Sending request failed with error code " + response.ok()));
                            }
                        };
                        loadResponse(request,c);
                        return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates OKHttp Request
     *
     * @param method
     * @param URL
     * @param body
     * @param header
     * @return OKHttp Request
     */
    public Request createRequest(String method, String URL, RequestBody body, Builder header) {
        Request.Builder request = new Request.Builder();
        if (method.equalsIgnoreCase("get")) {
            request = header.url(URL);
        } else if (method.equalsIgnoreCase("delete")) {
            request = header.url(URL).delete();
        } else {
            if (method.equalsIgnoreCase("post")) {
                request = header.url(URL).post(body);

            } else if (method.equalsIgnoreCase("put")) {
                request = header.url(URL).put(body);
            } else
                    throw new RuntimeException(method +" Method not Allowed. Please Refer API Documentation. See\n" +
                            "     * <a href =\"https://developer.ringcentral.com/api-docs/latest/index.html#!#Resources.html\">Server Endpoint</a> for more information. ");
        }
        return request.build();
    }

    /**
     * Loads OKHttp Response synchronizing async api calls
     * @param request
     * @param callback
     */
    public  void loadResponse(final Request request, final APICallback callback) {
          client.newCall(request).enqueue(callback);
    }

    public Headers getRequestHeader(Request request) {
        return request.headers();
    }

    public void _response(APICallback callback){
        Request request = new Request.Builder()
                .url("http://www.ringcentral.com")
                .build();
        sendRequest(request,callback);
    }

}