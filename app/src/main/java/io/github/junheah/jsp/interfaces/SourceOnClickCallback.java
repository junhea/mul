package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.viewHolder.SourceItem;

public interface SourceOnClickCallback {
    void download(SourceItem item);
    void delete(SourceItem item);
}
