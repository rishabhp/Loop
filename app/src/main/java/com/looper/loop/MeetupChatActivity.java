package com.looper.loop;

import android.app.ActionBar;
        import android.app.Activity;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.content.res.Configuration;
        import android.media.AudioManager;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.PowerManager;
        import android.support.v4.app.NotificationCompat;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;

        import com.opentok.android.BaseVideoRenderer;
        import com.opentok.android.OpentokError;
        import com.opentok.android.Publisher;
        import com.opentok.android.PublisherKit;
        import com.opentok.android.Session;
        import com.opentok.android.Stream;
        import com.opentok.android.Subscriber;
        import com.opentok.android.SubscriberKit;

        import java.util.ArrayList;


public class MeetupChatActivity extends Activity implements
        Session.SessionListener,
        Publisher.PublisherListener,
        Subscriber.VideoListener {

    private static final String LOGTAG = "demo-hello-world";
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ArrayList<Stream> mStreams;
    protected Handler mHandler = new Handler();

    /*
    * Publisher is an object representing the audio-video stream
    * sent from the Android device to the OpenTok session. So this is
    * the user holding the phone (or some other device), i.e., self
    *
    * Subscriber is an object that subscribes to an audio-video
    * stream from the OpenTok session that you display on your
    * device. The subscriber stream can receive publication from
    * self or (more commonly) from a stream published by another client
    * to the OpenTok session.
    *
    * In short, publisher is self and subscriber is the other part (peer B)
    * */
    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private boolean resumeHasRun = false;

    private boolean mIsBound = false;
    private NotificationCompat.Builder mNotifyBuilder;
    NotificationManager mNotificationManager;
    ServiceConnection mConnection;

    PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOGTAG, "ONCREATE");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meetup_chat);

        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);
        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Media streams (with Audio/Video tracks)
        mStreams = new ArrayList<Stream>();

        // An app must create a Session object and connect
        // to the (OpenTok) session before the app can
        // publish or subscribe to streams in the session.
        sessionConnect();

        // Wake Lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Remove publisher & subscriber views because we want to reuse them
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
        }
        reloadInterface();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSession != null) {
            mSession.onPause();

            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }
        }

        /*mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(this.getTitle())
                .setContentText(getResources().getString(R.string.notification))
                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        mNotifyBuilder.setContentIntent(intent);
        if(mConnection == null){
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className,IBinder binder){
                    ((ClearBinder) binder).service.startService(new Intent(HelloWorldActivity.this, ClearNotificationService.class));
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotificationManager.notify(ClearNotificationService.NOTIFICATION_ID, mNotifyBuilder.build());
                }

                @Override
                public void onServiceDisconnected(ComponentName className) {
                    mConnection = null;
                }

            };
        }

        if(!mIsBound){
            bindService(new Intent(HelloWorldActivity.this,
                            ClearNotificationService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }*/

    }

    @Override
    public void onResume() {
        super.onResume();

        if(mIsBound){
            unbindService(mConnection);
            mIsBound = false;
        }

        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        } else {
            if (mSession != null) {
                mSession.onResume();
            }
        }
        //mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);

        reloadInterface();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mIsBound){
            unbindService(mConnection);
            mIsBound = false;
        }

        if(mIsBound){
            unbindService(mConnection);
            mIsBound = false;
        }
        if (isFinishing()) {
            //mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
            if (mSession != null) {
                mSession.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        //mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
        if(mIsBound){
            unbindService(mConnection);
            mIsBound = false;
        }

        if (mSession != null)  {
            mSession.disconnect();
        }

        restartAudioMode();

        mWakeLock.release();

        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mSession != null) {
            mSession.disconnect();
        }

        restartAudioMode();

        super.onBackPressed();
    }

    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);
                }
            }
        }, 500);
    }

    public void restartAudioMode() {
        AudioManager Audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Audio.setMode(AudioManager.MODE_NORMAL);
        this.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    private void sessionConnect() {
        if (mSession == null) {
            // Instantiate a Session object
            mSession = new Session(MeetupChatActivity.this, OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            // Setup listeners for basic session-related events
            mSession.setSessionListener(this);
            // Connect session object to the OpenTok session
            mSession.connect(OpenTokConfig.TOKEN);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "Connected to the session.");
        if (mPublisher == null) {
            // Publisher is self

            // Create a new object that can be published to the
            // OpenTok session
            mPublisher = new Publisher(MeetupChatActivity.this, "publisher");
            // Listen to publisher related events
            mPublisher.setPublisherListener(this);
            // Add the view to display publisher stream (video)
            attachPublisherView(mPublisher);
            // Publish a stream to the OpenTok session
            mSession.publish(mPublisher);
        }
    }

    /*
    * Invoked when this client (publisher) disconnects from
    * this OpenTok session. So App removes views for any the
    * publisher and subscriber.
    *
    * Also other resource releasing happens.
    * */
    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Disconnected from the session.");
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
        }

        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
        }

        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();
        mSession = null;
    }

    /*
    * Subscribe to a stream (generally the other party)
    *
    * */
    private void subscribeToStream(Stream stream) {

        // Subscriber object to subscribe to a stream
        mSubscriber = new Subscriber(MeetupChatActivity.this, stream);
        // Sets Subscriber.VideoListener object to respond
        // to subcriber-related events
        mSubscriber.setVideoListener(this);
        // Make this session subscribe to the stream
        Log.d(LOGTAG, "Trying to subscribe once");
        mSession.subscribe(mSubscriber);
        // start loading spinning
        mLoadingSub.setVisibility(View.VISIBLE);
    }

    private void unsubscribeFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }
    }

    // Attach Subscriber view (other party)
    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    // Attach publisher view (self)
    private void attachPublisherView(Publisher publisher) {
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                320, 240);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        layoutParams.bottomMargin = dpToPx(8);
        layoutParams.rightMargin = dpToPx(8);
        Log.d(LOGTAG, String.valueOf(mPublisher.getView()));
        mPublisherViewContainer.addView(mPublisher.getView(), layoutParams);
    }

    /*
    * Invoked when something wrong happens while connecting
    * to the session, like no network connection.
    *
    * In such a case, the Session should be treated has dead
    * and unavailable. Do not attempt to reconnect or to call
    * other methods of the Session object.
    * */
    @Override
    public void onError(Session session, OpentokError exception) {
        Log.i(LOGTAG, "Session exception: " + exception.getMessage());
    }

    /*
    * When there's new stream published by another client
    * to this OpenTok Session, i.e., a Subscriber is kicking in
    * */
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(LOGTAG, "onStreamReceived method");

        // Only one video can be attached due to `mSubscriber == null`
        // condition. Also note,
        // if you remove that condition then the 3rd party
        // (second subcriber) will not replace the first subscriber (2nd party)

        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
            mStreams.add(stream);
            if (mSubscriber == null) {
                subscribeToStream(stream);
            }
        }
    }

    /*
    * Invoked when a client (generally subscriber - another party)
    * stops publishing a stream to this OpenTok session
    *
    * Also the app tries to subscribe to any other stream
    * in the session by calling subscribeToStream()
    * from unsubscribeFromStream()
    * */
    @Override
    public void onStreamDropped(Session session, Stream stream) {
        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
            if (mSubscriber != null) {
                unsubscribeFromStream(stream);
            }
        }
    }

    /*
    * Called when the Publisher starts streaming to the
    * OpenTok session
    * */
    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {
        Log.d(LOGTAG, "onStreamCreated method");

        // If you want to show the publisher face in
        // subcriber's screen
        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
            mStreams.add(stream);
            if (mSubscriber == null) {
                subscribeToStream(stream);
            }
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
        if ((OpenTokConfig.SUBSCRIBE_TO_SELF && mSubscriber != null)) {
            unsubscribeFromStream(stream);
        }
    }

    @Override
    public void onError(PublisherKit publisher, OpentokError exception) {
        Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
    }

    /*
    * Invoked when initially subscriber's video data is received
    *
    * So it's called only once
    * */
    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(LOGTAG, "First frame received");

        // stop loading spinning
        mLoadingSub.setVisibility(View.GONE);
        attachSubscriberView(mSubscriber);
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     *
     * @param dp
     *            A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG,
                "Video disabled:" + reason);
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG,"Video enabled:" + reason);
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriber) {
        Log.i(LOGTAG, "Video may be disabled soon due to network quality degradation. Add UI handling here.");
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        Log.i(LOGTAG, "Video may no longer be disabled as stream quality improved. Add UI handling here.");
    }

}