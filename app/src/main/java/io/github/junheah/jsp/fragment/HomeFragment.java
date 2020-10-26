package io.github.junheah.jsp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;

import static io.github.junheah.jsp.Utils.singleInputPopup;

public class HomeFragment extends CallbackFragment {
    PlayListItemClickCallback playListCallback; //used when adding playlists

    public HomeFragment(PlayListItemClickCallback callback){
        this.playListCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
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
                            //create playlist instance
                            PlayList pl = new PlayList(data);

                            //create playlist fragment and set callbacks
                            PlayListFragment fragment = new PlayListFragment(pl, playListCallback);
                            fragment.setAdapterCallback(fragmentAdapterCallback);

                            //add to adapter
                            fragmentAdapterCallback.addItem(fragment);

                            //save
                            new PlayListIO(getContext()).write(pl);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
