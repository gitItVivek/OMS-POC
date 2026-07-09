package com.orderservice.event;

import com.orderservice.entity.SearchInterest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class SearchInterestsRegisteredEvent extends ApplicationEvent {

    private final List<SearchInterest> interests;

    public SearchInterestsRegisteredEvent(Object source, List<SearchInterest> interests) {
        super(source);
        this.interests = interests;
    }
}
