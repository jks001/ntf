package com.blockchain.test.service.impl;


import com.blockchain.test.rpc.EthRpcService;
import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import org.bitcoinj.crypto.HDKeyDerivation;
public class EthChainTest {

    /**
     * 测试密码使用常量
     */
    private static final String passWord = "test1";

    /**
     * 查询账户信息
     * @throws Throwable
     */
    @Test
    public void getAccountsTest() throws Exception{
        Request<?, EthAccounts> request =  EthRpcService.initGeth().ethAccounts();
        EthAccounts ethAccounts = request.send();
        //[0x4cc82389a388b79656740e58fcd9f436f6295955, 0x13bdb45e4da0e38759b0171f7f229d2f6101c409]
        List<String> accounts = ethAccounts.getAccounts();
        System.out.println(accounts);
    }


    /**
     * 创建一个密码为test1的账户
     * @throws Exception
     */
    @Test
    public void newAccountTest(){
        try{
            String accountId = createAccounts(passWord);
            System.out.println(accountId);
        }catch (Exception e){
           e.printStackTrace();
        }
    }


    /**
     * path路径
     */
    private final static ImmutableList<ChildNumber> BIP44_ETH_ACCOUNT_ZERO_PATH =
            ImmutableList.of(new ChildNumber(44, true), new ChildNumber(60, true),
                    ChildNumber.ZERO_HARDENED, ChildNumber.ZERO);
    /**
     * 创建钱包
     * @throws
     */
    @Test
    public void createAccountLocal() throws Exception{
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);

        //生成12位助记词
        List<String>  str = MnemonicCode.INSTANCE.toMnemonic(entropy);

        //使用助记词生成钱包种子
        byte[] seed = MnemonicCode.toSeed(str, "");
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(BIP44_ETH_ACCOUNT_ZERO_PATH, false, true, new ChildNumber(0));
        byte[] bytes = deterministicKey.getPrivKeyBytes();
        ECKeyPair keyPair = ECKeyPair.create(bytes);
        //通过公钥生成钱包地址
        String address = Keys.getAddress(keyPair.getPublicKey());

