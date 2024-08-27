package com.project.apex.data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Order {
    @JsonProperty("class")
    private String className;
    @JsonProperty("create_date")
    private String createDate;
    private long id;
    private List<Leg> leg;
    @JsonProperty("reason_description")
    private String reasonDescription;
    private String status;
    @JsonProperty("transaction_date")
    private String transactionDate;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
