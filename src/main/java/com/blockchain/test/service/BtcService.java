package com.blockchain.test.service;

import com.blockchain.test.entity.TransferModel;


public interface BtcService {

    /**
     * 获取BTC余额
     * @param walletName 钱包名称
     * @param address 地址
     * @return 余额
     */
    double getBtcBalance(String walletName,String address);

}
