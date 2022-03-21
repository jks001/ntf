package com.blockchain.test.entity;

public class UtxoInput {

    /**
     * txid The transaction id
     */
    private String txid;

    /**
     * The output number
     */
    private String vout;

    /**
     * optional, default=depends on the value of the 'replaceable' and 'locktime' arguments) The sequence number
     */
    private int sequence;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getVout() {
        return vout;
    }

    public void setVout(String vout) {
        this.vout = vout;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
