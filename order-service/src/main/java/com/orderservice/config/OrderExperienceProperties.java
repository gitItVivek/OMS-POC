package com.orderservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.experience")
@Getter
@Setter
public class OrderExperienceProperties {

    private Search search = new Search();
    private Dashboard dashboard = new Dashboard();

    @Getter
    @Setter
    public static class Search {
        private int maxInterestsPerSearch;
    }

    @Getter
    @Setter
    public static class Dashboard {
        private int productLimit;
        private int historyLookback;
        private int categoryLookback;
    }
}
