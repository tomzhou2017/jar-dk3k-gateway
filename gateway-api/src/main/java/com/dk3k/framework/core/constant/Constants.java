/**
 *
 */
package com.dk3k.framework.core.constant;

/**
 * @author lichao
 *
 */
public interface Constants {

    /**
     * 系统常量
     *
     */
    public interface System{

        public static final String OK = "1";
        public static final String FAIL = "0";
        public static final String SERVER_SUCCESS = "00000000";

        public static final String SESSION_TIMEOUT = "4000000";
        public static final String PARAMS_INVALID = "4000001";
        public static final String PERMISSION_DENIED = "4000002";
        public static final String VALICODE_INVALID = "4000003";
        public static final String ACCOUNT_BALANCE_INSUFFICIENT = "4000004";
        public static final String CONNECTION_TIME_OUT = "4000005";
        public static final String NO_REQUEST_MATCH = "4000006";
        public static final String SYSTEM_ERROR_CODE = "4000007";
        public static final String NO_PERMISSIONS = "4000008";

        public static final String SESSION_TIMEOUT_MSG = "登陆超时!";
        public static final String PARAMS_INVALID_MSG = "参数无效!";
        public static final String PERMISSION_DENIED_MSG = "权限不足!";
        public static final String VALICODE_INVALID_MSG = "验证码错误！";
        public static final String ACCOUNT_BALANCE_INSUFFICIENT_MSG = "账户余额不足!";
        public static final String CONNECTION_TIME_OUT_MSG = "连接服务超时!";
        public static final String NO_REQUEST_MATCH_MSG = "没有找到资源!";
        public static final String SYSTEM_ERROR_MSG = "系统错误，请联系管理员!";
        public static final String NO_PERMISSIONS_MSG = "没有权限!";
    }


    /**
     * 用户常量
     */
    public interface User{

        public static final String USER_NOT_EXIEST_ID_MISMATCH = "5000001";
        public static final String USERNAME_OR_PASSWORD_ERROR = "5000002";
        public static final String USER_NOT_ROLE = "5000003";
        public static final String NO_USER_REALNAME_INFO = "5000004";


        public static final String USER_NOT_EXIEST_ID_MISMATCH_MSG = "用户不存在!";
        public static final String USERNAME_OR_PASSWORD_ERROR_MSG = "用户名密码不匹配!";
        public static final String USER_NOT_ROLE_MSG = "没有权限登录!";
        public static final String NO_USER_REALNAME_INFO_MSG = "无该用户实名信息!";


    }

    public interface Borrow{
        public static final String BORROW_NOT_EXIEST = "6000001";
        public static final String BORROW_NOT_EXIEST_MSG = "没有找到借款单";

        public static final String BORROW_VERIFY_ERROR = "6000002";
        public static final String BORROW_VERIFY_ERROR_MSG = "当前借款单不符合审核资格";

        public static final String BORROW_NOT_LENDING = "6000003";
        public static final String BORROW_NOT_LENDING_MSG = "该借款单未放款";

        public static final String BORROW_IS_PAYMENT = "6000004";
        public static final String BORROW_IS_PAYMENT_MSG = "该借款单已经还款";

    }


}
