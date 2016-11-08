package tickide.lexin.com.tickide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xushun on 16/6/30.
 */
public class Content {
    static String getTokenUrl = "http://180.76.179.148/api/user/get_token/";
    static String registerUrl = "http://180.76.179.148/api/user/insert/";
    static String getAllCoverUrl = "http://180.76.179.148/api/cover/list/";
    static String insertCoverUrl = "http://180.76.179.148/api/cover/insert/";
    static String deleteCoverUrl = "http://180.76.179.148/api/cover/remove/";

    static String getUserListUrl = "http://180.76.179.148/api/user/list/";
    static String userUpdateUrl = "http://180.76.179.148/api/user/update/";
    static String TOKEN = "";
    static String COOKIE = "";
    static String USER_NAME = "";

    static List<Map<String, String>> COVERARRAY = new ArrayList<Map<String, String>>();
}
