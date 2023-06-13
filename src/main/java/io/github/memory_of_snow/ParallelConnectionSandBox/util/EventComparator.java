package io.github.memory_of_snow.ParallelConnectionSandBox.util;

import io.github.memory_of_snow.ParallelConnectionSandBox.event.Event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event e1,Event e2){
        return Long.compare(e1.getCreated_at(),e2.getCreated_at());
    }
}
