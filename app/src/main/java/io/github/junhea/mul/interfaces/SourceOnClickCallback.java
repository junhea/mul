package io.github.junhea.mul.interfaces;

import io.github.junhea.mul.model.viewHolder.SourceItem;

public interface SourceOnClickCallback {
    void download(SourceItem item);
    void delete(SourceItem item);
}
