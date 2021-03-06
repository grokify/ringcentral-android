# RingCentral Android SDK

[![Build Status](https://travis-ci.org/vyshakhbabji/ringcentral-android.svg)](https://travis-ci.org/vyshakhbabji/ringcentral-android)
[![Bintray][bintray-version-svg]][bintray-version-link]
[![License][license-svg]][license-link]


## Table of contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Basic Usage](#basic-usage)
	1. [Initialization](##initialization)
	2. [Authentication](##authentication)
4. [Helper Examples](#helper-examples)
	1. [RingOut](##performing-a-ringout)
	2. [SMS](##sending-an-sms)
	3. [Call Log](##getting-the-call-log)

# Overview

This RingCentral Android SDK has been made to make Android development easier for developers who are using RingCentral Platform's suite of APIs. It handles authentication and the token lifecycle, makes API requests, and parses API responses. This documentation will help you get set up and going with some example API calls.

# Installation

## Android Studio Environment

### Install SDK

You can install the RingCentral SDK via JCenter or by installing the AAR file locally.

#### Via JCenter

To add this SDK to your project from JCenter, add this line to your Gradle dependencies for your app. Here is the link to the online repository: https://bintray.com/ringcentral/maven/rc_android_sdk/view
Add these to your app's Gradle dependencies:

```java
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.ringcentral.rcandroidsdk:rc_android_sdk:0.5'
}
```

#### Via Local AAR File

1. Download AAR from the GitHub release page
2. Move AAR file into your app module's `libs` directory
3. Edit the `app/build.gradle` file to add the following Gradle script:

```
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    ...
    compile 'com.ringcentral.library:ringcentral-android-0-7@aar'
}
```

### Configure App Permissions

Add the follow permissions to your app module's `AndroidManifest.xml` (`app/src/main/AndroidManifest.xml`) within the `<manifest>` tag to utilize RingCentral capabilties:

```java
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

### Import library

To import the SDK to your app, e.g. `MainActivity`, add this line:

```java
import com.ringcentral.rc_android_sdk.rcsdk.*;
```

# Basic Usage

## Initialization

Create an instance of the global SDK object in your application, and configure it with your unique API key, secret, and server URL.

##### Production:

```java
sdk = new SDK(appKey, appSecret, Platform.Server.PRODUCTION);
```
##### Sandbox

```java
sdk = new SDK(appKey, appSecret, Platform.Server.SANDBOX);
```

#### Get Platform Singleton

```java
Platform platform = sdk.platform();
```
With the oldPlatform singleton and the SDK configured with the correct server URL and API key, your application can authenticate to access the features of the API.

## Authentication

Authentication is done by calling the `oldPlatform.authorize()` method with the username, extension(optional), and password. Also, because the login process is asynchronous, you have to call a `new Callback()` and pass that in as the last parameter. You can handle login success in the overriding of the Callback's `onResponse()`, such as performing updates to the user interface. To handle login failure, you can add error handling in `onFailure()`.

```java
 platform.login("username", "ext", "password", new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        //handle exception
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && platform.loggedIn()) {
                                //set the current authorized platform instance to the singleton instance 
                                Singleton.getInstance().setPlatform(helpers);
                                //start activities
                                Intent optionsIntent = new Intent(ActivityA.this, ActivityB.class);
                                startActivity(optionsIntent);
                            }
                        } catch (Exception e) {
                            //handle exception
                        }
                    }
                });
```

####Checking Authentication State

To check in your Application if the user is authenticated, you can call the oldPlatform singleton's `isAuthorized()` method which will handle refreshing tokens for you, or throw an exception if the refreshed Access Token is invalid.

```java
platform.loggedIn(); //returns boolean 
```
<!---
##Performing API calls

To perform an authenticated API call, you should use the `get` `post` `put` or `delete` method of the oldPlatform singleton. For calling `get` and `post` requests, pass in a Hashmap for the body and the headers, and a Callback since Android HTTP requests are asynchronous. If your body needs to be encoded Form Data as key value pairs, add to the body HashMap with keys and values. Or else, just add the body string with they key as "body",

```java
LinkedHashMap<String, String> body = new LinkedHashMap();
body.put("body", "BodyStringGoesHere")
```
For all API calls, create a HashMap for headers and add header-type 
as the key, and header values as the value.

```java
HashMap<String, String> headers = new HashMap();
// Add headers (e.g. "Content-Type", "url") 
headers.put("Content-Type", "application/json");
String url = "/restapi/v1.0/account/~/extension/~/sms";
```
Example post request, passing in the body, headers, and Callback:

```java
platform.post(url, body, headers,
	new Callback() {
		@Override
		public void onFailure(Request request, IOException e) {
			e.printStackTrace();
		}
		@Override
		public void onResponse(Response response) throws IOException {
		Transaction transaction = new Transaction(response);
		if(!response.isSuccessful())
			throw new IOException(transaction.getError());
			// Your code goes here
		}
});
```




# Helper Examples

### Get Helper Singleton

To use the Helpers class which extends the Platform class, initialize it like you would for Platform and call the helper methods from this Helpers object.

```java
Helpers helpers = SDK.getHelpers();
```

## Performing a RingOut
The RingOut POST API call has a helper function written so you can just input the "To", "From", and "Caller ID" phone numbers.

```java
helpers.ringOut(
	"15101234567", // Phone number calling "To"
	"18881234567", // Phone number calling "From"
	"12223334444", // Caller ID number
	"True", // "True" or "False" states whether a prompt plays before a call
	new Callback() {
		@Override
                public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                }
                @Override
                public void onResponse(Response response) throws IOException {
                	Transaction transaction = new Transaction(response);
                    String responseString = transaction.getBodyString();
                    // If HTTP response is not successful, throw exception
                    if (!response.isSuccessful()) {
                        try {
                        JSONObject jsonObject = new JSONObject(responseString);
           				String errorCode = jsonObject.getString("errorCode");
                        String message = jsonObject.getString("message");
                        throw new IOException("Error code: "+ authResponse.getStatus() + ". Error: " + errorCode + ": " + message);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    // Your code goes here
                }
});	
```

## Sending an SMS

The send SMS POST API call has a helper function written so you can input the "To", and "From" phone number and SMS message.

```java
helpers.sendSMS(
	"15101234567", // Phone number calling "To"
	"18881234567", // Phone number calling "From"
	"This is a sample text message",
	new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                }
                @Override
                public void onResponse(Response response) throws IOException {
                	Transaction transaction = new Transaction(response);
                    String responseString = transaction.getBodyString();
                    // If HTTP response is not successful, throw exception
                    if (!response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            String errorCode = jsonObject.getString("errorCode");
                        	String message = jsonObject.getString("message");
                            throw new IOException("Error code: "+ smsResponse.getStatus() + ". Error: " + errorCode + ": " + message);
                        } catch (JSONException e){
                            e.printStackTrace();
                     	}
                    }
                	// Your code goes here
                }
});
```

## Getting the call log

The call log GET API call has a helper function written that returns the response in the Callback.
```java
helpers.callLog(
	new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                }
                @Override
                public void onResponse(Response response) throws IOException {
                if(!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                Transaction transaction = new Transaction(response);
                	// Your code goes here
                }
});	
```	
-->
### Android Demo app link: 
https://github.com/vyshakhbabji/ringcentral-android-sdk-demoapp

## License

RINGCENTRAL ringcental-android SDK is available under an MIT-style license. See [LICENSE.md](LICENSE.md) for details.

RINGCENTRAL ringcentral-android &copy; by Vyshakh Babji, Andrew Pang



 [build-status-svg]: https://travis-ci.org/ringcentral/ringcentral-android.svg?branch=master
 [build-status-link]: https://travis-ci.org/ringcentral/ringcentral-android
 [bintray-version-svg]: https://img.shields.io/bintray/v/ringcentral/maven/rc_android_sdk.svg
 [bintray-version-link]: https://bintray.com/ringcentral/maven/rc_android_sdk/view
 [license-svg]: https://img.shields.io/badge/license-MIT-blue.svg
 [license-link]: https://github.com/ringcentral/ringcentral-android/blob/master/LICENSE.md
