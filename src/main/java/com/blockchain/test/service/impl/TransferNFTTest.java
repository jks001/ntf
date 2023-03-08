package com.blockchain.test.service.impl;


import com.blockchain.test.rpc.EthRpcService;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 *
 * 基于Web3j接口的NFT转账测试
 * @date 2022年03月21日
 * @author jikunshan
 *
 */
public class TransferNFTTest {

    //ERC1155合约地址
    private static final String contractAddress_1155 = "0x0D82DaBe364eB5A2bc8017C312b6325bB7A75C02";

    //ERC721合约地址
    private static final String contractAddress_721 = "0xAcc196F9Ea2434CC99198Fdfd3d7204a815CE44a";
    /**
     * 查询账户信息
     * @throws Throwable
     */
    @Test
    public void getAccountsTest() throws Exception{
        Request<?, EthAccounts> request =  EthRpcService.initGeth().ethAccounts();
        EthAccounts ethAccounts = request.send();
        List<String> accounts = ethAccounts.getAccounts();
        System.out.println(accounts);
    }

    /**
     * ERC721合约mint 测试
     */
    @Test
    public void testMint721(){
        try {
            String to = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";

            //String tockenUrl1 = "https://ipfs.io/ipfs/QmRKpSQVE4fYWypiH1PnnAKN1Nhux1he8JqaBca83uxr3x";
            String tockenUrl = "https://ipfs.io/ipfs/QmNjzzuKCF1q3boi6obrCP6mydQ4dHj113KALXxAikVinQ";
//            String tockenUrl3 = "https://ipfs.io/ipfs/Qmc5aumGZ38VaPtMXQyUHYv6F6unHsSw5uQkTwiyt5QVnG";
            String transactionHash = testMintBaseErc721(privateKey,to,contractAddress_721,tockenUrl);
            System.out.println("transactionHash value: "+transactionHash);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ERC1155合约mint 测试
     */
    @Test
    public void testMint1155(){
        try {
            String to = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";

            List<Uint256> tokenIds = Arrays.asList(new Uint256(7000),new Uint256(5000));
            List<Uint256> mounts = Arrays.asList(new Uint256(20),new Uint256(20));

            String transactionHash = testMintBaseErc1155(privateKey,to,contractAddress_1155,tokenIds,mounts);
            System.out.println("transactionHash value: "+transactionHash);
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

            BigInteger tockenId = new BigInteger("1");
            String transactionHash = transferBalanceBaseErc721(from,privateKey,to,contractAddress_721,tockenId);
            System.out.println("transactionHash value: "+transactionHash);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ERC1155 转账测试
     */
    @Test
    public void transferBalanceERC1155(){
        try {
            String from = "0x4cc82389a388b79656740e58fcd9f436f6295955";
            //eth默认账户，密码为空
            String privateKey = "2836c656631bd1c3f367ecce5f9994135359b755e7607a13df2cf5069b0a6b35";
            String to = "0x13bdb45e4da0e38759b0171f7f229d2f6101c409";

            List<Uint256> tokenIds = Arrays.asList(new Uint256(1000),new Uint256(5000));
            List<Uint256> mounts = Arrays.asList(new Uint256(10),new Uint256(10));

            String transactionHash = transferBalanceBaseErc1155(from,privateKey,to,contractAddress_1155,tokenIds,mounts);
            System.out.println("transactionHash value: "+transactionHash);
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
            Web3j web3j = EthRpcService.intWeb3j();
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce = calNonce(web3j,from);
            //交易手续费
            BigInteger gasPrice = calGasPrice(web3j);
            //交易手续费上限。当gasPrice> gasLimit ,将导致交易失败。
            BigInteger gasLimit = BigInteger.valueOf(600000L);

            //封装调用合约转账方法
            String fromAddress = from.startsWith("0x") ? from.substring(2) : from;
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Function function = new Function(
                    "transferFrom",Arrays.asList(new Address(fromAddress),new Address(toAddress),
                    new Uint256(tockenId)),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易，广播交易-上链
            EthSendTransaction ethSendTransaction = buildTransactionAndPost(web3j,credentials,nonce,
                    gasPrice,gasLimit,contractAddress, data);
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
            Web3j web3j = EthRpcService.intWeb3j();
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce = calNonce(web3j,from);
            //交易手续费
            BigInteger gasPrice = calGasPrice(web3j);
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

            //构造交易，广播交易-上链
            EthSendTransaction ethSendTransaction = buildTransactionAndPost(web3j,credentials,nonce,
                    gasPrice,gasLimit,contractAddress, data);
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
            Web3j web3j = EthRpcService.intWeb3j();

            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce = calNonce(web3j,to);
            //交易手续费
            BigInteger gasPrice = calGasPrice(web3j);
            //交易手续费上限。当gasPrice> gasLimit ,将导致交易失败。
            BigInteger gasLimit = BigInteger.valueOf(600000L);

            //封装调用合约转账方法
            String toAddress = to.startsWith("0x") ? to.substring(2) : to;

            Function function = new Function(
                    "mintNFT",Arrays.asList(new Address(toAddress),new Utf8String(tockenUrl)),Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            //构造交易，广播交易-上链
            EthSendTransaction ethSendTransaction = buildTransactionAndPost(web3j,credentials,nonce,
                    gasPrice,gasLimit,contractAddress, data);
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
            Web3j web3j = EthRpcService.intWeb3j();

            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //交易的笔数
            BigInteger nonce = calNonce(web3j,to);
            //交易手续费
            BigInteger gasPrice = calGasPrice(web3j);

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

            //构造交易，广播交易-上链
            EthSendTransaction ethSendTransaction = buildTransactionAndPost(web3j,credentials,nonce,
                    gasPrice,gasLimit,contractAddress, data);
            return ethSendTransaction.getTransactionHash();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算交易nonce
     * @param web3j
     * @param address
     * @return
     * @throws IOException
     */
    private BigInteger calNonce(Web3j web3j,String address) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
        System.out.println("ethGetTransactionCount nonce: " + ethGetTransactionCount.getTransactionCount());
        if(ethGetTransactionCount == null){
            return null;
        }
        return ethGetTransactionCount.getTransactionCount();
    }

    /**
     * 计算交易gas费
     * @return
     */
    private BigInteger calGasPrice(Web3j web3j) throws ExecutionException, InterruptedException {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        System.out.println("ethGasPrice value: " + ethGasPrice.getGasPrice());
        if(ethGasPrice == null){
            return null;
        }
        return ethGasPrice.getGasPrice();
    }

    /**
     * 构造交易对象并post上链
     */
    private EthSendTransaction buildTransactionAndPost(Web3j web3j,Credentials credentials,BigInteger nonce,
                                                       BigInteger gasPrice,BigInteger gasLimit,
                                                       String contractAddress,String data) throws ExecutionException, InterruptedException {
        //构造交易对象
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
        //eth.chainId()
        Long chainId = 1337L;
        //进行离线签名（入参：交易对象+私钥）
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction,chainId,credentials);
        String hexValue = Numeric.toHexString(signMessage);

        //广播交易-上链
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        return ethSendTransaction;
    }

}
