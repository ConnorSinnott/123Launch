package com.pluviostudios.dialin.action.defaultActions;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by spectre on 8/10/16.
 */
public class ActionLaunchApplication extends Action {

    // TODO What happens when the user removes the application

    public static final String TAG = "ActionLaunchApplication";

    private static final int INDEX_APPLICATION_PACKAGE_NAME = 0;
    private static final int INDEX_APPLICATION_NAME = 1;
    private static final int INDEX_APPLICATION_ICON_URI = 2;

    private String mApplicationName = null;
    private DialinImage mApplicationIcon = null;

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
    public DialinImage getActionImage() {
        if (mApplicationIcon == null) {
            return new DialinImage(getContext(), R.drawable.ic_blaunch);
        } else {
            return mApplicationIcon;
        }
    }

    @Override
    public boolean onExecute() {

        Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(getActionArguments().get(INDEX_APPLICATION_PACKAGE_NAME));
        getContext().startActivity(launchIntent);
        return true;

    }

    @Override
    public void setActionArguments(ArrayList<String> actionArguments) {
        super.setActionArguments(actionArguments);
        mApplicationName = getActionArguments().get(INDEX_APPLICATION_NAME);
        mApplicationIcon = new DialinImage(Uri.parse(getActionArguments().get(INDEX_APPLICATION_ICON_URI)));
    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return new LaunchApplicationConfigurationFragment();
    }

    public static class LaunchApplicationConfigurationFragment extends ConfigurationFragment {

        ApplicationInfo selectedInfo = null;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable ArrayList<String> savedActionArguments) {

            // Generate list fragment
            IconListDialogFragment<ApplicationInfo> listDialogFragment = new IconListDialogFragment<>();

            // Get Application info list and sort it alphabetically
            ArrayList<ApplicationInfo> applicationInfoList = new ArrayList<>(getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));
            Collections.sort(applicationInfoList, new Comparator<ApplicationInfo>() {
                @Override
                public int compare(ApplicationInfo applicationInfo, ApplicationInfo t1) {
                    String leftName = ActionTools.getForeignApplicationNameFromInfo(getContext(), applicationInfo);
                    String rightName = ActionTools.getForeignApplicationNameFromInfo(getContext(), t1);
                    return leftName.toLowerCase().charAt(0) < rightName.toLowerCase().charAt(0) ? -1 : 1;
                }
            });


            String savedAppPackageName = null;
            if (savedActionArguments != null) {
                savedAppPackageName = savedActionArguments.get(INDEX_APPLICATION_PACKAGE_NAME);
            }

            // Filter Application info list to remove system applications and mark previous selection to be selected on init
            ArrayList<ApplicationInfo> listItems = new ArrayList<>();
            while (applicationInfoList.size() > 0) {

                ApplicationInfo appInfo = applicationInfoList.remove(0);
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || appInfo.icon == 0) {
                    continue;
                }

                listItems.add(appInfo);

                if (savedAppPackageName != null) {
                    if (appInfo.packageName.equals(savedAppPackageName)) {
                        listDialogFragment.setSelected(listItems.size());
                        savedAppPackageName = null;
                    }
                }

            }

            // Todo The saved application was removed. This probably isn't the best place to deal with this
            if (savedAppPackageName != null) {

            }

            listDialogFragment.setItems(listItems);
            listDialogFragment.setItemAdapter(new IconListDialogFragment.IconListDialogItemAdapter<ApplicationInfo>() {
                @Override
                public String getString(ApplicationInfo object) {
                    return ActionTools.getForeignApplicationNameFromInfo(getContext(), object);
                }

                @Override
                public Uri getImageUri(ApplicationInfo object) {
                    return ActionTools.getForeignApplicationImageUriFromInfo(object);
                }
            });
            listDialogFragment.setOnListItemSelected(new IconListDialogFragment.OnListItemSelected<ApplicationInfo>() {
                @Override
                public void onListItemSelected(ApplicationInfo object, int position) {
                    selectedInfo = object;
                }
            });

            return listDialogFragment.onCreateView(inflater, container, null);
        }

        @Override
        public ArrayList<String> getActionArguments() {
            ArrayList<String> out = new ArrayList<>();

            if (selectedInfo != null) {
                out.add(selectedInfo.packageName);
                out.add(ActionTools.getForeignApplicationNameFromInfo(getContext(), selectedInfo));
                out.add(ActionTools.getForeignApplicationImageUriFromInfo(selectedInfo).toString());
            }

            return out;
        }

    }

}
