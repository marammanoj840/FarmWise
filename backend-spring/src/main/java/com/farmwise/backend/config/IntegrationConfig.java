package com.farmwise.backend.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Configuration
public class IntegrationConfig {

    @Bean
    public RestClient integrationRestClient(IntegrationProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = Math.max(1000, props.getWeather().getTimeoutMs());
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().requestFactory(factory).build();
    }

    @Component
    @ConfigurationProperties(prefix = "app.integrations")
    public static class IntegrationProperties {

        private Weather weather = new Weather();
        private Ai ai = new Ai();
        private Market market = new Market();
        private Payment payment = new Payment();
        private Email email = new Email();
        private Sms sms = new Sms();
        private Storage storage = new Storage();
        private Maps maps = new Maps();

        public Weather getWeather() {
            return weather;
        }

        public void setWeather(Weather weather) {
            this.weather = weather;
        }

        public Ai getAi() {
            return ai;
        }

        public void setAi(Ai ai) {
            this.ai = ai;
        }

        public Market getMarket() {
            return market;
        }

        public void setMarket(Market market) {
            this.market = market;
        }

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }

        public Email getEmail() {
            return email;
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Sms getSms() {
            return sms;
        }

        public void setSms(Sms sms) {
            this.sms = sms;
        }

        public Storage getStorage() {
            return storage;
        }

        public void setStorage(Storage storage) {
            this.storage = storage;
        }

        public Maps getMaps() {
            return maps;
        }

        public void setMaps(Maps maps) {
            this.maps = maps;
        }

        public static class Weather {
            private String provider = "open-meteo";
            private String baseUrl = "https://api.open-meteo.com/v1";
            private String geocodingUrl = "https://geocoding-api.open-meteo.com/v1";
            private int timeoutMs = 5000;

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getBaseUrl() {
                return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public String getGeocodingUrl() {
                return geocodingUrl;
            }

            public void setGeocodingUrl(String geocodingUrl) {
                this.geocodingUrl = geocodingUrl;
            }

            public int getTimeoutMs() {
                return timeoutMs;
            }

            public void setTimeoutMs(int timeoutMs) {
                this.timeoutMs = timeoutMs;
            }
        }

        public static class Ai {
            private String provider = "none";
            private String apiUrl = "";
            private String apiKey = "";
            private String model = "";

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getApiUrl() {
                return apiUrl;
            }

            public void setApiUrl(String apiUrl) {
                this.apiUrl = apiUrl;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public boolean isConfigured() {
                return provider != null && !"none".equalsIgnoreCase(provider)
                        && apiKey != null && !apiKey.isBlank();
            }
        }

        public static class Market {
            private String provider = "sample";
            private String apiUrl = "";
            private String apiKey = "";

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getApiUrl() {
                return apiUrl;
            }

            public void setApiUrl(String apiUrl) {
                this.apiUrl = apiUrl;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public boolean isLiveConfigured() {
                return apiKey != null && !apiKey.isBlank() && apiUrl != null && !apiUrl.isBlank();
            }
        }

        public static class Payment {
            private String provider = "unconfigured";
            private String keyId = "";
            private String keySecret = "";

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getKeyId() {
                return keyId;
            }

            public void setKeyId(String keyId) {
                this.keyId = keyId;
            }

            public String getKeySecret() {
                return keySecret;
            }

            public void setKeySecret(String keySecret) {
                this.keySecret = keySecret;
            }

            public boolean isConfigured() {
                return provider != null
                        && !"unconfigured".equalsIgnoreCase(provider)
                        && keyId != null && !keyId.isBlank()
                        && keySecret != null && !keySecret.isBlank();
            }
        }

        public static class Email {
            private String provider = "log";
            private String fromAddress = "noreply@farmwise.local";
            private String smtpHost = "";
            private int smtpPort = 587;

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getFromAddress() {
                return fromAddress;
            }

            public void setFromAddress(String fromAddress) {
                this.fromAddress = fromAddress;
            }

            public String getSmtpHost() {
                return smtpHost;
            }

            public void setSmtpHost(String smtpHost) {
                this.smtpHost = smtpHost;
            }

            public int getSmtpPort() {
                return smtpPort;
            }

            public void setSmtpPort(int smtpPort) {
                this.smtpPort = smtpPort;
            }
        }

        public static class Sms {
            private String provider = "log";
            private String apiKey = "";

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }
        }

        public static class Storage {
            private String provider = "local-metadata";
            private String bucket = "";
            private String region = "";
            private String endpoint = "";
            private String accessKey = "";
            private String secretKey = "";
            private String publicBaseUrl = "";
            private long maxFileSizeBytes = 10_485_760L; // 10 MB

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getBucket() {
                return bucket;
            }

            public void setBucket(String bucket) {
                this.bucket = bucket;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            public String getPublicBaseUrl() {
                return publicBaseUrl;
            }

            public void setPublicBaseUrl(String publicBaseUrl) {
                this.publicBaseUrl = publicBaseUrl;
            }

            public long getMaxFileSizeBytes() {
                return maxFileSizeBytes;
            }

            public void setMaxFileSizeBytes(long maxFileSizeBytes) {
                this.maxFileSizeBytes = maxFileSizeBytes;
            }

            public boolean isS3Configured() {
                return bucket != null && !bucket.isBlank()
                        && accessKey != null && !accessKey.isBlank()
                        && secretKey != null && !secretKey.isBlank();
            }
        }

        public static class Maps {
            private String provider = "open-meteo-geo";
            private String apiKey = "";

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }
        }
    }
}
