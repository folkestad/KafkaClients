import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import java.util.*

class ProducerKafkaClient(private val topic: String) {
    private val producer: KafkaProducer<String, String>
    private val props = Properties()

    init {
        val kafkaConfig: Config = ConfigFactory.load().getConfig("ktor.kafka")

        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.getString("bootstrap-servers")
        props[ProducerConfig.CLIENT_ID_CONFIG] = "${kafkaConfig.getString("client-id")}-$topic"
        props[ProducerConfig.MAX_BLOCK_MS_CONFIG] = "200"
        props[ProducerConfig.RETRIES_CONFIG] = "0"

        props[AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] =
            kafkaConfig.getString("schema-registry-url")

        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = kafkaConfig.getString("security-protocol")

        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = kafkaConfig.getString("ssl.truststore-location")
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = kafkaConfig.getString("ssl.truststore-password")

        val clientUsername = kafkaConfig.getString("sasl.jaas-username")
        val clientPassword = kafkaConfig.getString("sasl.jaas-password")
        val jaasConfig =
            "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                    "username=\"${clientUsername}\" password=\"${clientPassword}\";"
        props[SaslConfigs.SASL_JAAS_CONFIG] = jaasConfig
        props[SaslConfigs.SASL_MECHANISM] = kafkaConfig.getString("sasl.mechanism")

        producer = KafkaProducer(props)
    }

    fun produce(message: String) {
        val producerRecord = ProducerRecord<String, String>(topic, message)

        producer.send(
            producerRecord
        ) { _: RecordMetadata?, e: Exception? ->
            if (e != null) {
                throw e
            }
        }
    }
}