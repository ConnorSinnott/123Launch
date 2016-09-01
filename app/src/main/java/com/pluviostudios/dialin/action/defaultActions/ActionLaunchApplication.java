package com.pluviostudios.dialin.action.defaultActions;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragmentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by spectre on 8/10/16.
 */
public class ActionLaunchApplication extends Action {

    public static final String TAG = "ActionLaunchApplication";

    private static final int INDEX_APPLICATION_PACKAGE_NAME = 0;
    private static final int INDEX_APPLICATION_NAME = 1;
    private static final int INDEX_APPLICATION_ICON_URI = 2;

    private String mApplicationName = null;
    private Uri mApplicationIcon = null;

    @Override
    public int getActionId() {
        return 2;
    }

    @Override
    public String getActionName() {
        if (mApplicationName == null) {
            return "Launch Application";
        } else {
            return getContext().getString(R.string.launch, mApplicationName);
        }
    }

    @Override
    public Uri getActionImageUri() {
        if (mApplicationIcon == null) {
            return ActionTools.convertResourceToUri(getContext(), R.drawable.ic_blaunch);
        } else {
            return mApplicationIcon;
        }
    }

    @Override
    public boolean onExecute() {

        Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(getActionParameters().get(INDEX_APPLICATION_PACKAGE_NAME));
        getContext().startActivity(launchIntent);
        return true;

    }

    @Override
    public void setParameters(ArrayList<String> actionParameters) {
        super.setParameters(actionParameters);
        if (actionParameters.size() > 0) {
            mApplicationName = getActionParameters().get(INDEX_APPLICATION_NAME);
            mApplicationIcon = Uri.parse(getActionParameters().get(INDEX_APPLICATION_ICON_URI));
        }
    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return new LaunchApplicationConfigurationFragment();
    }

    public static class LaunchApplicationConfigurationFragment extends ConfigurationFragment {

        private static final int DIALOG_REQUEST_CODE = 2134;

        private View mRoot;
        private ProgressBar mProgressBar;
        private ApplicationInfo selectedInfo = null;
        private ArrayList<ApplicationInfo> applicationInfoList = new ArrayList<>();

        @Override
        public int getParentActionId() {
            return 2;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            mRoot = inflater.inflate(R.layout.fragment_launch_application_configure, container, false);
            mProgressBar = (ProgressBar) mRoot.findViewById(R.id.fragment_launch_application_configure_progress);

            new AsyncGetApplicationInfo(getContext()).execute();

            return mRoot;

        }

        @Override
        public void onStart() {
            super.onStart();
            EventBus.getDefault().register(this);
        }

        @Override
        public void onStop() {
            EventBus.getDefault().unregister(this);
            super.onStop();
        }

        private class AsyncGetApplicationInfo extends AsyncTask<Void, Void, ArrayList<ApplicationInfo>> {

            private Context mContext;

            public AsyncGetApplicationInfo(Context context) {

                mContext = context;
            }

            @Override
            protected ArrayList<ApplicationInfo> doInBackground(Void... voids) {

                ArrayList<ApplicationInfo> applicationInfoList = new ArrayList<>(mContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));

                // Filter Application info list to remove system applications and mark previous selection to be selected on init
                ArrayList<ApplicationInfo> listItems = new ArrayList<>();
                while (applicationInfoList.size() > 0) {

                    ApplicationInfo appInfo = applicationInfoList.remove(0);
                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || appInfo.icon == 0) {
                        continue;
                    }

                    listItems.add(appInfo);

                }

                // Get Application info list and sort it alphabetically
                Collections.sort(applicationInfoList, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo applicationInfo, ApplicationInfo t1) {
                        String leftName = ActionTools.getForeignApplicationNameFromInfo(mContext, applicationInfo);
                        String rightName = ActionTools.getForeignApplicationNameFromInfo(mContext, t1);
                        return leftName.toLowerCase().charAt(0) < rightName.toLowerCase().charAt(0) ? -1 : 1;
                    }
                });

                return listItems;

            }

            @Override
            protected void onPostExecute(ArrayList<ApplicationInfo> applicationInfoArrayList) {

                showApplicationList(applicationInfoArrayList);
            }

        }

        private void showApplicationList(ArrayList<ApplicationInfo> applicationInfoList) {

            this.applicationInfoList = applicationInfoList;

            IconListDialogFragment.Builder builder = new IconListDialogFragment.Builder(DIALOG_REQUEST_CODE);

            for (ApplicationInfo x : applicationInfoList) {
                String applicationName = ActionTools.getForeignApplicationNameFromInfo(getContext(), x);
                Uri applicationUri = ActionTools.getForeignApplicationImageUriFromInfo(x);
                builder.addItem(applicationName, applicationUri);
            }

            mProgressBar.setVisibility(View.GONE);

            getChildFragmentManager().beginTransaction().replace(R.id.fragment_launch_application_configure_frame, builder.build()).commit();

        }

        @Subscribe
        public void onIconListDialogFragmentEvent(IconListDialogFragmentEvent event) {

            if (event.requestCode == DIALOG_REQUEST_CODE) {
                selectedInfo = applicationInfoList.get(event.position);
            }

        }

        @Override
        public ArrayList<String> getActionParameters() {
            ArrayList<String> out = new ArrayList<>();
            if (selectedInfo != null) {
                out.add(selectedInfo.packageName);
                out.add(ActionTools.getForeignApplicationNameFromInfo(getContext(), selectedInfo));
                out.add(ActionTools.getForeignApplicationImageUriFromInfo(selectedInfo).toString());
            } else {
                Bundle arguments = getArguments();
                if (arguments.containsKey(EXTRA_PARAMETERS_ARRAY)) {
                    return arguments.getStringArrayList(EXTRA_PARAMETERS_ARRAY);
                }
            }
            return out;
        }

    }


}

