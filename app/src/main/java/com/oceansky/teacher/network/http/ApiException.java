package com.oceansky.teacher.network.http;

/**
 * User: dengfa
 * Date: 16/7/18
 * Tel:  18500234565
 * Des:  统一处理网络请求的错误
 */
public class ApiException extends RuntimeException {
    //http result code
    public static final String CLINENT_INVALID       = "4012";
    public static final String TOKEN_INVALID         = "4013";
    public static final String PWD_OR_USERBANE_ERROR = "4001";
    public static final String ERROR_LOAD_FAIL       = "5000";
    public static final String ERROR_NO_NET          = "5001";

    public ApiException(String resultCode) {
        super(resultCode);
    }

    /**
     * 根据错误码获取错误提示信息
     *
     * @param resultCode
     * @return
     */
    public static String getApiExceptionMessage(String resultCode) {
        String message = "";
        switch (resultCode) {
            case CLINENT_INVALID:
                message = "非教师端用户请到鹦鹉螺其他平台登录";
                break;
            case PWD_OR_USERBANE_ERROR:
                message = "用户名或密码错误";
                break;
            case ERROR_LOAD_FAIL:
                message = "加载失败";
                break;
            case ERROR_NO_NET:
                message = "网络已断开";
                break;
            default:
                message = "请求失败，稍后再试";
        }
        return message;
    }
}

