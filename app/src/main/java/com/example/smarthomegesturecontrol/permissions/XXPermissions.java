package com.example.smarthomegesturecontrol.permissions;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XXPermissions {

    private Activity mActivity;
    private List<String> mPermissions;
    private boolean mConstant;


    private XXPermissions(Activity activity) {
        mActivity = activity;
    }


    public static XXPermissions with(Activity activity) {
        return new XXPermissions(activity);
    }


    public XXPermissions permission(String... permissions) { //perms
        if (mPermissions == null) {
            mPermissions = new ArrayList<>(permissions.length);
        }
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }


    public XXPermissions permission(String[]... permissions) {
        if (mPermissions == null) {
            int length = 0;
            for (String[] permission : permissions) {
                length += permission.length;
            }
            mPermissions = new ArrayList<>(length);
        }
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }



    public XXPermissions permission(List<String> permissions) {
        if (mPermissions == null) {
            mPermissions = permissions;
        } else {
            mPermissions.addAll(permissions);
        }
        return this;
    }



    public XXPermissions constantRequest() { //incase perms denied
        mConstant = true;
        return this;
    }




    public void request(OnPermission callback) {
        // If no permission is specified for the request, the request is made using the permission registered with the manifest
        if (mPermissions == null || mPermissions.isEmpty()) {
            mPermissions = PermissionUtils.getManifestPermissions(mActivity);
        }
        if (mPermissions == null || mPermissions.isEmpty()) {
            throw new IllegalArgumentException("Empty Permission");
        }
        if (mActivity == null) {
            throw new IllegalArgumentException("Empty Activity");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed()) {
                throw new IllegalStateException("Destroyed Event");
            } else if (mActivity.isFinishing()) {
                throw new IllegalStateException("Completed Event");
            }
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback Permission");
        }

        PermissionUtils.checkTargetSdkVersion(mActivity, mPermissions);

        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(mActivity, mPermissions);

        if (failPermissions == null || failPermissions.isEmpty()) {
            callback.hasPermission(mPermissions, true); //are all perms accepted
        } else {
            PermissionUtils.checkPermissions(mActivity, mPermissions); //are all perms in manifest
            PermissionFragment.newInstance((new ArrayList<>(mPermissions)), mConstant).prepareRequest(mActivity, callback);
        }
    }




    public static boolean isHasPermission(Context context, String... permissions) {
        return isHasPermission(context, Arrays.asList(permissions));
    }

    public static boolean isHasPermission(Context context, List<String> permissions) {
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissions);
        return failPermissions == null || failPermissions.isEmpty();
    }





    public static boolean isHasPermission(Context context, String[]... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : permissions) {
            permissionList.addAll(Arrays.asList(group));
        }
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissionList);
        return failPermissions == null || failPermissions.isEmpty();
    }




    public static void gotoPermissionSettings(Context context) { //perms settings
        PermissionSettingPage.start(context, false);
    }


    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}