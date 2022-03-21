package com.blockchain.test.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blockchain.test.entity.UtxoInput;
import com.blockchain.test.service.BtcService;
import com.blockchain.test.utils.UnSpentUtxo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BtcServiceImpl implements BtcService {

    private String user = "alice";//alice
    private String password = "123456";//123456
    private String port = "8334";
    private String host = "127.0.0.1";//localhost

    /**
     *
     * 生成比特币私钥、公钥、地址
     *{mnemonics=[rate, suspect, pony, begin, clinic, flag, throw, coast, animal, setup, join, animal],
     *  privateKey=cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T,
     *  publicKey=02eeab727493118d3d69e072a4fe00a778ebcda881460a753c8a4d98afb3148bd9,
     *  address=mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8}
     *
     * {mnemonics=[unfold, light, ozone, enhance, service, ticket, slush, elbow, display, message, blast, obtain],
     * privateKey=cSwBtKxx9Fi16FioEUWVUuFnH2s1zbpTw5Hft1reAvJUac9RrFuQ,
     * publicKey=031c99dddcb60cd3a0d633e6e23b162c37d0003b339e7105b3807636d5d442cacb,
     * address=miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK}
     *
     * {mnemonics=[ketchup, decline, dog, child, acid, soap, work, rude, bachelor, arctic, carbon, adult],
     * privateKey=cNDmSqzTBAcSB3ajURbickkDXdNWZtrN2Le1fKzLfG9gnrFPgGaz,
     * publicKey=037226f91763cc2701bca2d48d08ad13de29f919d99cdfe92cd15cff87299c3305,
     * address=n3yP7LEuT4bjjHizUyiNT21iUTa61QmnG8}
     *
     * {mnemonics=[leave, drill, blush, banner, another, security, delay, quality, enlist, double, clay, donate],
     * privateKey=cVW1YjLsjtJg4Fq89QkbPcwxDEAu3xe3WvAgCfqbjLDiZ45cRn63,
     * publicKey=034ee826a2ffc01fb17beaa7603efd46f8f0bb0b58f641fe3257210f67bd13cb93,
     * address=mtnjsiWBftaaTGmM1X6aAnTT2dCJUzZ5Ep}
     */
    @Test
    public void getBtcAddress(){
        Map btcMap2 = newAddress();
        System.out.println(btcMap2);
    }

    @Test
    public void getBalance(){
        String walletName="testwallet_99";
        String address="bcrt1qz4rwta6g0u38lkwrmsly9d48j7yux73sl67g4q";
        double balance= getBtcBalance(walletName,address);
        System.out.println("钱包("+walletName+")的余额是==>"+balance);
    }


    /**
     *生成地址，私钥 ，公钥以及助记词
     * @return
     */
    public static Map newAddress() {
        NetworkParameters networkParameters = RegTestParams.get() ;
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, "", Utils.currentTimeSeconds());
        Wallet wallet;
        String mnemonics = "";
        String privateKey = "";
        String publicKey = "";
        String address = "";
        String pwd = "";
        try {
            wallet = Wallet.fromSeed(networkParameters, seed);
            //私钥
            privateKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(networkParameters);
            //助记词
            mnemonics = wallet.getKeyChainSeed().getMnemonicCode().toString();
            publicKey = Hex.toHexString(ECKey.publicKeyFromPrivate(wallet.currentReceiveKey().getPrivKey(), true));
            //地址
            address = wallet.currentReceiveAddress().toBase58();
        } catch (Exception e) {
            System.out.println("比特币地址创建失败，原因"+e.getMessage());
            return null;
        }
        Map resultMap = new LinkedHashMap();
        resultMap.put("mnemonics", mnemonics);
        resultMap.put("privateKey", privateKey);
        resultMap.put("publicKey", publicKey);
        resultMap.put("address", address);
        return resultMap;
    }


    /**
     * 一对一转账交易
     */
    @Test
    public void testTransactionOneToOne() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "c8d84a75f8205870e11357d3c7fec4ac94733c46635a2d2449667be1a47938ef",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);
        us.add(u);
        System.out.println(JSON.toJSONString(us));
        String resultHex = createRawTransaction("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T", "mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK", 4999900000L, 100000L, us);
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }

    /**
     * 多对一组交易+转账交易
     * sendrawtransaction result :"2832cc14dc3b29285389e692d4b8f7a9218eaf15d6625281a8a5bde290dc8f9b"
     * 转账后地址余额：
     *  mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8 409.99900000
     *  miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK 140
     *  手续费：0.001
     */
    @Test
    public void testTransactionManyToOne() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "414d154d32dc55d747f3472e09808b47b5cd92d6ed7f06e8192b23da7724820c",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);

        UnSpentUtxo u1 = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "a519c24c26f1059c15d4d4cae435d90db71ae35733a2019537d7e72ed8f07d1b",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);

        UnSpentUtxo u2 = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "5de0a1dfa246a260cd591a4db48fde1e30e682941f3480ddf0b7f48580d6a857",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);

        us.add(u);
        us.add(u1);
        us.add(u2);
        System.out.println(JSON.toJSONString(us));
        String resultHex = createRawTransaction("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T",
                "mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8", "miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK",
                140*100000000L, 100000L, us);
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }
    /**
     * 一对多组交易+转账交易
     * 接受金额大于转账金额时：bad-txns-in-belowout, value in (50.00) < value out (69.996)"}
     * createRawTransaction result: 010000000147590ec01f6544e58b819acd5b6a79f6c82f2340466ee12da44a981d155bde36000000006a4730440220728701495c43115a6a3cc866e2f8674d6ef8b4aafb97c69eedb6f6926ece1cae022005912ca33d2d3d9d3de30bfd4913694ad48f43764f0a9ba9f95ace40099aaca2812102eeab727493118d3d69e072a4fe00a778ebcda881460a753c8a4d98afb3148bd9ffffffff0300ca9a3b000000001976a9149195ec5736ab4543d770fdc2ce75c3276027636d88ac00943577000000001976a914f6524c010c8669b7028fe11b276631b541f2936088acc0863277000000001976a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac00000000
     * sendrawtransaction result :"d8dc3d2613511109ab957d79aac796dd90e49e1a067cc45b025c0a0b5308d981"
     * 转账后地址余额：
     *  mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8 409.99900000
     *  miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK 140
     *  手续费：0.001
     */
    @Test
    public void testTransactionOneToMany() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "36de5b151d984aa42de16e4640232fc8f6796a5bcd9a818be544651fc00e5947",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);

        us.add(u);
        System.out.println(JSON.toJSONString(us));

        List<String> fromPrivateKeys = new ArrayList<>();
        fromPrivateKeys.add("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T");
        /**
         * 收钱地址列表
         */
        List<String> recevieAddrs = new ArrayList<>();
        recevieAddrs.add("mtnjsiWBftaaTGmM1X6aAnTT2dCJUzZ5Ep");
        recevieAddrs.add("n3yP7LEuT4bjjHizUyiNT21iUTa61QmnG8");
        /**
         * 收钱金额列表
         */
        List<Long> amounts = new ArrayList<>();
        amounts.add(10*100000000L);
        amounts.add(20*100000000L);
        /**
         * 费用列表
         */
        List<Long> fees = new ArrayList<>();
        fees.add(100000L);
        fees.add(100000L);

        String resultHex = createRawTransactionNew(fromPrivateKeys,"mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8", recevieAddrs,amounts, fees, us,"");
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }

    /**
     * 指定找零地址转账
     */
    @Test
    public void testTransactionPointTheChangeAddress() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "c02cfdfcd1942ada80cdecb8f7762f965c473649a42875b19d92131d451d3af1",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);
        us.add(u);
        System.out.println(JSON.toJSONString(us));

        //发送方私钥列表
        List<String> fromPrivateKeys = new ArrayList<>();
        fromPrivateKeys.add("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T");

        /**
         * 收钱地址列表
         */
        List<String> recevieAddrs = new ArrayList<>();
        recevieAddrs.add("mtnjsiWBftaaTGmM1X6aAnTT2dCJUzZ5Ep");
        /**
         * 收钱金额列表
         */
        List<Long> amounts = new ArrayList<>();
        amounts.add(10*100000000L);
        /**
         * 费用列表
         */
        List<Long> fees = new ArrayList<>();
        fees.add(100000L);

        String resultHex = createRawTransactionNew(fromPrivateKeys, "mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                recevieAddrs, amounts, fees, us,"n3yP7LEuT4bjjHizUyiNT21iUTa61QmnG8");
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }
    /**
     * 充值余额不足（接收金额 > 转账金额）
     * "error":{"code":-26,"message":"bad-txns-in-belowout, value in (50.00) < value out (60.00)"}
     */
    @Test
    public void testTransactionMoneyLess() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "c02cfdfcd1942ada80cdecb8f7762f965c473649a42875b19d92131d451d3af1",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);
        us.add(u);
        System.out.println(JSON.toJSONString(us));
        String resultHex = createRawTransaction("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T", "mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK", 6000000000L, 100000L, us);
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }

    /**
     * 充值余额不足（手续费不足）
     *"error":{"code":-25,"message":"Fee exceeds maximum configured by user (e.g. -maxtxfee, maxfeerate)"}
     */
    @Test
    public void testTransactionMoneyLess1() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "c02cfdfcd1942ada80cdecb8f7762f965c473649a42875b19d92131d451d3af1",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);
        us.add(u);
        System.out.println(JSON.toJSONString(us));
        String resultHex = createRawTransaction("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T", "mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK", 4900000000L, 1000000000L, us);
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }

    /**
     * 多对一组交易+转账交易+多私钥
     *
     *
     */
    @Test
    public void testTransactionManyToOneDifPrivateKey() {
        List<UnSpentUtxo> us = new ArrayList<>();
        UnSpentUtxo u = buildUtxo("mxx6rySkTDGnQXYr2HbvxSnjW6bBNbvHX8",
                "c8d84a75f8205870e11357d3c7fec4ac94733c46635a2d2449667be1a47938ef",0,
                "76a914bf3bb071547f3d0072732fb6393e89d1c5714a1588ac",0,50);

        UnSpentUtxo u1 = buildUtxo("miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK",
                "2832cc14dc3b29285389e692d4b8f7a9218eaf15d6625281a8a5bde290dc8f9b",0,
                "76a914257d1afe8f15c1ea03f1fd7ea1ffc2033cf8d76288ac",0,140);
        us.add(u);
        us.add(u1);
        System.out.println(JSON.toJSONString(us));

        List<String> fromPrivateKeys = new ArrayList<>();
        fromPrivateKeys.add("cQGDhYXGKSEMR3vZaWgPk5sfU99EUpA4SgMaVyccZMU3F49Kxx6T");
        fromPrivateKeys.add("cSwBtKxx9Fi16FioEUWVUuFnH2s1zbpTw5Hft1reAvJUac9RrFuQ");

        /**
         * 转出地址私钥列（此处默认与utxo列表对应）
         */
        List<String> recevieAddrs = new ArrayList<>();
        recevieAddrs.add("mtnjsiWBftaaTGmM1X6aAnTT2dCJUzZ5Ep");

        /**
         * 收钱金额列表
         */
        List<Long> amounts = new ArrayList<>();
        amounts.add(180*100000000L);
        /**
         * 费用列表
         */
        List<Long> fees = new ArrayList<>();
        fees.add(100000L);

        String resultHex = createRawTransactionNew(fromPrivateKeys,"miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK",
                recevieAddrs, amounts, fees, us,"miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK");
        System.out.println("createRawTransaction result: "+resultHex);
        /**
         * 广播上链
         */
        sendRawTransaction(resultHex);
    }

    /**
     * 构造一个UTXO交易对象
     * @param address
     * @param hash
     * @param height
     * @param script
     * @param vout
     * @param value 单位（BTC）
     * @return
     */
    private UnSpentUtxo buildUtxo(String address,String hash,int height,String script,int vout,long value){
        UnSpentUtxo utxo = new UnSpentUtxo();
        utxo.setAddress(address);
        utxo.setHash(hash);
        utxo.setHeight(height);
        utxo.setScript(script);
        utxo.setTxN(vout);//vout
        //单位（聪）
        utxo.setValue(value*100000000L);
        return utxo;
    }
    /**
     * 广播上链
     */
    private void sendRawTransaction(String transactiontHex){
        BitcoinJSONRPCClient bitcoinClient = getBitcoinClient();
        Object walletAddressAll = bitcoinClient.query("sendrawtransaction",transactiontHex);
        String publishResult = JSON.toJSONString(walletAddressAll);
        System.out.println("sendrawtransaction result :" + publishResult);
    }

    /**
     * @Title createRawTransaction
     * @param fromPrivateKeys 发送方私钥
     * @param formAddr 发送方地址
     * @param recevieAddrs 接收方地址
     * @param amounts 转账金额 --单位（聪）
     * @param fees  交易费用--单位（聪）
     * @param unUtxos 待花费
     * @return    参数
     * 1）正常交易（一对一交易）
     * 2）找零场景（设置找零地址）
     * 3）多对一交易场景
     * 4)一对多交易场景
     *
     * @throws
     */
    public String createRawTransactionNew(List<String> fromPrivateKeys,String formAddr, List<String> recevieAddrs,
                                          List<Long> amounts, List<Long> fees,List<UnSpentUtxo> unUtxos,String theChangeAddress) {
        if(CollectionUtils.isNotEmpty(unUtxos)){
            List<UTXO> utxos = new ArrayList<>();
            // String to a private key
            NetworkParameters params = RegTestParams.get();


            //计算输出交易对象
            Transaction tx = buildTransactionOut(params,formAddr,recevieAddrs,amounts,fees,unUtxos,theChangeAddress);
            // utxos is an array of inputs from my wallet
            for (UnSpentUtxo unUtxo : unUtxos) {
                utxos.add(new UTXO(Sha256Hash.wrap(unUtxo.getHash()),
                        unUtxo.getTxN(),
                        Coin.valueOf(unUtxo.getValue()),
                        unUtxo.getHeight(),
                        false,
                        new Script(Utils.HEX.decode(unUtxo.getScript())),
                        unUtxo.getAddress()));
            }
            //多UTXO多私钥场景
            for(int i = 0 ; i< utxos.size() ; i++){
                UTXO utxo = utxos.get(i);
                DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, fromPrivateKeys.get(i));
                ECKey key = dumpedPrivateKey.getKey();
                TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
                // YOU HAVE TO CHANGE THIS
                tx.addSignedInput(outPoint, utxo.getScript(), key, Transaction.SigHash.ALL, true);
            }

            Context context = new Context(params);
            tx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
            tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
            System.out.println("=== [BTC] sign success,hash is :{} ==="+ tx.getHashAsString());
            return new String(org.apache.commons.codec.binary.Hex.encodeHex(tx.bitcoinSerialize()));
        }
        return null;
    }

    /**
     * 组装多vout场景下的交易对象
     * @param params
     * @param formAddr
     * @param recevieAddrs
     * @param amounts
     * @param fees
     * @param unUtxos
     * @return
     */
    private Transaction buildTransactionOut(NetworkParameters params,String formAddr, List<String> recevieAddrs, List<Long> amounts, List<Long> fees,List<UnSpentUtxo> unUtxos,String theChangeAddress){
        Transaction tx = new Transaction(params);
        if(params == null){
            return null;
        }
        if(CollectionUtils.isEmpty(recevieAddrs) || CollectionUtils.isEmpty(amounts) || CollectionUtils.isEmpty(fees)){
            return null;
        }
        if(!(recevieAddrs.size() == amounts.size() && amounts.size() == fees.size())){
            return null;
        }
        for (int i = 0 ; i< recevieAddrs.size() ; i++){
            // 接收地址
            Address receiveAddress = Address.fromBase58(params, recevieAddrs.get(i));
            tx.addOutput(Coin.valueOf(amounts.get(i)), receiveAddress); // 转出
        }

        // 如果需要找零 消费列表总金额 - 已经转账的金额 - 手续费
        long value = unUtxos.stream().mapToLong(UnSpentUtxo::getValue).sum();

        //找零钱地址(默认找零给转账地址,非空时，指定目标找零地址)
        Address changeAddress = StringUtils.isEmpty(theChangeAddress) ? Address.fromBase58(params, formAddr) : Address.fromBase58(params, theChangeAddress) ;
        long amount = amounts.stream().mapToLong(Long::longValue).sum();
        long fee = fees.stream().mapToLong(Long::longValue).sum();
        //找零金额
        long leave  = value - amount - fee;
        if(leave > 0){
            tx.addOutput(Coin.valueOf(leave), changeAddress);
        }
        return tx;
    }


    /**
     * @Title createRawTransaction
     * @param fromPrivateKey 发送方私钥
     * @param formAddr 发送方地址
     * @param recevieAddr 接收方地址
     * @param amount 转账金额 --单位（聪）
     * @param fee  交易费用--单位（聪）
     * @param unUtxos 待花费
     * @return    参数
     * 1）正常交易（一对一交易）
     * 2）找零场景（设置找零地址）
     * 3）多对一交易场景
     * 4)一对多交易场景
     *
     * @throws
     */
    public String createRawTransaction(String fromPrivateKey,String formAddr, String recevieAddr, long amount, long fee,List<UnSpentUtxo> unUtxos) {
        if(CollectionUtils.isNotEmpty(unUtxos)){
            List<UTXO> utxos = new ArrayList<>();
            // String to a private key
            NetworkParameters params = RegTestParams.get();
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, fromPrivateKey);
            ECKey key = dumpedPrivateKey.getKey();
            // 接收地址
            Address receiveAddress = Address.fromBase58(params, recevieAddr);
            // 构建交易
            Transaction tx = new Transaction(params);
            tx.addOutput(Coin.valueOf(amount), receiveAddress); // 转出

            // 如果需要找零 消费列表总金额 - 已经转账的金额 - 手续费
            long value = unUtxos.stream().mapToLong(UnSpentUtxo::getValue).sum();
            Address toAddress = Address.fromBase58(params, formAddr);
            long leave  = value - amount - fee;
            if(leave > 0){
                tx.addOutput(Coin.valueOf(leave), toAddress);
            }
            // utxos is an array of inputs from my wallet
            for (UnSpentUtxo unUtxo : unUtxos) {
                utxos.add(new UTXO(Sha256Hash.wrap(unUtxo.getHash()),
                        unUtxo.getTxN(),
                        Coin.valueOf(unUtxo.getValue()),
                        unUtxo.getHeight(),
                        false,
                        new Script(Utils.HEX.decode(unUtxo.getScript())),
                        unUtxo.getAddress()));
            }
            for (UTXO utxo : utxos) {
                TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
                // YOU HAVE TO CHANGE THIS
                tx.addSignedInput(outPoint, utxo.getScript(), key, Transaction.SigHash.ALL, true);
            }
            Context context = new Context(params);
            tx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
            tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
            System.out.println("=== [BTC] sign success,hash is :{} ==="+ tx.getHashAsString());
            return new String(org.apache.commons.codec.binary.Hex.encodeHex(tx.bitcoinSerialize()));
        }
        return null;
    }

    /**
     * 获取BTC余额
     *
     * @param walletName 钱包名称
     * @param address    地址
     * @return 余额
     */
    @Override
    public double getBtcBalance(String walletName, String address) {
        try {
            List<JSONArray> jsonArrayList= getListAddressInfo(walletName);
            for(JSONArray jsonArray : jsonArrayList){
                if(jsonArray.getString(0).equals(address)){
                    return jsonArray.getDoubleValue(1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("钱包("+walletName+")地址("+address+")获取余额异常==>"+e.getMessage());
        }
        return 0;
    }

    /**
     * 查询待花费UTXO
     */
    @Test
    public void testGetUnSpent(){
        List<UTXO> utxos = getUnspent("");
        System.out.println(utxos);
    }

    /***
     * 获取未消费列表
     * @param address ：地址
     * @return
     */
    public List<UTXO> getUnspent(String address) {
        List<UTXO> utxos = Lists.newArrayList();
        try {
            List<Map> outputs = queryUnSpent();
            if (outputs == null || outputs.size() == 0) {
                System.out.println("交易异常，余额不足");
            }
            for (int i = 0; i < outputs.size(); i++) {
                Map outputsMap = outputs.get(i);
                String tx_hash = outputsMap.get("txid").toString();
                String tx_output_n = outputsMap.get("vout").toString();
                String script = outputsMap.get("scriptPubKey").toString();
                BigDecimal amount = (BigDecimal) outputsMap.get("amount");
                Long value = amount.longValue();
                UTXO utxo = new UTXO(Sha256Hash.wrap(tx_hash), Long.valueOf(tx_output_n), Coin.valueOf(value),
                        0, false, new Script(Hex.decode(script)));
                utxos.add(utxo);
            }
            return utxos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取BTC客户端
     * @return BTC客户端
     */
    private BitcoinJSONRPCClient getBitcoinClient(){
        BitcoinJSONRPCClient bitcoinClient=null;
        try {
            URL url = new URL("http://" + user + ':' + password + "@" + host + ":" + port + "");
            bitcoinClient = new BitcoinJSONRPCClient(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.err.println("获取BTC RPC错误==>"+e.getMessage());
        }
        return bitcoinClient;
    }
    /**
     * 获取所有钱包地址信息
     * @param walletName 钱包名称
     * @return 所有地址信息
     */
    private List<JSONArray> getListAddressInfo(String walletName){
        BitcoinJSONRPCClient bitcoinClient = getBitcoinClient();
        List<JSONArray> resultList=new ArrayList<>();
        try {
            Object walletAddressAll=bitcoinClient.query("listaddressgroupings");
            JSONArray jsonArrayAll= JSON.parseArray(JSON.toJSONString(walletAddressAll));
            for(int i=0;i<jsonArrayAll.size();i++){
                JSONArray walletArray=JSON.parseArray(jsonArrayAll.get(i).toString());
                for(int j=0;j<walletArray.size();j++){
                    JSONArray jsonArray= JSON.parseArray(walletArray.getString(j));
                    resultList.add(jsonArray);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("钱包("+walletName+")获取所有地址信息异常==>"+e.getMessage());
        }
        return resultList;
    }

    /**
     * 执行裸交易（线下签名）
     * @return
     */
    @Test
    public void createRawTransactionAndBroadCast(){
        List<Map> input_utxo = queryUnSpent();
        List<Map<String,String>> outPut = new ArrayList<>();
        Map<String,String> address_value = Maps.newHashMap();

        address_value.put("miwBAdXWnCV9UJAfVTd5ARDkqN5Tng1dqK","1");
        outPut.add(address_value);

        BitcoinJSONRPCClient bitcoinClient = getBitcoinClient();
        Object result = bitcoinClient.query("createrawtransaction",input_utxo,outPut);
        System.out.println("createrawtransaction result:" +result);

        //广播交易
        broadCastTransaction(result);
    }
    /**
     * 广播交易
     * @return
     */
    public String broadCastTransaction(Object hashValue){
        String publishResult = null;
        try{
            BitcoinJSONRPCClient bitcoinClient = getBitcoinClient();
            Object fundrawResult = bitcoinClient.query("fundrawtransaction",hashValue.toString());
            System.out.println("fundrawtransaction result :" + JSON.toJSONString(fundrawResult));

            Object signedHex = bitcoinClient.query("signrawtransactionwithwallet",hashValue.toString());
            JSONObject object = JSONObject.parseObject(JSON.toJSONString(signedHex));
            Map signResul = JSONObject.parseObject(object.toJSONString(),Map.class);

            System.out.println("signrawtransactionwithwallet result :" + object.toJSONString());

            Object walletAddressAll = bitcoinClient.query("sendrawtransaction",signResul.get("hex"));
            publishResult = JSON.toJSONString(walletAddressAll);
            System.out.println("sendrawtransaction result :" + publishResult);
        }catch (Exception e){
            e.printStackTrace();
        }
        return publishResult ;
    }
    private List<Map> queryUnSpent(){
        BitcoinJSONRPCClient bitcoinClient= getBitcoinClient();
        List<Map> unSpents = null;
        try {
            Object walletAddressAll = bitcoinClient.query("listunspent");
            JSONArray unspentOutputs = JSON.parseArray(JSON.toJSONString(walletAddressAll));
            System.out.println(JSON.toJSONString("listunspent result: "+walletAddressAll));
            unSpents = JSONObject.parseArray(unspentOutputs.toJSONString(), Map.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return unSpents;
    }


    private List<UtxoInput> buildUtxoInput(){
        List<UtxoInput> list = new ArrayList<>();
        try {
            List<Map> outputs = queryUnSpent();
            if (outputs == null || outputs.size() == 0) {
                System.out.println("交易异常，余额不足");
            }
            for (int i = 0; i < outputs.size(); i++) {
                Map outputsMap = outputs.get(i);
                String txid = outputsMap.get("txid").toString();
                String vout = outputsMap.get("vout").toString();

                UtxoInput utxoInput = new UtxoInput();
                utxoInput.setTxid(txid);
                utxoInput.setVout(vout);
                list.add(utxoInput);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        List<Long> amounts = new ArrayList<>();
        amounts.add(100L);
        amounts.add(50L);
        amounts.add(2L);
        long amount = amounts.stream().mapToLong(Long::longValue).sum();
        System.out.println(amount);
    }

}
