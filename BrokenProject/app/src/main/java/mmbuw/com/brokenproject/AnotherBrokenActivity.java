package mmbuw.com.brokenproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import mmbuw.com.brokenproject.R;

public class AnotherBrokenActivity extends Activity {

    private EditText userInput;
    private TextView responseText;
    private WebView webResponse;
    private String responseAsString;
    private String message;

    private static final String TAG = "AnotherBrokenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_broken);

        Intent intent = getIntent();
        String message = intent.getStringExtra(BrokenActivity.EXTRA_MESSAGE);

        userInput = (EditText) findViewById(R.id.editText2);

        responseText = (TextView) findViewById(R.id.serverResponse);
        responseText.setMovementMethod(new ScrollingMovementMethod());

        webResponse = (WebView) findViewById(R.id.webDisplay);
        webResponse.getSettings().setJavaScriptEnabled(true);
        webResponse.setVerticalScrollBarEnabled(true);
        webResponse.setHorizontalScrollBarEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.another_broken, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchHTML(View view) throws IOException {

        // Thread for networking
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestedURL = userInput.getText().toString();
                System.out.println("The user wrote: "+ requestedURL);
                try {
                    // Beginning of helper code for HTTP Request.
                    HttpParams httpParameters = new BasicHttpParams();
                    // In case of timeout, the limit is 5 seconds
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                    HttpClient client = new DefaultHttpClient(httpParameters);
                    //HttpResponse response = client.execute(new HttpGet("http://lmgtfy.com/?q=android+ansync+task"));
                    HttpResponse response = client.execute(new HttpGet(requestedURL));
                    StatusLine status = response.getStatusLine();

                    if (status.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        response.getEntity().writeTo(outStream);
                        responseAsString = outStream.toString();
                        System.out.println("Response string: " + responseAsString);

                        // The view can only be accessed on the UI thread
                        AnotherBrokenActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display as plain text
                                responseText.setText(responseAsString);
                            }
                        });

                    } else {
                        //Well, this didn't work.
                        response.getEntity().getContent().close();
                        throw new IOException(status.getReasonPhrase());
                    }
                } catch (ConnectException exception) {
                    // Connection refused
                    toastMessage("ConnectException", exception.getMessage());
                } catch (UnknownHostException exception) {
                    // Unable to resolve host
                    toastMessage("UnknownHostException", exception.getMessage());
                } catch (IOException exception) {
                    // Connection time out, server failed to respond, authorization required
                    toastMessage("IOException", exception.getMessage());
                } catch (OutOfMemoryError exception) {
                    // e.g. link to a big executable file download
                    toastMessage("OutOfMemoryError", exception.getMessage());
                }
                catch (Exception exception) {
                    // Other errors
                    toastMessage(exception.getClass().toString(), exception.getMessage());
                }
            }
        }).start();
    }

    // Display of exception errors with Toast
    public void toastMessage(String exceptionName, String exceptionMessage) {

        message = "An error of " + exceptionName + " occurred:\n " + exceptionMessage;

        AnotherBrokenActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Log.e(TAG, message);
                message = message + "\nCheck log for more information.";
                Toast toast = Toast.makeText(context, message, duration);
                toast.show();
            }
        });

    }

    // Bonus task: display of server response with web view for proper images, HTML, etc
    public void webDisplay(View view) {
        String requestedURL = userInput.getText().toString();

        System.out.println("The user wrote: "+ requestedURL);
        responseText.setText(" ");

        webResponse.loadUrl(requestedURL);
    }
}
