package dev.neate.event.internal;

import dev.neate.domain.Country;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for CountryKafkaProducer.
 * 
 * Verifies that:
 * - Country events are sent to Kafka with correct topic and key
 * - Success cases are logged appropriately
 * - Failures throw KafkaException for retry
 * - Country ID is used as message key
 */
class CountryKafkaProducerTest {

    private KafkaTemplate<String, Object> mockKafkaTemplate;
    private CountryKafkaProducer producer;

    @BeforeEach
    void setUp() {
        mockKafkaTemplate = mock(KafkaTemplate.class);
        producer = new CountryKafkaProducer(mockKafkaTemplate);
    }

    @Test
    void sendsCountryEventSuccessfully() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("United Kingdom", "GB");
        country.setId(countryId);
        country.setPopulation("67000000");
        country.setCurrency("GBP");
        country.setLanguage("English");
        country.setValidCountry(true);

        ProducerRecord<String, Object> producerRecord = 
            new ProducerRecord<>("country-events", countryId.toString(), country);
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition("country-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        
        CompletableFuture<SendResult<String, Object>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(mockKafkaTemplate.send(eq("country-events"), eq(countryId.toString()), eq(country)))
            .thenReturn(future);

        // When
        producer.sendCountryEvent(country);

        // Then
        verify(mockKafkaTemplate).send("country-events", countryId.toString(), country);
    }

    @Test
    void usesCountryIdAsKey() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("France", "FR");
        country.setId(countryId);

        ProducerRecord<String, Object> producerRecord = 
            new ProducerRecord<>("country-events", countryId.toString(), country);
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition("country-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        
        CompletableFuture<SendResult<String, Object>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendCountryEvent(country);

        // Then
        verify(mockKafkaTemplate).send(eq("country-events"), eq(countryId.toString()), eq(country));
    }

    @Test
    void sendsToCorrectTopic() {
        // Given
        Country country = new Country("Germany", "DE");
        country.setId(UUID.randomUUID());

        ProducerRecord<String, Object> producerRecord = 
            new ProducerRecord<>("country-events", country.getId().toString(), country);
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition("country-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        
        CompletableFuture<SendResult<String, Object>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendCountryEvent(country);

        // Then
        verify(mockKafkaTemplate).send(eq("country-events"), any(), any());
    }

    @Test
    void throwsKafkaExceptionOnFailure() {
        // Given
        Country country = new Country("Spain", "ES");
        country.setId(UUID.randomUUID());

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka connection failed"));
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When/Then
        assertThatThrownBy(() -> producer.sendCountryEvent(country))
            .isInstanceOf(KafkaException.class)
            .hasMessageContaining("Failed to send country event to Kafka");
    }

    @Test
    void sendsCompleteCountryData() {
        // Given
        UUID countryId = UUID.randomUUID();
        Country country = new Country("Italy", "IT");
        country.setId(countryId);
        country.setPopulation("60000000");
        country.setCurrency("EUR");
        country.setLanguage("Italian");
        country.setValidCountry(true);

        ProducerRecord<String, Object> producerRecord = 
            new ProducerRecord<>("country-events", countryId.toString(), country);
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition("country-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        
        CompletableFuture<SendResult<String, Object>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendCountryEvent(country);

        // Then
        verify(mockKafkaTemplate).send(eq("country-events"), eq(countryId.toString()), eq(country));
    }

    @Test
    void sendsMultipleCountries() {
        // Given
        Country country1 = new Country("Portugal", "PT");
        country1.setId(UUID.randomUUID());
        
        Country country2 = new Country("Belgium", "BE");
        country2.setId(UUID.randomUUID());

        ProducerRecord<String, Object> producerRecord = 
            new ProducerRecord<>("country-events", "key", new Object());
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition("country-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        
        CompletableFuture<SendResult<String, Object>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendCountryEvent(country1);
        producer.sendCountryEvent(country2);

        // Then
        verify(mockKafkaTemplate, times(2)).send(any(), any(), any());
    }

    @Test
    void propagatesExceptionForRetry() {
        // Given
        Country country = new Country("Netherlands", "NL");
        country.setId(UUID.randomUUID());

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Network error"));
        
        when(mockKafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When/Then - exception should be thrown for Spring Modulith retry
        assertThatThrownBy(() -> producer.sendCountryEvent(country))
            .isInstanceOf(KafkaException.class);
    }
}
