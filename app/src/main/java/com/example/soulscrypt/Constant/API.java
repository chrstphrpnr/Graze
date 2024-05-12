package com.example.soulscrypt.Constant;

public class API {

    public static String URL = "http://192.168.169.224:8000/";
    public static String API = URL + "api/";

    // Auth and Profiles
    public static String login_api = API + "mobile/login";
    public static String register_api = API + "mobile/register";
    public static String profile_information = API + "mobile/user/profile";
    public static String update_profile = API + "mobile/user/update/profile";

    public static String relative_list_api = API + "mobile/relatives";
    public static String relative_details_api = API + "mobile/relatives/details";

    public static String fetch_add_relatives = API + "mobile/request/relatives";
    public static String submit_add_relatives = API + "mobile/request/submit/relatives";



    public static String fetch_services = API + "mobile/show/services";
    public static String submit_service_request = API + "mobile/service/request";

    public static String check_new_notification = API + "mobile/user/notifications";



}
