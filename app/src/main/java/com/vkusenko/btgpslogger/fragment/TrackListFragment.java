package com.vkusenko.btgpslogger.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.vkusenko.btgpslogger.R;
import com.vkusenko.btgpslogger.activity.GoogleActivity;
import com.vkusenko.btgpslogger.common.AlertCollection;
import com.vkusenko.btgpslogger.common.FileManager;
import com.vkusenko.btgpslogger.events.ListFileEvent;
import com.vkusenko.btgpslogger.common.NetworkState;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrackListFragment extends ListFragment {

    private List<File> listFiles;
    private List<String> listNameFiles;
    private ArrayAdapter<String> adapter;

    private Button btnTrackListDelete;
    private Button btnTrackListExport;
    private CheckBox cbAllListTrack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.track_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListFiles();
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, listNameFiles);
        setListAdapter(adapter);

        btnTrackListDelete = (Button) getActivity().findViewById(R.id.btnTrackListDelete);
        btnTrackListDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteFiles();
            }
        });

        btnTrackListExport = (Button) getActivity().findViewById(R.id.btnTrackListExport);
        btnTrackListExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExportFile();
            }
        });

        cbAllListTrack = (CheckBox) getActivity().findViewById(R.id.cbAllListTrack);
        cbAllListTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(int i = 0; i < adapter.getCount(); i++)
                    getListView().setItemChecked(i, isChecked);
            }
        });
    }

    private boolean checkSelected() {
        if (getSelectedFiles().size() > 0) {
            return true;
        } else {
            AlertCollection.simple(getActivity(),
                    getString(R.string.error),
                    getString(R.string.no_files_selected),
                    getString(R.string.ok));
            return false;
        }
    }


    private void onDeleteFiles() {
        if (checkSelected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.confirm))
                    .setMessage(getString(R.string.confirm_delete))
                    .setNegativeButton(getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteFiles();
                                    listRefresh();
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void onExportFile() {
        if (checkSelected()) {
            if (NetworkState.isOnline(getActivity().getApplicationContext())) {
                EventBus.getDefault().postSticky(new ListFileEvent(getSelectedFiles()));
                Intent intent = new Intent(getActivity().getApplicationContext(), GoogleActivity.class);
                startActivity(intent);
            } else {
                AlertCollection.simple(getActivity(),
                        getString(R.string.error),
                        getString(R.string.network_disable),
                        getString(R.string.ok));
            }
        }
    }

    public void listRefresh() {
        getListFiles();
        adapter.clear();
        getListView().clearChoices();
        adapter.addAll(listNameFiles);
        cbAllListTrack.setChecked(false);
    }

    private void deleteFiles() {
        for (File file : getSelectedFiles())
            file.delete();
    }

    private List<File> getSelectedFiles() {
        List<File> listSelectedFiles = new ArrayList<File>();
        List<Integer> selected = getSelected();
        if (selected.size() > 0) {
            for (int i : selected ) {
                listSelectedFiles.add(listFiles.get(i));
            }
        }
        return listSelectedFiles;
    }

    private void getListFiles() {
        FileManager fileManager = new FileManager(getActivity());
        File dir = fileManager.getDir();
        listFiles = new ArrayList<File>();
        listNameFiles = new ArrayList<String>();
        for (File file : dir.listFiles()) {
            listFiles.add(file);
            listNameFiles.add(file.getName());
        }
    }

    private List<Integer> getSelected() {
        List<Integer> selected = new ArrayList<Integer>();
        SparseBooleanArray sbArray = getListView().getCheckedItemPositions();
        for (int i = 0; i < sbArray.size(); i++) {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key))
                selected.add(key);
        }
        return selected;
    }
}
