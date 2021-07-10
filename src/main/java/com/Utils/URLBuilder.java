package com.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLBuilder {
    private final String URLBase = "https://search.maven.org/solrsearch/select?q=";
    private final String URLSuffix = "&rows=100&wt=json";
    private URL requestURL;

    public URLBuilder(String query) throws MalformedURLException {

        this.requestURL = new URL(URLBase.concat(query).concat(URLSuffix));
    }

    public URL getRequestURL() {
        return this.requestURL;
    }

}
