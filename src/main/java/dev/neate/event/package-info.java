/**
 * Event Module - Kafka event production.
 * 
 * Public API:
 * - None (this module only consumes events and produces to Kafka)
 * 
 * Internal (not exposed):
 * - CountryKafkaProducer
 * - CountryEnrichedEventListener
 * - Kafka configuration
 * 
 * This module listens for enrichment events and produces messages to Kafka.
 * All components are internal to this module.
 */
package dev.neate.event;
