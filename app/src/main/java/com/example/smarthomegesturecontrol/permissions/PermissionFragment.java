package com.example.smarthomegesturecontrol.permissions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PermissionFragment extends Fragment implements Runnable {


    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static final String PERMISSION_GROUP = "permission_group"; //perms

    private static final String REQUEST_CODE = "request_code";
    private static final String REQUEST_CONSTANT = "request_constant";

    private final static SparseArray<OnPermission> PERMISSION_ARRAY = new SparseArray<>();
    private boolean mCallback;
    //no duplicated callbacks


    public static PermissionFragment newInstance(ArrayList<String> permissions, boolean constant) {
        PermissionFragment fragment = new PermissionFragment();
        Bundle bundle = new Bundle();
        int requestCode;
        do {
            requestCode = new Random().nextInt(255);// The APK request code compiled by Eclipse must be less than 256
        } while (PERMISSION_ARRAY.get(requestCode) != null);
        bundle.putInt(REQUEST_CODE, requestCode);
        bundle.putStringArrayList(PERMISSION_GROUP, permissions);
        bundle.putBoolean(REQUEST_CONSTANT, constant);
        fragment.setArguments(bundle);

        return fragment;
    }

    public void prepareRequest(Activity activity, OnPermission callback) {
        PERMISSION_ARRAY.put(getArguments().getInt(REQUEST_CODE), callback);
        activity.getFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commitAllowingStateLoss();
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);

        if (permissions == null) {
            return;
        }

        boolean isRequestPermission = false; //below access to unknown sources
        if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !PermissionUtils.isHasInstallPermission(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, getArguments().getInt(REQUEST_CODE));
            isRequestPermission = true;
        }

        if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !PermissionUtils.isHasOverlaysPermission(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, getArguments().getInt(REQUEST_CODE));
            isRequestPermission = true;
        }
        if (!isRequestPermission) {
            requestPermission();
        }
    }



    public void requestPermission() {
        if (PermissionUtils.isOverMarshmallow()) {
            ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);
            if (permissions != null && permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size() - 1]), getArguments().getInt(REQUEST_CODE));
            }
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        OnPermission callback = PERMISSION_ARRAY.get(requestCode);
        if (callback == null) {
            return;
        }
        for (int i = 0; i < permissions.length; i++) { //are perms done or not
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permissions[i])) { //are perms done or not
                if (PermissionUtils.isHasInstallPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            if (Permission.SYSTEM_ALERT_WINDOW.equals(permissions[i])) { //are hover done or not
                if (PermissionUtils.isHasOverlaysPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            //version checking
            if (Permission.ANSWER_PHONE_CALLS.equals(permissions[i]) || Permission.READ_PHONE_NUMBERS.equals(permissions[i])) {
                if (!PermissionUtils.isOverOreo()) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                }
            }
        }



        List<String> succeedPermissions = PermissionUtils.getSucceedPermissions(permissions, grantResults);
        if (succeedPermissions.size() == permissions.length) { //if all perms are accepted
            callback.hasPermission(succeedPermissions, true);
        } else { //if not all perms are accepted by user
            List<String> failPermissions = PermissionUtils.getFailPermissions(permissions, grantResults);


            if (getArguments().getBoolean(REQUEST_CONSTANT)
                    && PermissionUtils.isRequestDeniedPermission(getActivity(), failPermissions)) {
                requestPermission();
                return;
            }

            //address user to access the denied perm
            callback.noPermission(failPermissions, PermissionUtils.checkMorePermissionPermanentDenied(getActivity(), failPermissions));

            if (!succeedPermissions.isEmpty()) {
                callback.hasPermission(succeedPermissions, false);
            }
        }


        PERMISSION_ARRAY.remove(requestCode);
        getFragmentManager().beginTransaction().remove(this).commit(); //avoid duplicates
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mCallback && requestCode == getArguments().getInt(REQUEST_CODE)) {
            mCallback = true;
            HANDLER.postDelayed(this, 500); //a just in case delay
        }
    }




    @Override
    public void run() {
        if (isAdded()) {
            requestPermission();
        }
    }
}