        System.out.println();
        System.out.println("助记词：");
        System.out.println(str);
        System.out.println();
        System.out.println("地址：");
        System.out.println("0x"+address);
        System.out.println();
        System.out.println("私钥：");
        System.out.println("0x"+keyPair.getPrivateKey().toString(16));
        System.out.println();
        System.out.println("公钥：");
        System.out.println(keyPair.getPublicKey().toString(16));
    }
    /**
     * 解锁账户
     * @return
     * @throws IOException
     */
    @Test
    public void unlockAccountTest() throws IOException {
        Admin admin = EthRpcService.initAdmin();
        String address = "0x278bc7e2a48b73fc683e5213ec446a1bb5adb43a";
        BigInteger timeSeconds = new BigInteger("100000000");
        /**
         * @param address  地址
         * @param passWord 密码
         * @param timeSeconds 解锁有效时间，单位秒
         */
        Request<?, PersonalUnlockAccount> request = admin.personalUnlockAccount(address, passWord, timeSeconds);
        PersonalUnlockAccount result  = request.send();
        System.out.println(result.accountUnlocked());
    }

    /**
     * 根据passWord 创建一个新的账户
     * @param passWord
     * @throws IOException
     */
    private String createAccounts(String passWord) throws IOException {
        Admin admin = EthRpcService.initAdmin();
        Request<?, NewAccountIdentifier> request = admin.personalNewAccount(passWord);
        NewAccountIdentifier result = request.send();
        String accountId = result.getAccountId();
        return accountId;
    }

    @Test
    public void printPrivateKey(){
        String privateKey = getPrivateKey("","/Users/jikunshan/Documents/doc/eth_self/keystore/UTC--2022-02-15T06-03-54.178873000Z--4cc82389a388b79656740e58fcd9f436f6295955");
        System.out.println(privateKey);
    }
    /**
     * 获取用户私钥
     * @param passWord
     * @param keyStorePath
     * @return
     */
    private String getPrivateKey(String passWord,String keyStorePath){
        try{
            Credentials credentials = WalletUtils.loadCredentials(passWord,keyStorePath);
            BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
            return privateKey.toString(16);
        }catch (IOException | CipherException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询账户余额测试
     * 0x58f724a1f0e5619954c15984ebabea492ba31a70 线下生成的私钥地址。
     *0x58f724a1f0e5619954c15984ebabea492ba31a70   ---2000000000000000000  --->  999999999999831616
     *0x4cc82389a388b79656740e58fcd9f436f6295955    ---115792089237316195423570985008687907853269984665640564039453584007913129197919  ---> 115792089237316195423570985008687907853269984665640564039454584007913129218967
     */
    @Test
    public void getBalanceTest(){
        try {
            String address = "0x4ecf34fe48b729d1a1a90e27ccc63c2207820376";
            String address1 = "0xb81a72b477ef1b5ea2e4eb580309e1fe255c3200";
            //
            BigInteger balance = getBalance(address);
            System.out.println(balance);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void broadCastTest(){
        String signHash0 = "0xf86e808509502f900083015f9094b81a72b477ef1b5ea2e4eb580309e1fe255c3200872386f26fc1000080820a95a0d56a10d8f3f3bb16c76c245bdb5b6f2a864a70ca5561c5f1ae4f3800773d4162a05bf041ac0a6d712d87eff21cc8336e398cd5e289209445f29d735dbaf5bad0fb";
        String signHash1 = "0xf86e018502cb41780083015f90944ecf34fe48b729d1a1a90e27ccc63c220782037687038d7ea4c6800080820a96a0a89bfdc1483b1b63e542d1c4cc0eb05f2370e980edd3e359d8632f859e6d206da04f9dee8ef76026c1c5c6add9b5fb92f596947a1f19651aba966295786eb7d09e";
        String signHash2 = "0xf8ac03851f3305bc00830120c1946e12b66036b4cc58dbae267cfa6dac5800fe45d580b844a9059cbb0000000000000000000000004ecf34fe48b729d1a1a90e27ccc63c2207820376000000000000000000000000000000000000000000000000000000000000000a820a96a081bd890a37fb6e5327185b9f541817af468035ad712701b2ddfa43c179c18c1ca0053f152b3650e39641d77a332d5de6d2915f8953438aadc6b792f83de1d92a43";
        String signHash = "0xf8ac80851f3305bc00830120c1946e12b66036b4cc58dbae267cfa6dac5800fe45d580b844a9059cbb0000000000000000000000004ecf34fe48b729d1a1a90e27ccc63c2207820376000000000000000000000000000000000000000000000000000000000000000a820a96a08d202dd2d4ed5bead925600565da0884b1df383542a25f2db0bfc21f610269b9a06eb351cb1cb69e025a7b818f262ad513e1109465df78329e2886632ed894a4fc";

        //发起交易（广播以太坊交易）
        try{
            Web3j web3j = EthRpcService.intWeb3j();
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signHash1).sendAsync().get();
            Object boradCastResult = ethSendTransaction.getTransactionHash();
            System.out.println("boradCastResult : " + boradCastResult);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private BigInteger getBalance(String address) throws IOException{
        Web3j web3j = EthRpcService.intWeb3j();
        Request<?, EthGetBalance> request = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"));
        EthGetBalance result = request.send();
        return result.getBalance();
    }

    /**
     * 转账测试
     */
    @Test
    public void transferBalanceTest(){
        try {
            String from = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String passWordFrom = "";
            String keyStrore = "/Users/jikunshan/Documents/doc/eth_self/keystore/UTC--2022-02-15T06-03-54.178873000Z--4cc82389a388b79656740e58fcd9f436f6295955";
            String to = "0x8ac507d3d2a451211448913ea6023e595cb0543c";
            //该值默认为ETHR
            int value = 100;
            String data = "转账测试！";
            String transactionHash = transferBalance(from,passWordFrom,keyStrore,to,value,data);
            System.out.println(transactionHash);
            System.out.println(getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 转账测试
     */
    @Test
    public void transferBalanceTest1(){
        try {
            String from = "0x58f724a1f0e5619954c15984ebabea492ba31a70";
            //eth默认账户，密码为空
            String privateKey = "0x8696c01a94b6d41f9853ae856c327b04877a14357e0b98cd18078c662ec7fd5";
            String to = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //该值默认为ETHR
            int value = 1;
            String data = "转账测试！";
            String transactionHash = transferBalance(from,privateKey,to,value,data);
            System.out.println(transactionHash);
            System.out.println(getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 转账流程。
     * @param from 转账发送人
     * @param passWordFrom  发送人密码
     * @param keyStore 发送人keyStore文件
     * @param to 接收人
     * @param value 接受金额
     * @param data 附加数据
     * @return
     */
    private String transferBalance(String from,String passWordFrom, String keyStore,String to , int value,String data){
        try{
            Web3j web3j = EthRpcService.intWeb3j();
            //生成转账的凭证，需要传入私钥 （由密码及keyStore生成）
            Credentials credentials = Credentials.create(getPrivateKey(passWordFrom,keyStore));
            //交易的笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            System.out.println("ethGetTransactionCount nonce: " + ethGetTransactionCount.getTransactionCount());
            if(ethGetTransactionCount == null){
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            //交易手续费
            BigInteger gasPrice;
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            System.out.println("ethGasPrice value: " + ethGasPrice.getGasPrice());
            if(ethGasPrice == null){
                return null;
            }
            gasPrice = ethGasPrice.getGasPrice();
            //交易手续费上限。当gasPrice> gasLimit ,将导致交易失败。
            BigInteger gasLimit = BigInteger.valueOf(600000L);

            //交易金额单位转换为Wei
            BigInteger amountToTransferInWei = Convert.toWei(String.valueOf(value),Convert.Unit.ETHER).toBigInteger();

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,gasPrice,gasLimit,to,amountToTransferInWei,data);
            //eth.chainId()
            Long chainId = 1337L;


            //进行离线签名（入参：交易对象+私钥）
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction,chainId,credentials);
            String hexValue = Numeric.toHexString(signMessage);

            //发起交易（广播以太坊交易）
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            return ethSendTransaction.getTransactionHash();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 转账流程。
     * @param from 转账发送人
     * @param privateKey  发送人私钥
     * @param to 接收人
     * @param value 接受金额
     * @param data 附加数据
     * @return
     */
    private String transferBalance(String from,String privateKey,String to , int value,String data){
        try{
            Web3j web3j = EthRpcService.intWeb3j();
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            System.out.println("ethGetTransactionCount nonce: " + ethGetTransactionCount.getTransactionCount());
            if(ethGetTransactionCount == null){
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            //交易手续费
            BigInteger gasPrice;
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            System.out.println("ethGasPrice value: " + ethGasPrice.getGasPrice());
            if(ethGasPrice == null){
                return null;
            }
            gasPrice = ethGasPrice.getGasPrice();
            //交易手续费上限。当gasPrice> gasLimit ,将导致交易失败。
            BigInteger gasLimit = BigInteger.valueOf(600000L);

            //交易金额单位转换为Wei
            BigInteger amountToTransferInWei = Convert.toWei(String.valueOf(value),Convert.Unit.ETHER).toBigInteger();

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,gasPrice,gasLimit,to,amountToTransferInWei,data);
            //eth.chainId()
            Long chainId = 1337L;

            //进行离线签名（入参：交易对象+私钥）
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction,chainId,credentials);
            String hexValue = Numeric.toHexString(signMessage);

            //广播交易-上链
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            return ethSendTransaction.getTransactionHash();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
