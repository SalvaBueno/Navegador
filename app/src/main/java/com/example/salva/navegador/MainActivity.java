package com.example.salva.navegador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.AutoText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView ed;
    WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ed = (AutoCompleteTextView) findViewById(R.id.editText);
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                ed.setText(view.getUrl());
                super.onPageFinished(view, url);
            }
        });
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);

        ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    buscar(v);
                    handled = true;
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(myWebView.getWindowToken(), 0);
                }
                return handled;
            }
        });
        final Context context = this;
        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AdminSqlLite admin = new AdminSqlLite(context,"historial",null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                String SelectQuery = "select DISTINCT nombre from urlBusqueda where nombre LIKE '%"+ed.getText()+"%'";
                Cursor cursor = db.rawQuery(SelectQuery,null);
                String url[]= new String[cursor.getCount()];
                for(int i=0;cursor.moveToNext();i++){
                    url[i]= cursor.getString(0);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,url);
                ed.setAdapter(adapter);
                //ed.showDropDown();
                db.close();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    public void buscar(View view){

        int protocolo = ed.getText().toString().indexOf("http://");
        if(protocolo>=0) {
            myWebView.loadUrl(ed.getText().toString());
        } else{
            myWebView.loadUrl("http://"+ed.getText().toString());
        }
        registroURL(ed.getText().toString());
        ed.setText(myWebView.getUrl());
    }

   private void registroURL(String url) {
        AdminSqlLite admin = new AdminSqlLite(this,"historial",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("nombre",url);
        db.insert("urlBusqueda",null,registro);
        db.close();
        Toast.makeText(this,"Registrado en la Base de datos",Toast.LENGTH_SHORT).show();
    }

    public void historial(){
        AdminSqlLite admin = new AdminSqlLite(this,"historial",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        String SelectQuery = "select nombre from urlBusqueda ORDER BY codigo ASC";
        Cursor cursor = db.rawQuery(SelectQuery,null);
        String url[]= new String[cursor.getCount()];
        for(int i=0;cursor.moveToNext();i++) {
            url[i] = cursor.getString(0);
        }
        db.close();
    }


    public void onBackPressed(){
        atras(myWebView);
    }
    public void atras(View view){
        if(myWebView.canGoBack()){
            myWebView.goBack();
        }
    }


}
