package com.looper.loop;

/**
 * Created by rishabhpugalia on 26/01/15.
 *
 * TODO:
 * Someday understand how these work:
 * - CustomVideoCapturer
 * - CustomVideoRenderer
 * - CustomAudioDevice
 *
 * Must try to understand how the audio streams
 * are played and video streams are displayed on
 * SurfaceViews (OpenGL based ones too).
 */
public class OpenTokConfig {

    // *** Fill the following variables using your own Project info from the OpenTok dashboard  ***
    // ***                      https://dashboard.tokbox.com/projects                           ***
    // Replace with a generated Session ID
    public static final String SESSION_ID = "1_MX40NTEzNTcwMn5-MTQyMjI2NzQ5NDUwNn5qMTZHUUxIWWlVbzlwSmJGUUFuUDN0VEV-fg";
    // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00NTEzNTcwMiZzaWc9MDEwYzdjMTkxYTlmZGNmYzQyMjNhMTgyMWM1YWI2ODNiMGFlZjIwZjpyb2xlPXB1Ymxpc2hlciZzZXNzaW9uX2lkPTFfTVg0ME5URXpOVGN3TW41LU1UUXlNakkyTnpRNU5EVXdObjVxTVRaSFVVeElXV2xWYnpsd1NtSkdVVUZ1VUROMFZFVi1mZyZjcmVhdGVfdGltZT0xNDIyMjY3NTA2Jm5vbmNlPTAuNzMxNjQ1MzMyOTgzNTM4MiZleHBpcmVfdGltZT0xNDI0ODU4OTI5";
    // Replace with your OpenTok API key
    public static final String API_KEY= "45135702";

    // Subscribe to a stream published by this client. Set to false to subscribe
    // to other clients' streams only.
    public static final boolean SUBSCRIBE_TO_SELF = false;
}
