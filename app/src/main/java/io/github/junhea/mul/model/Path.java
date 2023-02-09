package io.github.junhea.mul.model;

import androidx.annotation.Nullable;

public class Path {
    String path;
    boolean r;
    public Path(String path, boolean r){
        this.path = path;
        this.r = r;
    }
    public Path(String path){
        this.path = path;
        this.r = false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        //todo: path 중복 (recursive는 다름), 혹은 새로 추가한 path가 포함하는 것이 이미 있을때, 처리 방법
        if(obj instanceof Path){
            Path p = (Path) obj;
            if(this.path.equals(p.path) && this.r == p.r) return true;
            if(p.r){
                if(this.path.indexOf(p.path) == 0) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return path + (r ? "*" : "");
    }
}
