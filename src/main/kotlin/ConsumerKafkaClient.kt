import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.*
import java.time.Duration
import java.util.regex.Pattern
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs

class ConsumerKafkaClient {
    private val props = Properties()
    private val consumer: KafkaConsumer<String, String>
    private val kafkaConfig: Config = ConfigFactory.load().getConfig("kafka")

    init {
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.getString("bootstrap-servers")
        props[ConsumerConfig.GROUP_ID_CONFIG] = kafkaConfig.getString("group-id")
        props[ConsumerConfig.CLIENT_ID_CONFIG] = kafkaConfig.getString("client-id")
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java

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

        consumer = KafkaConsumer(props)
    }

    fun consume() {
        consumer.subscribe(Pattern.compile("event-stream-*"))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            records.forEach {
                println(it.offset())
            }
        }
    }
}