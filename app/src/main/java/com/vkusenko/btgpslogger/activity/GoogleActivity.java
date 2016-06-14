package com.vkusenko.btgpslogger.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.vkusenko.btgpslogger.R;
import com.vkusenko.btgpslogger.events.ListFileEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.List;

public class GoogleActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private DriveId appFolderId = null;
    private String appFolderName;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private List<File> listFiles;
    private int countFile;

    private EditText editGoogleDriveNameFolder;
    private ProgressDialog progressDialog;
    private Button btnGoogleDriveUpload;

    private enum Action {
        CREATE_FOLDER,
        CREATE_FILE
    }
    private Action actionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        editGoogleDriveNameFolder = (EditText) findViewById(R.id.editGoogleDriveNameFolder);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editGoogleDriveNameFolder.setText(sharedPreferences.getString("google_folder_name", getString(R.string.folder_name_default)));

        btnGoogleDriveUpload = (Button) findViewById(R.id.btnGoogleDriveUpload);
        btnGoogleDriveUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpload();
            }
        });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ListFileEvent event) {
        listFiles = event.listFiles;
        countFile = listFiles.size();
    }

    private void onUpload() {
        appFolderName = editGoogleDriveNameFolder.getText().toString();
        if(appFolderName == null || appFolderName.isEmpty())
            appFolderName = getString(R.string.folder_name_default);
        showProgressDialog();
        startUpload();
    }

    private void startUpload() {
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"),
                        Filters.eq(SearchableField.TITLE, appFolderName)))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(
                new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.problem_while_retrieving_files), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Iterator<Metadata> metadataIterator = result.getMetadataBuffer().iterator();
                        if (metadataIterator.hasNext()) {
                            appFolderId = metadataIterator.next().getDriveId();
                            createFile();
                        } else {
                            actionType = Action.CREATE_FOLDER;
                            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                    .setResultCallback(driveContentsCallback);
                        }
                    }
                });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading_in_process));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(countFile);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(getApplicationContext(), getString(R.string.upload_canceled), Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.show();
    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
        new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (result.getStatus().isSuccess()) {
                    switch (actionType) {
                        case CREATE_FOLDER:
                            createFolderOnGoogleDrive();
                            break;
                        case CREATE_FILE:
                                createFileOnGoogleDrive(result);
                            break;
                    }
                }
            }
        };

    private void createFile() {
        actionType = Action.CREATE_FILE;
        for (int i = 0; i < countFile; i++) {
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsCallback);
        }
    }

    private void createFileOnGoogleDrive(DriveApi.DriveContentsResult result){
        final DriveContents driveContents = result.getDriveContents();
        InputStream is;
        OutputStream os;
        File file = listFiles.get(progressDialog.getProgress());

        try {
            is = new FileInputStream(file);
            os = driveContents.getOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(file.getName())
                .setMimeType(getMimeType(file.getName()))
                .setStarred(true).build();

        DriveFolder folder = appFolderId.asDriveFolder();
        folder.createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(
                        new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    Toast.makeText(getApplicationContext(), "file not created !"
                                            , Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }
                );

        progressDialog.setIndeterminate(false);
        progressDialog.incrementProgressBy(1);
        if (progressDialog.getProgress() == progressDialog.getMax())
            progressDialog.dismiss();
    }

    private void createFolderOnGoogleDrive() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(appFolderName).build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(
            new ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_while_trying_to_create_the_folder), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    appFolderId = result.getDriveFolder().getDriveId();
                    createFile();
                }
            }
        );
    }



    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        }

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        btnGoogleDriveUpload.setEnabled(true);
        Toast.makeText(this, getString(R.string.connected), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.google_connection_failed) + connectionResult.toString(), Toast.LENGTH_SHORT);

        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith("kml")) {
            return "application/vnd.google-earth.kml+xml";
        }

        if (fileName.endsWith("gpx")) {
            return "application/gpx+xml";
        }

        if (fileName.endsWith("zip")) {
            return "application/zip";
        }

        if (fileName.endsWith("xml")) {
            return "application/xml";
        }

        if (fileName.endsWith("nmea")) {
            return "text/plain";
        }

        return "application/vnd.google-apps.spreadsheet";
    }

}
