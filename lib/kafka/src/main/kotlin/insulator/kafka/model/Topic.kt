package insulator.kafka.model

data class Topic(
    val name: String,
    val isInternal: Boolean? = null,
    val partitionCount: Int = 0,
    val messageCount: Long? = null,
    val replicationFactor: Short = 0,
    val isCompacted: Boolean = false
) {
    companion object {
        fun empty() = Topic("")
    }
}

data class TopicConfiguration(
    val isCompacted: Boolean
)
