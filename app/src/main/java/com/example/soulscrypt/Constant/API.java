package com.example.soulscrypt.Constant;

public class API {

    public static String URL = "http://192.168.0.53:8000/";
    public static String API = URL + "api/";

    // Auth and Profiles
    public static String login_api = API + "mobile/login";
    public static String register_api = API + "mobile/register";
    public static String profile_information = API + "mobile/user/profile";
    public static String update_profile = API + "mobile/user/update/profile";

    public static String relative_list_api = API + "mobile/relatives";
    public static String relative_details_api = API + "mobile/relatives/details";


}
