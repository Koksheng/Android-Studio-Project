package com.koksheng.procleanservices.Model;

public class Rating {
    private String userPhone;
    private String cleaningId;
    private String rateValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String cleaningId, String rateValue, String comment) {
        this.userPhone = userPhone;
        this.cleaningId = cleaningId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getCleaningId() {
        return cleaningId;
    }

    public void setCleaningId(String cleaningId) {
        this.cleaningId = cleaningId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
