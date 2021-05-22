package com.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLBuilder {
    private final String URLBase = "https://search.maven.org/solrsearch/select?q=fc:";
    private final String URLSuffix = "&rows=100&wt=json";
    private URL requestURL;

    public URLBuilder(String importedClass) throws MalformedURLException {

        this.requestURL = new URL(URLBase.concat(importedClass).concat(URLSuffix));
    }

    public URL getRequestURL() {
        return this.requestURL;
    }

}
