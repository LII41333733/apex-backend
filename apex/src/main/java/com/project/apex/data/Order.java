package com.project.apex.data;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
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
    private BigDecimal last;

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

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }
}
