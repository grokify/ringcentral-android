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

import com.ringcentral.rc_android_sdk.rcsdk.platform.APIException;
import com.ringcentral.rc_android_sdk.rcsdk.platform.RingCentralException;
import com.squareup.okhttp.Callback;
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

//    public OkHttpClient client;
//
//    public Client() {
//        client = new OkHttpClient();
//    }

    private final OkHttpClient client = new OkHttpClient();

    /**
     * Makes a OKHttp  call
     *
     * @param request
     */
    public void sendRequest(final Request request, final APICallback callback) {


        try {
            new AsyncTask<String, Integer, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    Callback responseLoaderCallback = new Callback() {

                        @Override
                        public void onResponse(Response response) throws IOException {
                            APIResponse apiresponse = new APIResponse(response);
                            if (apiresponse.ok())
                                callback.onResponse(apiresponse);
                            else {

                                throw new APIException(apiresponse.showError());
                            }
                        }

                        @Override
                        public void onFailure(Request request, IOException e) {
                            callback.onFailure(new APIException(new APIResponse(request), e));
                        }
                    };
                    loadResponse(request, responseLoaderCallback);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } catch (ExecutionException e) {
            handleExecutionException(e);
        }

    }

    private RuntimeException handleInterruptedException(Exception e) {
        Thread.currentThread().interrupt();
        return new RingCentralException(e);
    }

    private RuntimeException handleExecutionException(Exception e) {
        Thread.currentThread().interrupt();
        return new RingCentralException(e);
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
                throw new APIException(method + " Method not Allowed. Please Refer API Documentation. See\n" +
                        "     * <a href =\"https://developer.ringcentral.com/api-docs/latest/index.html#!#Resources.html\">Server Endpoint</a> for more information. ");
        }
        return request.build();
    }

    /**
     * Loads OKHttp Response synchronizing async api calls
     *
     * @param request
     * @param callback
     */
    protected void loadResponse(final Request request, final Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    public void _response(APICallback callback) {
        Request request = new Request.Builder()
                .url("http://www.ringcentral.com")
                .build();
        sendRequest(request, callback);
    }


}