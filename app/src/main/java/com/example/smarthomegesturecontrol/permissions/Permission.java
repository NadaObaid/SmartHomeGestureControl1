package com.example.smarthomegesturecontrol.permissions;

public final class Permission {


    public static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";

    public static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    //request calendar perm
    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";


    //cam and msgs perm
    public static final String CAMERA = "android.permission.CAMERA";

   //audio perm
    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    //contacts perm
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";

    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";

    //call perm
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String CALL_PHONE = "android.permission.CALL_PHONE";
    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    public static final String USE_SIP = "android.permission.USE_SIP";
    public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    public static final String ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS";
    public static final String READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS";
    public static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public static final String READ_SMS = "android.permission.READ_SMS";
    public static final String SEND_SMS = "android.permission.SEND_SMS";
    public static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    public static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS";


  //body senesors perm
    public static final String BODY_SENSORS = "android.permission.BODY_SENSORS";


 //storage perm
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";


    ///////


    public static final class Group {

        public static final String[] CALENDAR = new String[]{
                Permission.READ_CALENDAR,
                Permission.WRITE_CALENDAR};


        public static final String[] CONTACTS = new String[]{
                Permission.READ_CONTACTS,
                Permission.WRITE_CONTACTS,
                Permission.GET_ACCOUNTS};


        public static final String[] STORAGE = new String[]{
                Permission.READ_EXTERNAL_STORAGE,
                Permission.WRITE_EXTERNAL_STORAGE};
    }
}