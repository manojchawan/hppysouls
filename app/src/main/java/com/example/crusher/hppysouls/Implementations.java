package com.example.crusher.hppysouls;

/**
 * Created by crusher on 24/1/17.
 */

public class Implementations {

    public static String base_url = "http://139.59.15.60/hppysouls/index.php/";
    public static String update_pic = "http://139.59.15.60/hppysouls/upload.php";

    public static String login_url_auth = base_url + "login/auth";
    public static String forget_url_verify = base_url + "update/forgot_verify";
    public static String forget_url_auth = base_url + "update/forgot_auth";
    public static String signup_verify = base_url + "signup/verify/";
    public static String signup_auth = base_url + "signup/auth/";
    public static String resend_otp = base_url + "signup/resend/";
    public static String suggest_all = base_url + "suggest/all";
    public static String like_url = base_url + "like/hit";
    public static String liked_user = base_url + "like/fetch";
    public static String chat_url = base_url + "chat/send";
    public static String update_profile = base_url + "update";
    public static String chat_receive = base_url + "chat/receive";
    public static String chat_list_url = base_url + "chat/listchats";
    public static String gang_list_url = base_url + "group/listgroups";
    public static String group_send_url = base_url + "group/send";
    public static String group_receive_url = base_url + "group/receive";
    public static String group_members = base_url + "group/members";
    public static String group_leave = base_url + "group/leavegroup";
    public static String group_delete = base_url + "group/delete";
    public static String group_status = base_url + "update/groupstatus";
    public static String add_gang_url = base_url + "group/add";
    public static String add_gang_member = base_url + "group/adduser";
    public static String interest_send = base_url + "interest/add";
    public static String interest_fetch = base_url + "interest/fetch";
    public static String interest_delete = base_url + "interest/delete";
    public static String chatBlock = base_url + "chat/block";
    public static String isUserBlock = base_url + "chat/isuserblocked";
    public static String isUser = base_url + "getinfo/isuser";
    public static String education_fetch = base_url + "education/fetch";
    public static String education_add = base_url + "education/add";
    public static String education_delete = base_url + "education/delete";
    public static String fetch_favorites = base_url + "group/fetchUsers";
    public static String notification_url = base_url + "updates/fetch";


    public static String user_num;
    public static String chat_num;
    public static String user_email;
    public static String gang_id;
    public static String gang_name;
    public static String gang_type;


}
