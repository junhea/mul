package io.github.junheah.jsp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.junheah.jsp.Activity.DebugActivity;
import io.github.junheah.jsp.Activity.MainActivity;
import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;

import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;

public class HomeFragment extends CallbackFragment {
    PlayListItemClickCallback playListCallback; //used when adding playlists

    public HomeFragment(){
        //don't do anything
    }

    public static final HomeFragment newInstance() {
        HomeFragment f = new HomeFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        playListCallback = ((MainActivity)getActivity()).getPlayListCallback();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.home_add_playlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fragmentAdapterCallback != null){
                    singleInputPopup(getContext(), new StringCallback() {
                        @Override
                        public void callback(String data) {
                            //playlist io
                            PlayListIO io =  new PlayListIO(getContext());

                            if(io.getNames().contains(data)){
                                //duplicate
                                showPopup(getContext(),data,"이 플레이리스트는 이미 존재합니다");
                            } else {
                                //create playlist instance
                                PlayList pl = new PlayList(data);

                                //create playlist fragment and set callbacks
                                PlayListFragment fragment = PlayListFragment.newInstance(pl);
                                fragment.setAdapterCallback(fragmentAdapterCallback);

                                //add to adapter
                                fragmentAdapterCallback.addItem(fragment);

                                //save
                                io.write(pl);
                            }
                        }
                    });
                }
            }
        });

        view.findViewById(R.id.home_add_local_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        view.findViewById(R.id.home_add_online_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_debug:
                startActivity(new Intent(getContext(), DebugActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);
    }
}
