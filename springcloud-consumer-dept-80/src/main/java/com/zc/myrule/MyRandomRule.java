
package com.zc.myrule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
//import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MyRandomRule extends AbstractLoadBalancerRule {

    //  每个机器访问5次，然后换下一个服务

    //  total=0，默认=0.如果=5，指向下一个服务节点
    //  index=0，默认0。如果total=5，index+1

    private int total = 0;  //被调用的次数
    private int currentIndex = 0;   //当前是谁在提供服务

    //@SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE"})
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        } else {
            Server server = null;

            while(server == null) {
                if (Thread.interrupted()) {
                    return null;
                }

                List<Server> upList = lb.getReachableServers(); //获得活着的服务
                List<Server> allList = lb.getAllServers();  //获取全部服务
                int serverCount = allList.size();
                if (serverCount == 0) {
                    return null;
                }

                //  ===============自定义代码===================

                if(total < 5) {
                    server = upList.get(currentIndex);
                    total++;
                } else {
                    total = 0;
                    currentIndex++;
                    if(currentIndex >= upList.size()) {
                        currentIndex = 0;
                    }
                    server = upList.get(currentIndex);  //从活着的服务中，获取指定的服务来进行操作
                }

                //  ======================================

                if (server == null) {
                    Thread.yield();
                } else {
                    if (server.isAlive()) {
                        return server;
                    }

                    server = null;
                    Thread.yield();
                }
            }

            return server;
        }
    }

    protected int chooseRandomInt(int serverCount) {
        return ThreadLocalRandom.current().nextInt(serverCount);
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}
