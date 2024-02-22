package com.example.smarthomegesturecontrol.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class PermissionUtils {


    static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M; //V6
    }
    static boolean isOverOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O; //V8
    }


    static List<String> getManifestPermissions(Context context) {
        try {
            return Arrays.asList(context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }



    //perm to install
    static boolean isHasInstallPermission(Context context) {
        if (isOverOreo()) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }



    static boolean isHasOverlaysPermission(Context context) {
        if (isOverMarshmallow()) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }




    static ArrayList<String> getFailPermissions(Context context, List<String> permissions) {
        if (!PermissionUtils.isOverMarshmallow()) {
            return null; //when v6
        }

        ArrayList<String> failPermissions = null;

        for (String permission : permissions) {
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission)) { //install perm

                if (!isHasInstallPermission(context)) {
                    if (failPermissions == null) {
                        failPermissions = new ArrayList<>();
                    }
                    failPermissions.add(permission);
                }
                continue;
            }

            if (Permission.SYSTEM_ALERT_WINDOW.equals(permission)) { //hover perm

                if (!isHasOverlaysPermission(context)) {
                    if (failPermissions == null) {
                        failPermissions = new ArrayList<>();
                    }
                    failPermissions.add(permission);
                }
                continue;
            }


            if (Permission.ANSWER_PHONE_CALLS.equals(permission) || Permission.READ_PHONE_NUMBERS.equals(permission)) {
                if (!isOverOreo()) {
                    continue;
                }
            }

            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                if (failPermissions == null) {
                    failPermissions = new ArrayList<>();
                }
                failPermissions.add(permission);
            }
        }

        return failPermissions;
    }





    static boolean isRequestDeniedPermission(Activity activity, List<String> failPermissions) {
        for (String permission : failPermissions) {
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission) || Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                continue;
            }
            if (!checkSinglePermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }

        return false;
    }


    //if perms are denied
    static boolean checkMorePermissionPermanentDenied(Activity activity, List<String> permissions) {
        for (String permission : permissions) {
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission) || Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                continue;
            }
            if (checkSinglePermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }

        return false;
    }




    private static boolean checkSinglePermissionPermanentDenied(Activity activity, String permission) {


        //v8 perms
        if (Permission.ANSWER_PHONE_CALLS.equals(permission) || Permission.READ_PHONE_NUMBERS.equals(permission)) {
            if (!isOverOreo()) {
                return false;
            }
        }

        if (PermissionUtils.isOverMarshmallow()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED &&
                    !activity.shouldShowRequestPermissionRationale(permission);
        }

        return false;
    }



    //get failed perms
    static List<String> getFailPermissions(String[] permissions, int[] grantResults) {
        List<String> failPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                failPermissions.add(permissions[i]);
            }
        }
        return failPermissions; //0 granted and -1 not
    }



    //get success perms
    static List<String> getSucceedPermissions(String[] permissions, int[] grantResults) {
        List<String> succeedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                succeedPermissions.add(permissions[i]);
            }
        }
        return succeedPermissions; //0 granted and -1 not
    }




    //check perms in manifest
    static void checkPermissions(Activity activity, List<String> requestPermissions) {
        List<String> manifestPermissions = PermissionUtils.getManifestPermissions(activity);
        if (manifestPermissions != null && !manifestPermissions.isEmpty()) {
            for (String permission : requestPermissions) {
                if (!manifestPermissions.contains(permission)) {
                    throw new ManifestException(permission);
                }
            }
        } else {
            throw new ManifestException();
        }
    }




    //sdk perms
    static void checkTargetSdkVersion(Context context, List<String> requestPermissions) {
        if (requestPermissions.contains(Permission.REQUEST_INSTALL_PACKAGES)
                || requestPermissions.contains(Permission.ANSWER_PHONE_CALLS)
                || requestPermissions.contains(Permission.READ_PHONE_NUMBERS)) {
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O) {
                throw new RuntimeException("The targetSdkVersion must be more than or equal to 26"); //sdk v more than or equal to 26
            }
        } else {
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
                throw new RuntimeException("The targetSdkVersion SDK must be 23 or more"); //sdk v more than or equal to 23
            }
        }
    }
}