package io.github.junheah.jsp.model.source;

public class Source {
    String name;
    public Source(String name){
        this.name = name;
    }

    public Search getSearch(String query){
        return new Search(this, query);
    }
}
