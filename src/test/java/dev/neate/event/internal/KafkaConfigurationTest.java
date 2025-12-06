package dev.neate.event.internal;

import dev.neate.KafkaTestcontainersConfiguration;
import dev.neate.MongoTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for KafkaConfiguration.
 * 
 * Verifies that:
 * - Kafka configuration is correctly loaded
 * - KafkaTemplate bean is available
 * - Connection to Kafka can be established
 */
@SpringBootTest
@Import({MongoTestcontainersConfiguration.class, KafkaTestcontainersConfiguration.class})
class KafkaConfigurationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void kafkaTemplateIsAvailable() {
        // Then
        assertThat(kafkaTemplate).isNotNull();
    }

    @Test
    void canConnectToKafka() {
        // Given
        String testTopic = "test-topic";
        String testKey = "test-key";
        String testValue = "test-value";

        // When/Then - should not throw exception
        kafkaTemplate.send(testTopic, testKey, testValue);
    }

}
