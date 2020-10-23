/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.github.xiaoymin.knife4j.spring.configuration;

import com.github.xiaoymin.knife4j.core.extend.OpenApiExtendSetting;
import com.github.xiaoymin.knife4j.core.model.MarkdownProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 配置文件
 * @since:knife4j 1.9.6
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2019/08/27 15:40
 */
@Component
@ConfigurationProperties(prefix = "knife4j")
public class Knife4jProperties {

    /**
     * 是否开启BasicHttp验证
     */
    private Knife4jHttpBasic basic;

    /**
     * 是否生产环境
     */
    private boolean production;

    /**
     * 个性化配置
     */
    private OpenApiExtendSetting setting;

    /**
     * 分组文档集合
     */
    private List<MarkdownProperty> markdownFiles;

    public Knife4jHttpBasic getBasic() {
        return basic;
    }

    public void setBasic(Knife4jHttpBasic basic) {
        this.basic = basic;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public List<MarkdownProperty> getMarkdownFiles() {
        return markdownFiles;
    }

    public void setMarkdownFiles(List<MarkdownProperty> markdownFiles) {
        this.markdownFiles = markdownFiles;
    }

    public OpenApiExtendSetting getSetting() {
        return setting;
    }

    public void setSetting(OpenApiExtendSetting setting) {
        this.setting = setting;
    }
}
