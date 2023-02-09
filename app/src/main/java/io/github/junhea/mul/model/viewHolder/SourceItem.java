package io.github.junhea.mul.model.viewHolder;

import androidx.annotation.Nullable;

import static io.github.junhea.mul.adapter.SourceAdapter.AVAILABLE;

public class SourceItem{
    public String name;
    public int status;
    public String url;
    public SourceItem(String name, int status) {
        this.name = name;
        this.status = status;
    }
    public SourceItem(String name, String url) {
        this.name = name;
        this.status = AVAILABLE;
        this.url = url;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof SourceItem){
            return ((SourceItem)obj).name.equals(name);
        }
        return false;
    }
}
