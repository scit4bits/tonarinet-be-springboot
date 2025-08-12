package org.scit4bits.tonarinetserver.dto;

public class SimpleResponse {
    private String reply;

    public SimpleResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}