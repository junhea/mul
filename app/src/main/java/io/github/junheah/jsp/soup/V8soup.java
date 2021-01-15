package io.github.junheah.jsp.soup;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class V8soup {
    List<Object> data;
    V8 v8;

    public V8soup(V8 runtime){
        this.v8 = runtime;
        data = new ArrayList<>();
    }

    public int parse(String html){
        //returns Document pointer
        Document res = Jsoup.parse(html);
        int pointer = data.size();
        data.add(res);
        return pointer;
    }

    public int selectFirst(Integer dp, String query){
        //returns element pointer
        Object obj = data.get(dp);
        Element res;
        if(obj instanceof Document){
            res = ((Document)obj).selectFirst(query);
        }else if(obj instanceof Element){
            res = ((Element)obj).selectFirst(query);
        }else{
            return -1;
        }
        if(res == null){
            return -1;
        }
        int pointer = data.size();
        data.add(res);
        return pointer;
    }

    public V8Array select(Integer dp, String query){
        //returns element pointer
        V8Array res = new V8Array(v8);
        Object obj = data.get(dp);
        Elements es;
        if(obj instanceof Document){
            es = ((Document)obj).select(query);
        }else if(obj instanceof Element){
            es = ((Element)obj).select(query);
        }else{
            return res;
        }

        for(Element e : es){
            res.push(data.size());
            data.add(e);
        }
        return res;
    }

    public void reset(){
        data.clear();
    }

    public String ownText(Integer ep){
        Element e = (Element)data.get(ep);
        return e.ownText();
    }

    public String attr(Integer p, String key){
        Element e = (Element)data.get(p);
        return e.attr(key);
    }
}
