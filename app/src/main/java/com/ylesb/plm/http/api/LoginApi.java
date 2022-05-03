package com.ylesb.plm.http.api;

import com.hjq.http.config.IRequestApi;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/12/07
 *    desc   : 用户登录
 */
public final class LoginApi implements IRequestApi {

    @Override
    public String getApi() {
        return "app/login";
    }

    /** 手机号 */
    private String mobile;
    /** 登录密码 */
    private String password;

    public LoginApi setPhone(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public LoginApi setPassword(String password) {
        this.password = password;
        return this;
    }

    public final static class Bean {

        private String token;

        public String getToken() {
            return token;
        }
    }
}