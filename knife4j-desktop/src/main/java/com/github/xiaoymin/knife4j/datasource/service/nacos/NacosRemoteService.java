package com.github.xiaoymin.knife4j.datasource.service.nacos;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.common.lang.DesktopConstants;
import com.github.xiaoymin.knife4j.datasource.model.ServiceRoute;
import com.github.xiaoymin.knife4j.datasource.model.config.route.NacosRoute;
import com.github.xiaoymin.knife4j.datasource.model.service.nacos.NacosInstance;
import com.github.xiaoymin.knife4j.gateway.executor.apache.pool.PoolingConnectionManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author <a href="xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2022/12/18 19:04
 * @since:knife4j-desktop
 */
@Slf4j
public class NacosRemoteService extends PoolingConnectionManager implements Callable<Optional<ServiceRoute>> {

    /**
     * Nacos获取实例列表OpenAPI接口，详情参考：https://nacos.io/zh-cn/docs/open-api.html
     */
    private static final String NACOS_INSTANCE_LIST_API="/v1/ns/instance/list";


    /**
     * 服务名称
     */
    private final String serviceUrl;
    /**
     * nacos密钥
     */
    private final String accessToken;
    /**
     * Nacos配置
     */
    private final NacosRoute nacosRoute;


    public NacosRemoteService(String serviceUrl, String accessToken, NacosRoute nacosRoute) {
        this.serviceUrl = serviceUrl;
        this.accessToken = accessToken;
        this.nacosRoute = nacosRoute;
    }

    @Override
    public Optional<ServiceRoute> call() throws Exception {
        List<String> params=new ArrayList<>();
        params.add("serviceName="+nacosRoute.getServiceName());
        //默认聚合时只返回健康实例
        params.add("healthyOnly=true");
        if (StrUtil.isNotBlank(nacosRoute.getGroupName())){
            params.add("groupName="+nacosRoute.getGroupName());
        }
        if (StrUtil.isNotBlank(nacosRoute.getNamespaceId())){
            params.add("namespaceId="+nacosRoute.getNamespaceId());
        }
        if (StrUtil.isNotBlank(nacosRoute.getClusters())){
            params.add("clusters="+nacosRoute.getClusters());
        }
        //是否需要登录token
        if (StrUtil.isNotBlank(this.accessToken)){
            params.add("accessToken="+this.accessToken);
        }
        String parameter= CollectionUtil.join(params,"&");
        String api=serviceUrl+NACOS_INSTANCE_LIST_API+"?"+parameter;
        HttpGet get=new HttpGet(api);
        CloseableHttpResponse response=getClient().execute(get);
        if (response!=null){
            int statusCode=response.getStatusLine().getStatusCode();
            log.info("Nacos Response Status:{}",statusCode);
            if (statusCode== HttpStatus.SC_OK){
                String content= EntityUtils.toString(response.getEntity(),"UTF-8");
                if (StrUtil.isNotBlank(content)){
                    JsonElement jsonElement= JsonParser.parseString(content);
                    if (jsonElement!=null&&jsonElement.isJsonObject()){
                        JsonElement instances=jsonElement.getAsJsonObject().get("hosts");
                        if (instances!=null&&instances.isJsonArray()){
                            Type type=new TypeToken<List<NacosInstance>>(){}.getType();
                            List<NacosInstance> nacosInstances= DesktopConstants.GSON.fromJson(instances,type);
                            if (CollectionUtil.isNotEmpty(nacosInstances)){
                                NacosInstance nacosInstance=nacosInstances.stream().findAny().get();
                                nacosInstance.setServiceName(nacosRoute.getServiceName());
                                return Optional.of(new ServiceRoute(nacosRoute,nacosInstance));
                            }
                        }
                    }
                }
            }else{
                get.abort();
            }
        }
        IoUtil.close(response);
        return Optional.empty();
    }
}
