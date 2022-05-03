package com.ylesb.plm.http.model;

import com.ylesb.plm.other.AppConfig;
import com.hjq.http.config.IRequestServer;
import com.hjq.http.model.BodyType;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2020/10/02
 *    desc   : 服务器配置
 */
public class RequestServer implements IRequestServer {

    @Override
    public String getHost() {
        return AppConfig.getHostUrl();
    }
    //在整体配置加上api
    @Override
    public String getPath() {
        return "renren-fast/";
    }

    @Override
    public BodyType getType() {
        // 以表单的形式提交参数
        return BodyType.JSON;
    }
}