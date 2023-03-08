package com.blockchain.test.rpc;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;

/**
 * 以太坊Web3j 服务。
 */
public class EthRpcService {
    /**
     * eth客户端RPC访问地址
     */
    private static final String urlStr = "http://localhost:8545/";

    /**
     * 初始化Web3j普通调用
     * @return
     */
    public static Web3j intWeb3j(){
        return Web3j.build(getHttpServie());
    }

    /**
     * 初始化personal级别的api接口实例
     * @return
     */
    public static Geth initGeth(){
        return Geth.build(getHttpServie());
    }

    /**
     * 初始化admin级别的api接口实例
     * @return
     */
    public static Admin initAdmin(){
        return Admin.build(getHttpServie());
    }

    /**
     * http方式连接到eth客户端。
     * @return
     * @throws Throwable
     */
    private static HttpService getHttpServie(){
        return new HttpService(urlStr);
    }
}
