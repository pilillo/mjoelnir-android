package at.aau.nes.mjoelnir.webView;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import at.aau.nes.mjoelnir.MainActivity;
import at.aau.nes.mjoelnir.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Analytics extends Fragment {


    public Analytics() {
        // Required empty public constructor
    }

    public static Analytics newInstance(){
        return new Analytics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_analytics, container, false);

        WebView mWebView = new WebView(getContext());

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.loadUrl(getString(R.string.webpage_server_url)+"?authkey=" + MainActivity.authentificationKey );

        return mWebView;
    }

}
