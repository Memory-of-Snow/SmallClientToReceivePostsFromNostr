package io.github.memory_of_snow.ParallelConnectionSandBox.message;

public class EOSEMessage extends Message{

    private String subscriptionId;

    public EOSEMessage() {
        command = "EOSE";
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    @Override
    public String getCommand() {
        return command;
    }
}
