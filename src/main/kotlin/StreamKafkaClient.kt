import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.*


class StreamKafkaClient {
    val props = Properties()
    val builder = StreamsBuilder()

    init {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application")
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String())
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String())

    }

    fun start() {
        builder.stream<String, String>("my-input-topic")
            .mapValues { value -> "$value got mapped" }
            .to("my-output-topic")

        val streams = KafkaStreams(builder.build(), props)
        streams.start()
    }
}
