package io.github.memory_of_snow.ParallelConnectionSandBox.message;

import io.github.memory_of_snow.ParallelConnectionSandBox.event.Event;

public class EventMessage extends Message{

    private String subscriptionId;

    private Event innerEvent;

    public EventMessage() {
        command = "EVENT";
    }

    @Override
    public String getCommand() {
        return command;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Event getInnerEvent() {
        return innerEvent;
    }

    public void setInnerEvent(Event innerEvent) {
        this.innerEvent = innerEvent;
    }
}
