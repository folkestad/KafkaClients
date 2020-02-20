import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.CreateTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import java.util.*

object AdminKafkaClient {
    private val client: AdminClient
    private val props = Properties()

    init {
        val kafkaConfig: Config = ConfigFactory.load().getConfig("ktor.kafka")

        props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.getString("bootstrap-servers")
        props[AdminClientConfig.CLIENT_ID_CONFIG] = kafkaConfig.getString("admin-client-id")

        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = kafkaConfig.getString("ssl.truststore-location")
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = kafkaConfig.getString("ssl.truststore-password")

        val clientUsername = kafkaConfig.getString("sasl.jaas-admin-username")
        val clientPassword = kafkaConfig.getString("sasl.jaas-admin-password")
        val jaasConfig =
            "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"${clientUsername}\" password=\"${clientPassword}\";"
        props[SaslConfigs.SASL_JAAS_CONFIG] = jaasConfig
        props[SaslConfigs.SASL_MECHANISM] = kafkaConfig.getString("sasl.mechanism")

        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = kafkaConfig.getString("security-protocol")

        client = AdminClient.create(props)
    }

    fun createTopic(topic: String, numPartitions: Int = 15, replicationFactor: Short = 3) {
        try {
            val newTopic = NewTopic(topic, numPartitions, replicationFactor)
            val createTopicsResult: CreateTopicsResult = client.createTopics(Collections.singleton(newTopic))
            createTopicsResult.values()[topic]!!.get()
        } catch (e: Exception) {
            throw e
        }
    }
}