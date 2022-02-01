package com.pedro.rtpstreamer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtpstreamer.defaultexample.ExampleRtmpActivity;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.security.KeyStore;
import java.security.KeyFactory;
import java.security.cert.Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class DialogActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, SurfaceHolder.Callback  {
    static class Message
    {
        private String m_strUserid;
        private String m_strMessage;
        private String m_strMessageR;
        private String m_strDate;

        public Message( String strNickname, String strMessage, String strMessageR, String strDate )
        {
            m_strMessage = strMessage;
            m_strMessageR = strMessageR;
            m_strDate = strDate;
        }

        public String getUserid()
        {
            return m_strUserid;
        }

        public String getMessage()
        {
            return m_strMessage;
        }
        public String getMessageR() { return m_strMessageR; }

        public String getDateUi()
        {
            return m_strDate;
        }
    }

    static class MessageListAdapter extends BaseAdapter
    {
        private ArrayList<Message> m_listMessage;
        private LayoutInflater m_inflater;

        public MessageListAdapter( Activity activity )
        {
            super();
            m_listMessage = new ArrayList<Message>();
            m_inflater = activity.getLayoutInflater();
        }

        // リストへの追加
        public void addMessage( Message message )
        {
            m_listMessage.add( 0, message );    // 先頭に追加
            notifyDataSetChanged();    // ListViewの更新
        }

        // リストのクリア
        public void clear()
        {
            m_listMessage.clear();
            notifyDataSetChanged();    // ListViewの更新
        }

        @Override
        public int getCount()
        {
            return m_listMessage.size();
        }

        @Override
        public Object getItem( int position )
        {
            return m_listMessage.get( position );
        }

        @Override
        public long getItemId( int position )
        {
            return position;
        }

        static class ViewHolder
        {
            TextView textviewDate;
            TextView textviewDateR;
            TextView textviewNickname;
            TextView textviewMessage;
            TextView textviewMessageR;
            View layout_l;
            View layout_r;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if( null == convertView )
            {
                convertView = m_inflater.inflate( R.layout.listitem_message, parent, false );
                viewHolder = new ViewHolder();
                viewHolder.textviewDate = (TextView)convertView.findViewById( R.id.textview_date );
                viewHolder.textviewDateR = (TextView)convertView.findViewById( R.id.textview_date_r );
                viewHolder.textviewNickname = (TextView)convertView.findViewById( R.id.textview_nickname );
                viewHolder.textviewMessage = (TextView)convertView.findViewById( R.id.textview_message );
                viewHolder.textviewMessageR = (TextView)convertView.findViewById( R.id.textview_message_r );
                viewHolder.layout_l = (View)convertView.findViewById(R.id.layout_l);
                viewHolder.layout_r = (View)convertView.findViewById(R.id.layout_r);
                convertView.setTag( viewHolder );
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            Message message = m_listMessage.get( getCount() - 1 - position );
            viewHolder.textviewNickname.setText( message.getUserid() );
            viewHolder.textviewMessage.setText( message.getMessage() );
            viewHolder.textviewMessageR.setText( message.getMessageR() );
             if (message.getMessage().equals("")) {
                 viewHolder.textviewDate.setText( "" );
                 viewHolder.textviewDateR.setText( message.getDateUi() );
                 viewHolder.textviewMessage.setVisibility(View.INVISIBLE);
                 viewHolder.textviewMessageR.setVisibility(View.VISIBLE);
                 viewHolder.textviewDate.setVisibility(View.INVISIBLE);
                 viewHolder.textviewDateR.setVisibility(View.VISIBLE);
                 viewHolder.layout_l.setVisibility(View.INVISIBLE);
                 viewHolder.layout_r.setVisibility(View.VISIBLE);

            }
            else {
                 viewHolder.textviewDate.setText( message.getDateUi() );
                 viewHolder.textviewDateR.setText( "" );
                 viewHolder.textviewMessage.setVisibility(View.VISIBLE);
                 viewHolder.textviewMessageR.setVisibility(View.INVISIBLE);
                 viewHolder.textviewDate.setVisibility(View.VISIBLE);
                 viewHolder.textviewDateR.setVisibility(View.INVISIBLE);
                 viewHolder.layout_l.setVisibility(View.VISIBLE);
                 viewHolder.layout_r.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    // 定数
    public static final String EXTRA_USERID = "USER_ID";
    // メンバー変数
    private TextView wsStateTextView;
    private String m_strUserid = "test-pattern00";
    private String m_websocketurl = "wss://micsus.nict.go.jp/uiService";
    private String m_websocketurltest = "ws://10.1.27.234:5001";

    private myWsClientListener m_ws;
    MessageListAdapter m_messagelistadapter;
    private String m_sidstr; // 開始時に決めたsession id
    private String m_curRequest;
    private String m_volume = "50";
    private String m_voicetype = "1";
    private String m_speed = "10";
    private String m_speechinterval = "100";
    private String m_emotion_thresholds = "";
    private Queue<JSONObject> m_responsqueue = new ArrayDeque<>();
    private JSONObject m_currentresponse = null;
    private Handler guiThreadHandler;

    private RtmpCamera1 rtmpCamera1;

    private final String TAG = "MAINDIALOG";
    private int micsts = -1;

    private String getDate()
    {
        SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdft = new SimpleDateFormat("HH:mm:ss.SSS");
        Date d = new Date();
        String strdate = sdfd.format(d) + "T" + sdft.format(d);
        return strdate;
    }
    private String getDateUi()
    {
        SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdft = new SimpleDateFormat("HH:mm");
        Date d = new Date();
        String strdate = sdfd.format(d) + " " + sdft.format(d);
        return strdate;
    }

    private static final String BEGIN_RSA_CERT = "-----BEGIN CERTIFICATE-----\n"
            +"MIIECDCCAvCgAwIBAgIBAzANBgkqhkiG9w0BAQsFADCBhzELMAkGA1UEBhMCSlAx\n"
            +"DjAMBgNVBAgMBUt5b3RvMRMwEQYDVQQHDApTZWlrYS10b3duMRkwFwYDVQQKDBBJ\n"
            +"bnRlciBDQSBvZiBOSUNUMQ8wDQYDVQQLDAZESVJFQ1QxJzAlBgNVBAMMHmludGVy\n"
            +"LWNhLmxvY2FsLWF1dGgubmljdC5nby5qcDAeFw0yMDA3MzAwNTI4MTBaFw0zMDA3\n"
            +"MjgwNTI4MTBaMIGJMQswCQYDVQQGEwJKUDEOMAwGA1UECAwFS3lvdG8xEzARBgNV\n"
            +"BAcMClNlaWthLXRvd24xDTALBgNVBAoMBE5JQ1QxDzANBgNVBAsMBkNsaWVudDE1\n"
            +"MDMGA1UEAwwsY2xpZW50LTIwMjAwNzMwLTAwMS5jbGllbnQubWljc3VzLm5pY3Qu\n"
            +"Z28uanAwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDGENDijTyNqUcJ\n"
            +"2Q7Zkp0Hq//4J8MnzSzLVujtluMcZoZaWXMYwL/OT916iCatLoSGjGujLgjex242\n"
            +"1dA5c4mw6jtbSXfHTbpkaz6P8ieL3i8V5+Zfdo0zSd8VosDKzDJ8fCOmkdEzWLWJ\n"
            +"az9W4Oy0yPaMrG+vI4qA8jZAgg75WhYGHcu/smiJ7tLY7tGqN+xny0L/ivnd2agJ\n"
            +"4tur2zrdswFvERyt7+wDDPsjWK0ov3XM5SiwdSSGjL1k8fRc/6JGz3q8EEFU1fd+\n"
            +"8JSje0Z03gZRNsIOESWzx9xpDvVYni8qgySWfbh8ItzY3XEBxjH7YiW6FfTcfAf9\n"
            +"ZNXYcjPZAgMBAAGjezB5MAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8WHU9wZW5T\n"
            +"U0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBSHEHUUkeOecN11hJwH\n"
            +"pnAUINbNgDAfBgNVHSMEGDAWgBR9U9mngrDmSVdDM/+xFRsXVBXU1zANBgkqhkiG\n"
            +"9w0BAQsFAAOCAQEASZCmMSKoRlyktgVcxE+mJAW4xy3Of1Vs2av3cWKGf7seM5WK\n"
            +"sWYBoD9hsTde2ZyVmbJWH8oQvzzti8iKmJraAx9nyaqKNxkSepDbgCJvG9OXXE1w\n"
            +"iq0/xC8gxdmSpj4g+PNPzttOwfZg/VKm4w2OWUFZAxXS+aAtg+ysTfKRoH5c21WW\n"
            +"Z2fK6x8TzbCxb7ylfXs7JTWTTUagY8msfkj43N0h6QInnoENvW7HI2+4KCP1gCY0\n"
            +"GSN/CDZnmzgQFGCTQFnnEjkvpAcrlJonInIbrYUVh+UOsWQlBHVacYlBAb300T45\n"
            +"Tzs4OTHh3q15IkYSpvjSc9siO+tcl2MOcWg6Pg==\n"
            +"-----END CERTIFICATE-----";

    private static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
            +"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGENDijTyNqUcJ\n"
            +"2Q7Zkp0Hq//4J8MnzSzLVujtluMcZoZaWXMYwL/OT916iCatLoSGjGujLgjex242\n"
            +"1dA5c4mw6jtbSXfHTbpkaz6P8ieL3i8V5+Zfdo0zSd8VosDKzDJ8fCOmkdEzWLWJ\n"
            +"az9W4Oy0yPaMrG+vI4qA8jZAgg75WhYGHcu/smiJ7tLY7tGqN+xny0L/ivnd2agJ\n"
            +"4tur2zrdswFvERyt7+wDDPsjWK0ov3XM5SiwdSSGjL1k8fRc/6JGz3q8EEFU1fd+\n"
            +"8JSje0Z03gZRNsIOESWzx9xpDvVYni8qgySWfbh8ItzY3XEBxjH7YiW6FfTcfAf9\n"
            +"ZNXYcjPZAgMBAAECggEBALDdb0n+yOXPFUpr82DCFUs2gjp8dz5hbvCcyR7wMBm6\n"
            +"S5r+aVLfO+flVWcHkVHwShND7RIoF2+oMnjw8SF8VKYQY7SepEDq3diyE4nCuSGi\n"
            +"4/TxwbvZRzlXa8gML5BoS2TyDXu+lkr9mg3+IO9ZpsXA9rijeo+wcwu2Pk/YU7rA\n"
            +"7tt+QJ4G6TqlTAZasS5ZrVgLLvlKGLqWOjqTh2034bSSwzV8nrGh2WkajF1IjpqE\n"
            +"WtDbQhGXKmlnBq2B6MRcpx7nklRx/ckedwA+r7dk3FTqEiiLBL/QUQZ/hbYjFfnx\n"
            +"LwmUhROiWJRMuhrt0h6MBQVKBy7qAxpnqIr6PvZgpKkCgYEA5GrLNF5PGKqRmz+R\n"
            +"hEL11JtWJT6gxCP8wfabCuYhgzCHnxx16sm1fuUNFUWspcnTwof+KyQNnMZ+mTe9\n"
            +"A4ACosiVmGzVD0Lgav3k5H2diwuhdxqN4L87WYcTYlOMEZicQKfXaacBRDmBiouE\n"
            +"NvuXj8zvX9aL/GpMWQ7Hs1yFUwsCgYEA3fu/X77K7YdW9XWWWMxtE892++k1F1aa\n"
            +"YK/XmlD3ZI93EAMvWNlZ3XK9uVXDbHe+6Lphqabs7sPxMyS+fTGMZNU4u4aZ7VWY\n"
            +"bPPgKakI+pQoGZUkX4hc5v66Nxh5QbVI/oj0VYJqs6kMFPcp+nHWC2pkDi0p/KBM\n"
            +"UGycepLQYysCgYB+93HeuTGER4PKZSpw93uIY5Qd/dMFU++yrW5/P26PatFoOzy8\n"
            +"dsThNVbxZRpTyX1hG5DH0DGU3K0QY/A32tccPx0QbiZ8ZjMypdCuxbhlyuBEwUju\n"
            +"Db5ixHec503rlgX81o8h1kgY8VI1fdnPakhUEZlZj3ueokOwGUweQiaGAwKBgBao\n"
            +"GDtMfOfLdXuuGb9MdiGcfjdt8QZFx5HwwJ+2fdmcrvlKae22mW55xJWDBGQwwKJ+\n"
            +"S3OGXw5rn1Wp/WtHwF2WSakSLunDX8BXD90QrKLutj0ONN0vTNQi0ZzH+bBUMXQR\n"
            +"RGbp3bwil+IX7Afs0HzvyfCMeYmYTtGylDIKhAxrAoGAPOUmeYKmhMI3LXYCv9Ap\n"
            +"G0xVKTFOCk6U2lGIhaNawSXDFMrUiTgQvVbxaoq95ZmakwTpBv3MLmR3cizPNiz2\n"
            +"UUF0FO3durEZDUBkU70XPQ3m+AQ18adzwLPsX4oX7BVk88gzPiVvRFAtoW6+8tck\n"
            +"V//aKvn3DVpfsQsdpuhF61s=\n"
            + "-----END PRIVATE KEY-----";

    private static SSLContext getContext() {
        SSLContext context;
        String password = "";
        try {
            context = SSLContext.getInstance("TLS");

            X509Certificate cert = loadCertificate();
            PrivateKey key = loadPrivateKey();

            String type = KeyStore.getDefaultType();
            KeyStore keystore = KeyStore.getInstance(type);
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

            String algo = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algo);
            kmf.init(keystore, password.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();

            context.init(km, null, null);
        } catch (Exception e) {
            System.out.println(e);
            context = null;
        }
        return context;
    }

    private static PrivateKey loadPrivateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        String privKeyPEM = BEGIN_RSA_PRIVATE_KEY.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");
        System.out.println(privKeyPEM);

        // Base64 decode the data
        byte [] encoded = Base64.decode(privKeyPEM, Base64.DEFAULT);

        // PKCS8 decode the encoded RSA private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);

        return privKey;
    }

    private static X509Certificate loadCertificate() throws CertificateException {
        X509Certificate cert = null;
        CertificateFactory cf = null;
        String certificateString = BEGIN_RSA_CERT;
        try {
            if (certificateString != null && !certificateString.trim().isEmpty()) {
                certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----\n", "")
                        .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
                byte[] certificateData = Base64.decode(certificateString, Base64.DEFAULT);
                cf = CertificateFactory.getInstance("X509");
                cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
            }
        } catch (CertificateException e) {
            throw new CertificateException(e);
        }
        return cert;
    }

    //WS Lister
    private class myWsClientListener extends WebSocketClient {
        public myWsClientListener(URI serverUri) {
            super(serverUri);
        }

        @Override
        //接続
        public void onOpen(ServerHandshake handshakedata) {
            try {
                startSession();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wsStateTextView.setText("Conneted!!");

                }
            });
        }

        @Override
        //Serverからのメッセージの受信
        public void onMessage(final String message) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strNickname = "";
                    String strMessage  = "";
                    String strDate     = "";

                    strMessage = message;
                    JSONObject json = null;
                    JSONObject jsonresult = null;
                    JSONObject jsonparams = null;

                    String id = "";
                    String method = "";

                    try {
                        json = new JSONObject(message);
                        if (json.has("id") ) {
                            id = json.getString("id");
                        }
                        if (json.has("method")) {
                            method = json.getString("method");
                            if (json.has("params")) {
                                jsonparams = json.getJSONObject("params");
                            }
                        }
                        if (json.has("result")) {
                            jsonresult = json.getJSONObject("result");
                        }
                    }
                    catch (org.json.JSONException e) { }

                    strMessage = message;
                    if (jsonparams != null) {
                        // requeset
                        //strMessage += jsonparams.toString();
                        try {
                            parseRequest(method, jsonparams, id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (jsonresult != null) {
                        // response
                        strMessage += jsonresult.toString();
                        try {
                            parseResponse(jsonresult);
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Message msg = new Message( strNickname, strMessage, "", strDate );
                    Log.d(TAG, json.toString());


                    // 拡散されたメッセージをメッセージリストに追加

                }
            });
        }

        /*
        AI->UIのリクエスト
        putEmotion,putText, putResponse, putGesture
         */
        public void parseRequest(String method, JSONObject params, String idstr) throws JSONException {
            Message msg = new Message( method, params.toString(), "" , "");
            String strmsg;
            String sid, id;
            switch (method) {
                case "putEmotion":
                    break;
                case "putText":
                    putTextProcess(params);
                    //m_messagelistadapter.addMessage(msg);
                    break;
                case "putResponse":
                    putResponseProcess(params, idstr);
                    break;
                case "putGesture":
                    break;
                case "putDisplay":
                    putDisplayProcess(params);
                    break;
            }
        }

        public void parseResponse(JSONObject result) throws JSONException, IOException {
            String req = m_curRequest;
            m_curRequest = "";
            switch (req) {
                case "startSession":
                    OnStartSession(result);
                    break;
                case "endSession":
                    OnEndSession(result);
                    break;
                case "parameterGetting":
                    break;
                case "parameterSetting":
                    break;
                case "text2speech":
                    byte[] wavdata = Base64.decode(result.getString("speech"), Base64.DEFAULT);
                    wavPlay(wavdata);
                    break;
            }
        }

        @Override
        //Serverの切断
        public void onClose(int code, String reason, boolean remote) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wsStateTextView.setText("DisConneted..");
                }
            });
        }

        @Override
        //エラー
        public void onError(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void putTextProcess(JSONObject params) throws JSONException {
        String txt = params.getString("text");
        Message msg = new Message( "",  "", txt,  getDateUi());
        //m_messagelistadapter.addMessage(msg);
    }

    private void putDisplayProcess(JSONObject params) throws JSONException {
        switch (params.getString("type")) {
            case "text":
                String txt = params.getString("value");
                addMessageAsync("", txt);
                break;
            case "gesture":
                break;

        }
    }

    private void putResponseProcess(JSONObject params, String id) throws JSONException {
        String strmsg = params.getString("srtext");
        String sid = params.getString("sid");
        int synthesisflag = params.getInt("synthesis_flag");
        int endflag = params.getInt("end_flag");
        JSONArray addition = params.getJSONArray("addition");
 //       Message msg = new Message( "", strmsg, "",  getDate() );
 //       m_messagelistadapter.addMessage(msg);
        String jsonstr =
                "{" +
                        "\"method\":" + "\"" + "putResponse" + "\"," +
                        "\"text\":" + "\"" + strmsg + "\"," +
                        "\"speech\":" + "\"" + addition.get(3) + "\"," +
                        "\"synthesis_flag\":" + "\"" + synthesisflag + "\"," +
                        "\"end_flag\":" + "\"" + endflag + "\"," +
                        "\"sid\":" + "\"" + sid + "\"," +
                        "\"id\":" + "\"" + id + "\"" +
                        "}";
        JSONObject data = new JSONObject(jsonstr);
        m_responsqueue.add(data);
        execSpeech();

        //if (params.getInt("synthesis_flag") == 1) {
//        text2Speech(params.getString("sid"), strmsg);
        //}
        //sendPutResponseResult("uidevice", sid, idstr);
    }

    private void execSpeech() throws JSONException {
        JSONObject data;
        if (m_responsqueue.isEmpty() || m_currentresponse != null) {
            return;
        }
        micmute(true);
        m_currentresponse = m_responsqueue.poll();
        data = m_currentresponse;
        String sid = data.getString("sid");
        String txt = data.getString("text");
        addMessageAsync(txt, "");
        text2Speech(sid, txt);
    }

    private void updateMicMute()
    {
        ImageView myImage= findViewById(R.id.imagemic);
        ListView dialog = findViewById(R.id.listview_messagelist);
        GradientDrawable drawable = (GradientDrawable)dialog.getBackground();
        if (micsts == 1) {
            myImage.setImageResource(R.drawable.icon_microphone_off);
            myImage.setBackgroundColor(Color.RED);
            drawable.setStroke(5, Color.RED);
        } else if (micsts == 0){
            myImage.setImageResource(R.drawable.icon_microphone);
            drawable.setStroke(5, Color.GREEN);
            myImage.setBackgroundColor(Color.GREEN);
        }
    }

    private void updateLayout()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean videodisable = !sharedPreferences.getBoolean("enablevideo", false);

        LinearLayout layout = (LinearLayout)findViewById(R.id.contents_dialog);
        // 内容を全部消す
        layout.removeAllViews();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横向きの場合
            getLayoutInflater().inflate(R.layout.layout_contents_l, layout);
        } else  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 縦向きの場合
            getLayoutInflater().inflate(R.layout.layout_contents_p, layout);
        }
        if (videodisable) {
            View v = findViewById( R.id.imageface );
            v.setVisibility(View.INVISIBLE);
            LinearLayout l = (LinearLayout ) v.getParent();
            l.removeView(v);
        }
        updateMicMute();
        ListView listView = (ListView)findViewById( R.id.listview_messagelist );    // リストビューの取得
        listView.setAdapter( m_messagelistadapter );    // リストビューにビューアダプターをセット
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.smoothScrollToPosition(m_messagelistadapter.getCount());
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString("username", "");
        boolean videodisable = !sharedPreferences.getBoolean("enablevideo", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        m_messagelistadapter = new MessageListAdapter( this ); // ビューアダプターの初期化

        updateLayout();

        SurfaceView surfaceView = findViewById(R.id.surfaceView2);
        rtmpCamera1 = new RtmpCamera1(surfaceView, this, videodisable); // videosolid true:grayscreen
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);
        // 呼び出し元からパラメータ取得
        Bundle extras = getIntent().getExtras();
        m_strUserid = username;
        if( null != extras )
        {
            m_strUserid = extras.getString( EXTRA_USERID );
        }
        guiThreadHandler = new Handler();

        if (sharedPreferences.getBoolean("enabledebug", false)) {
            m_websocketurl = m_websocketurltest;
        }

        // ニックネームの表示
        TextView textviewNickname = (TextView)findViewById( R.id.textview_nickname );
        textviewNickname.setText( m_strUserid );
        wsStateTextView = findViewById(R.id.StateTextView);
        //サーバーの接続準備
        //       WebSocketImpl.DEBUG = true;
        try {
            m_ws = new myWsClientListener(new URI(m_websocketurl));

            SSLContext context = getContext();
            SSLSocketFactory factory = context
                    .getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

            if (m_websocketurl.substring(0,4).equals("wss:")) {
                m_ws.setSocketFactory(factory);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if(!m_ws.isOpen()) {
            m_ws.connect();
        }
    }

    public void addMessageAsync(final String ltext, String rtext){
        guiThreadHandler.post(new Runnable(){
            @Override
            public void run() {
                Message msg = new Message( "", ltext, rtext,  getDateUi() );
                m_messagelistadapter.addMessage(msg);
                ListView listView = (ListView)findViewById( R.id.listview_messagelist );
                listView.smoothScrollToPosition(m_messagelistadapter.getCount());
            }
        });
    }

    @Override
    public void onConnectionStartedRtmp(String rtmpUrl) {
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DialogActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rtmpCamera1.reTry(5000, reason)) {
                    Toast.makeText(DialogActivity.this, "Retry", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(DialogActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                            .show();
                    rtmpCamera1.stopStream();
                }
            }
        });
    }

    @Override
    public void onNewBitrateRtmp(long bitrate) {

    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DialogActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DialogActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DialogActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final int SamplingRate = 16000;

    private void startStream(String url)
    {
        if (!rtmpCamera1.isStreaming()) {
            if (rtmpCamera1.isRecording()
                    || rtmpCamera1.prepareAudio(64*1024, 16000, false)
                    && rtmpCamera1.prepareVideo(640,480, 10, 500000, 2, CameraHelper.getCameraOrientation(this))
            ) {
                rtmpCamera1.startStream(url);
                micmute(false);
            } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopStream()
    {
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
        }
    }

    private void OnStartSession(JSONObject result) throws JSONException {
        if (result.has("streamURL")) {
            String url = result.getString("streamURL");
            url = url.replace(".jp", ".jp:443");
            //url = "rtmps://micsus.nict.go.jp:443/live/73534167-6796-4d04-bbce-13bd8ef356a8?user=nes&pass=nes00";
            //url = "rtmps://micsus.nict.go.jp:443/live/ui-test123?user=nes&pass=nes00";
            //url = "rtmp://10.5.0.165/live/stream";
            Log.d(TAG, "URL:"+ url);
            startStream(url);
            String strmsg = result.getString("srtext");
            String sid = result.getString( "sid");
            int synthesisflag = result.getInt("synthesis_flag");
            int endflag = 0;
            String jsonstr =
                    "{" +
                            "\"method\":" + "\"" + "startSession" + "\"," +
                            "\"text\":" + "\"" + strmsg + "\"," +
                            "\"speech\":" + "\"" + "skip" + "\"," +
                            "\"synthesis_flag\":" + "\"" + synthesisflag + "\"," +
                            "\"end_flag\":" + "\"" + endflag + "\"," +
                            "\"sid\":" + "\"" + sid + "\"," +
                            "\"id\":" + "\"" + "" + "\"" +
                            "}";
            JSONObject data = new JSONObject(jsonstr);
            //Message msg = new Message( "",strmsg, "", getDate());
            //m_messagelistadapter.addMessage(msg);
            m_responsqueue.add(data);
            execSpeech();
        }
    }

    private void OnEndSession(JSONObject result)
    {
        stopStream();
        m_ws.close();
        finish();
    }

    /*
    byteデータを再生する
     */
    private void wavPlay(byte[] wavData) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream input = null;

                // バッファサイズの計算
                int bufSize = android.media.AudioTrack.getMinBufferSize(
                        SamplingRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                // AudioTrack.Builder API level 26より
                AudioTrack audioTrack = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SamplingRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(bufSize)
                        .build();

                // 再生
                audioTrack.play();

                //audioTrack.write(wavData, 0, wavData.length);
                // ヘッダ44byteをオミット
                audioTrack.write(wavData, 44, wavData.length-44);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // マルチスレッドにしたい処理 ここまで
                try {
                    WavPalyEnd();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void WavPalyEnd() throws JSONException {
        if (m_currentresponse != null) {
            if (m_currentresponse.getString("method").equals("putResponse")){
                sendPutResponseResult("uidevice",
                        m_currentresponse.getString("sid"),
                        m_currentresponse.getString("id"));
            }
            if (m_currentresponse.getInt("end_flag") == 1) {
                // 終了
                endSession();
            }
            String strspeech = m_currentresponse.getString("speech");
            if (strspeech.equals("wait")) {
                micmute(false);
            }
            m_currentresponse = null;
        }
        try {
            execSpeech();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     パラメータ取得
      */
    private void getParam(String uid) throws JSONException {
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"parameterGetting\"," +
                        "\"params\":" +
                        "{" +
                        "\"uid\":" + "\"" + uid + "\"," +
                        "}," +
                        "\"id\":" + "\"" + UUID.randomUUID().toString() + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        m_curRequest = request.getString("method");
        SendJson(jsonstr);
    }

    /*
    パラメータ設定
     */
    private void setParam(String uid, String value) throws JSONException {
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"parameterGetting\"," +
                        "\"params\":" +
                        "{" +
                        "\"uid\":" + "\"" + uid + "\"," +
                        "\"date\":" + "\"" + getDate() + "\"" +
                        "\"devcie\":" +
                        "{" +
                        "\"volume\":" + "\"" + m_volume + "\"," +
                        "\"voicetype\":" + "\"" + m_voicetype + "\"," +
                        "\"speed\":" + "\"" + m_speed + "\"," +
                        "\"speechinterval\":" + "\"" + value + "\"," +
                        "}," +
                        "\"emotion_thresholds\":" + "\"" + m_emotion_thresholds + "\"" +
                        "}," +
                        "\"id\":" + "\"" + UUID.randomUUID().toString() + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        m_curRequest = request.getString("method");
        SendJson(jsonstr);
    }


    private void startSession() throws JSONException {
        addMessageAsync(m_strUserid, "");
        m_sidstr = UUID.randomUUID().toString();
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"startSession\"," +
                        "\"params\":" +
                        "{" +
                        "\"uid\":" + "\"" + m_strUserid + "\"," +
                        "\"sid\":" + "\"" + m_sidstr + "\"," +
                        "\"date\":" + "\"" + getDate() + "\"" +
                        "}," +
                        "\"id\":" + "\"" + UUID.randomUUID().toString() + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        m_curRequest = request.getString("method");
        SendJson(jsonstr);
    }

    private void endSession() throws JSONException {
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"endSession\"," +
                        "\"params\":" +
                        "{" +
                        "\"sid\":" + "\"" + m_sidstr + "\"," +
                        "\"date\":" + "\"" + getDate() + "\"" +
                        "}," +
                        "\"id\":" + "\"" + UUID.randomUUID().toString() + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        m_curRequest = request.getString("method");
        SendJson(jsonstr);
    }

    private void sendPutResponseResult(String modules, String sid, String id) throws JSONException {
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"result\":" +
                        "{" +
                        "\"modules\":" + "\"" + modules + "\"," +
                        "\"sid\":" + "\"" + sid + "\"," +
                        "\"date\":" + "\"" + getDate()  + "\"" +
                        "}," +
                        "\"id\":" + "\"" + id + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        SendJson(jsonstr);
    }

    /*
   テキスト->wavリクエスト
    */
    private void text2Speech(String sidstr, String srtext) throws JSONException {
        String jsonstr =
                "{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"text2speech\"," +
                        "\"params\":" +
                        "{" +
                        "\"sid\":" + "\"" + sidstr + "\"," +
                        "\"text\":" + "\"" + srtext+ "\"" +
                        "}," +
                        "\"id\":" + "\"" + UUID.randomUUID().toString() + "\"" +
                        "}";

        JSONObject request = new JSONObject(jsonstr);
        m_curRequest = request.getString("method");
        SendJson(jsonstr);
    }

    private void SendJson(String jsonstr)
    {
        Log.d(TAG, jsonstr);
        m_ws.send(jsonstr);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation; //向き取得
        updateLayout();
//        setContentView(R.layout.activity_dialog);
//        ListView listView = (ListView)findViewById( R.id.listview_messagelist );    // リストビューの取得
//        listView.setAdapter( m_messagelistadapter );    // リストビューにビューアダプターをセット
//        UpdateLayout(orientation); //レイアウト更新
//        setContentView(R.layout.activity_dialog);
    }
    @Override
    protected void onDestroy()
    {
        stopStream();
        try {
            if (m_ws.isOpen()) {
                endSession();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onDestroy();

        m_ws.close();        // 切断
    }

    private void micmute(boolean on)
    {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(on);
        if (on) {
            micsts = 1;
        } else {
            micsts = 0;
        }
        updateMicMute();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //rtmpCamera1.startPreview();
        rtmpCamera1.startPreview(CameraHelper.Facing.FRONT);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
        }
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
        }
        rtmpCamera1.stopPreview();
    }
}