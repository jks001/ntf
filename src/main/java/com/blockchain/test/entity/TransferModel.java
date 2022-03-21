package com.blockchain.test.entity;

public class TransferModel {

    /**
     * 充值状态说明
     */
    private String message;

    /**
     * 充值装
     */
    private int status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
