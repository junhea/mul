package io.github.junheah.jsp.model.source;

import javax.crypto.spec.PSource;

public class Search {
    Source source;
    String query;
    int page = -1;

    public Search(Source source, String query){
        this.source = source;
        this.query = query;
    }

    public void fetch(){

    }
}
