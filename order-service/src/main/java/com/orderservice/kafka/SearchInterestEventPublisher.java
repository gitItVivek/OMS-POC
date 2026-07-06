package com.orderservice.kafka;

import com.orderservice.dto.SearchInterestRegisteredEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchInterestEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(SearchInterestRegisteredEventDto event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    OmsKafkaTopics.SEARCH_INTEREST_REGISTERED_EVENT,
                    event.getCustomerId().toString(),
                    json);
            log.info("Published search interest event interestId={} customerId={} key={}",
                    event.getInterestId(), event.getCustomerId(), event.getInterestKey());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish search interest event", e);
        }
    }
}
