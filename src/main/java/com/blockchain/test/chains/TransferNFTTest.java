package com.blockchain.test.chains;


import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.ChildNumber;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransferNFTTest {

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
        Request<?, EthAccounts> request =  EthService.initGeth().ethAccounts();
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
     * 解锁账户
     * @return
     * @throws IOException
     */
    @Test
    public void unlockAccountTest() throws IOException {
        Admin admin = EthService.initAdmin();
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
        Admin admin = EthService.initAdmin();
        Request<?, NewAccountIdentifier> request = admin.personalNewAccount(passWord);
        NewAccountIdentifier result = request.send();
        String accountId = result.getAccountId();
        return accountId;
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
            String address = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            BigInteger balance = getBalance(address);
            System.out.println(balance);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private BigInteger getBalance(String address) throws IOException{
        Web3j web3j = EthService.intWeb3j();
        Request<?, EthGetBalance> request = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"));
        EthGetBalance result = request.send();
        return result.getBalance();
    }

    @Test
    public void testMint721(){
        try {
            String to = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";

            //ERC721合约地址
            String contractAddress_721 = "0xAcc196F9Ea2434CC99198Fdfd3d7204a815CE44a";
            //ERC1155 合约地址
            String contractAddress_1155 = "0x037a2872B049fb54b0e3911e66862802760167E4";


//            String tockenUrl1 = "https://ipfs.io/ipfs/QmRKpSQVE4fYWypiH1PnnAKN1Nhux1he8JqaBca83uxr3x";
            String tockenUrl = "https://ipfs.io/ipfs/QmNjzzuKCF1q3boi6obrCP6mydQ4dHj113KALXxAikVinQ";
//            String tockenUrl3 = "https://ipfs.io/ipfs/Qmc5aumGZ38VaPtMXQyUHYv6F6unHsSw5uQkTwiyt5QVnG";
            String transactionHash = testMintBaseErc721(privateKey,to,contractAddress_721,tockenUrl);
            System.out.println("transactionHash value: "+transactionHash);
            System.out.println("after balance value: "+getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testMint1155(){
        try {
            String to = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";
            //ERC1155 合约地址
            String contractAddress_1155 = "0xAcc196F9Ea2434CC99198Fdfd3d7204a815CE44a";

            List<Uint256> tokenIds = Arrays.asList(new Uint256(7000),new Uint256(5000));
            List<Uint256> mounts = Arrays.asList(new Uint256(20),new Uint256(20));

            String transactionHash = testMintBaseErc1155(privateKey,to,contractAddress_1155,tokenIds,mounts);
            System.out.println("transactionHash value: "+transactionHash);
            System.out.println("after balance value: "+getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 转账测试
     */
    @Test
    public void transferBalanceERC721(){
        try {
            String from = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";
            String to = "0x13bdb45e4da0e38759b0171f7f229d2f6101c409";
            //ERC721合约地址
            String contractAddress = "0xAcc196F9Ea2434CC99198Fdfd3d7204a815CE44a";

            BigInteger tockenId = new BigInteger("1");
            String transactionHash = transferBalanceBaseErc721(from,privateKey,to,contractAddress,tockenId);
            System.out.println("transactionHash value: "+transactionHash);
            System.out.println("after balance value: "+getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 转账测试
     */
    @Test
    public void transferBalanceERC1155(){
        try {
            String from = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";
            String to = "0x13bdb45e4da0e38759b0171f7f229d2f6101c409";
            //ERC1155合约地址
            String contractAddress_1155 = "0x037a2872B049fb54b0e3911e66862802760167E4";

            List<Uint256> tokenIds = Arrays.asList(new Uint256(1000),new Uint256(3000),new Uint256(5000));
            List<Uint256> mounts = Arrays.asList(new Uint256(10),new Uint256(10),new Uint256(10));

            String transactionHash = transferBalanceBaseErc1155(from,privateKey,to,contractAddress_1155,tokenIds,mounts);
            System.out.println("transactionHash value: "+transactionHash);
            System.out.println("after balance value: "+getBalance(to));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 基于ERC721合约的转账流程。
     * @param from 转账发送人
     * @param privateKey  发送人私钥
     * @param to 接收人
     * @param contractAddress 合约地址
     * @param tockenId
     * @return
     */
    private String transferBalanceBaseErc721(String from,String privateKey,String to ,String contractAddress,BigInteger tockenId){
        try{
            Web3j web3j = EthService.intWeb3j();
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

            //封装调用合约转账方法
            String fromAddress = from.startsWith("0x") ? from.substring(2) : from;
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Function function = new Function(
                    "transferFrom",Arrays.asList(new Address(fromAddress),new Address(toAddress),
                    new Uint256(tockenId)),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
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

    /**
     *  基于ERC1155合约的transfer
      * @param from
     * @param privateKey
     * @param to
     * @param contractAddress
     * @param tokenIds
     * @param amounts
     * @return
     */
    private String transferBalanceBaseErc1155(String from,String privateKey,String to ,String contractAddress,List<Uint256> tokenIds,List<Uint256> amounts){
        try{
            Web3j web3j = EthService.intWeb3j();
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

            //封装调用合约转账方法
            String fromAddress = from.startsWith("0x") ? from.substring(2) : from;
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Array tokenArrs = new DynamicArray<Uint256>(tokenIds);
            Array amountsArrs = new DynamicArray<Uint256>(amounts);
            String extDataStr = "mintBatch Test";
            DynamicBytes extData = new DynamicBytes(extDataStr.getBytes(StandardCharsets.UTF_8));

            Function function = new Function(
                    "safeBatchTransferFrom",Arrays.asList(new Address(fromAddress),new Address(toAddress),
                    tokenArrs,amountsArrs,extData),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
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


    /**
     * mint test
     * @param privateKey
     * @param to
     * @param contractAddress
     * @param tockenUrl
     * @return
     */
    private String testMintBaseErc721(String privateKey,String to ,String contractAddress ,String tockenUrl){
        try{
            Web3j web3j = EthService.intWeb3j();

            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(to, DefaultBlockParameterName.PENDING).send();
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

            //封装调用合约转账方法
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Function function = new Function(
                    "mintNFT",Arrays.asList(new Address(toAddress),new Utf8String(tockenUrl)),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
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


    /**
     * mint test
     * @param privateKey
     * @param to
     * @param contractAddress
     * @param tokenIds tokenId列表
     * @param amounts 数量
     * @return
     */
    private String testMintBaseErc1155(String privateKey,String to ,String contractAddress ,List<Uint256> tokenIds,List<Uint256> amounts){
        try{
            Web3j web3j = EthService.intWeb3j();

            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(to, DefaultBlockParameterName.PENDING).send();
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

            //封装调用合约转账方法
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Array tokenArrs = new DynamicArray<Uint256>(tokenIds);
            Array amountsArrs = new DynamicArray<Uint256>(amounts);
            String extDataStr = "mintBatch Test";
            DynamicBytes extData = new DynamicBytes(extDataStr.getBytes(StandardCharsets.UTF_8));
            Function function = new Function("mintBatch",
                    Arrays.asList(new Address(toAddress),tokenArrs,amountsArrs,extData),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
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
