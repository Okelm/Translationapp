package com.bwidlarz.translationapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    EditText translateEditText;
    private Locale currentSpokenLang = Locale.US;
    private TextToSpeech textToSpeech;
    private Spinner languageSpinner;
    private int spinnerIndex = 0;
    private String[] arrayOfTranslations;


    private Locale locSpanish = new Locale("es", "MX");
    private Locale locRussian = new Locale("ru", "RU");
    private Locale locPortuguese = new Locale("pt", "BR");
    private Locale locDutch = new Locale("nl", "NL");

    private Locale[] languages = {locDutch, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN, locPortuguese, locRussian, locSpanish};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSpinner = (Spinner) findViewById(R.id.spinner);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
                currentSpokenLang = languages[index];
                spinnerIndex = index;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        textToSpeech = new TextToSpeech(this, this);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);


        if (!isEmpty(translateEditText)) {
            Toast.makeText(this, "Getting Translations", Toast.LENGTH_LONG).show();
            new SaveTheFeed().execute();
        } else {
            Toast.makeText(this, "Enter Words to Translate", Toast.LENGTH_LONG).show();
        }

    }

    protected boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;

    }

    @Override
    public void onInit(int status) {

        if (status == textToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(currentSpokenLang);
            if (result == textToSpeech.LANG_MISSING_DATA || result == textToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(this, "Language Not Supported", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Text to Speech Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void readTheText(View view) {
        textToSpeech.setLanguage(currentSpokenLang);

        if (arrayOfTranslations.length >= 9) {
            textToSpeech.speak(arrayOfTranslations[spinnerIndex + 4], TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(this, "Translate Text First", Toast.LENGTH_SHORT).show();
        }
    }

    class GetXMLData extends AsyncTask<Void, Void, Void>{

        String stringToPrint = "";

        @Override
        protected Void doInBackground(Void... voids) {

            String xmlString = "";

            String wordsToTranslate = "";

            EditText translateEditText = (EditText) findViewById(R.id.editText);

            wordsToTranslate = translateEditText.getText().toString();

            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=xmltranslations&english_words=" + wordsToTranslate);

            httpPost.setHeader("Content-type", "text/xml");

            InputStream inputStream = null;

            try{

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line = null;

                while((line = reader.readLine()) != null){

                    sb.append(line);


                }

                xmlString = sb.toString();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);

                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(xmlString));

                int eventType = xpp.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){

                    if((eventType == XmlPullParser.START_TAG) && (!xpp.getName().equals("translations"))){

                        stringToPrint = stringToPrint + xpp.getName() + " : ";


                    } else if(eventType == XmlPullParser.TEXT){

                        stringToPrint = stringToPrint + xpp.getText() + "\n";

                    }

                    eventType = xpp.next();

                }


            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView translateTextView = (TextView) findViewById(R.id.TranslationTextView);

            // Make the TextView scrollable
            translateTextView.setMovementMethod(new ScrollingMovementMethod());

            // Eliminate the "language :" part of the string for the
            // translations
            String stringOfTranslations = stringToPrint.replaceAll("\\w+\\s:","#");

            // Store the translations into an array
            arrayOfTranslations = stringOfTranslations.split("#");

            translateTextView.setText(stringToPrint);

        }

    }





    class SaveTheFeed extends AsyncTask<String, Void, Void> {

        String jsonString = "";
        String result = "";
        String wordsToTranslate;

        protected void onPreExecute() {
            super.onPreExecute();

            EditText translateEditText = (EditText) findViewById(R.id.editText);

            String wordsToTranslate = translateEditText.getText().toString();

        }


        @Override
        protected Void doInBackground(String... params) {

            wordsToTranslate = wordsToTranslate.replace("", "+");

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words=" + wordsToTranslate);

            httpPost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;

            try {

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line = null;

                while ((line = reader.readLine()) != null) {

                    sb.append(line + "\n");

                }

                jsonString = sb.toString();

                JSONObject jObject = new JSONObject(jsonString);

                JSONArray jArray = jObject.getJSONArray("translation");

                outputTranslations(jArray);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void outputTranslations(JSONArray jsonArray) {

            String[] languages = {"arabic", "chinese", "danish", "dutch",
                    "french", "german", "italian", "german", "portuguese", "russian", "spanish"};

            try {
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject translationObject =
                            jsonArray.getJSONObject(i);

                    result = result + languages[i] + " : " +
                            translationObject.getString(languages[i]);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView translation_text_view = (TextView) findViewById(R.id.TranslationTextView);
            translation_text_view.setText(result);


            // Make the TextView scrollable
            translation_text_view.setMovementMethod(new ScrollingMovementMethod());

            // Eliminate the "language :" part of the string for the
            // translations
            String stringOfTranslations = result.replaceAll("\\w+\\s:", "#");

            // Store the translations into an array
            arrayOfTranslations = stringOfTranslations.split("#");

            translation_text_view.setText(result);
            //  - See more at: http://www.newthinktank.com/2014/11/make-android-apps-16/#sthash.ryv9A2mZ.dpuf
        }


    }

    public void ExceptSpeakInput(View view) {

        // Starts an Activity that will convert speech to text
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Use a language model based on free-form speech recognition
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Recognize speech based on the default speech of device
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Prompt the user to speak
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_input_phase));

        try {

            startActivityForResult(intent, 100);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(this, getString(R.string.stt_not_supported_message), Toast.LENGTH_LONG).show();

        }

    }

    // The results of the speech recognizer are sent here
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 100 is the request code sent by startActivityForResult
        if ((requestCode == 100) && (data != null) && (resultCode == RESULT_OK)) {

            // Store the data sent back in an ArrayList
            ArrayList<String> spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            EditText wordsEntered = (EditText) findViewById(R.id.editText);

            // Put the spoken text in the EditText
            wordsEntered.setText(spokenText.get(0));

        }

    }
}
