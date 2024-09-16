package com.project.apex.data.orders;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.apex.data.trades.Leg;

import java.util.List;

public class Order {
    @JsonProperty("class")
    private String className;
    @JsonProperty("createDate")
    @JsonAlias("create_date")
    private String createDate;
    private Integer id;
    private List<Leg> leg;
    @JsonAlias("reason_description")
    @JsonProperty("reasonDescription")
    private String reasonDescription;
    private String status;
    @JsonAlias("transaction_date")
    @JsonProperty("transactionDate")
    private String transactionDate;
    private Double last;

    public Order(String className, String createDate, Integer id, List<Leg> leg, String reasonDescription, String status, String transactionDate, Double last) {
        this.className = className;
        this.createDate = createDate;
        this.id = id;
        this.leg = leg;
        this.reasonDescription = reasonDescription;
        this.status = status;
        this.transactionDate = transactionDate;
        this.last = last;
    }

    // Getters and Setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Leg> getLeg() {
        return leg;
    }

    public void setLeg(List<Leg> leg) {
        this.leg = leg;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }
}
