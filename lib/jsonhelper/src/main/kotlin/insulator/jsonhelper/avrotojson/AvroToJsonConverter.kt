package insulator.jsonhelper.avrotojson

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import com.fasterxml.jackson.databind.ObjectMapper
import insulator.helper.runCatchingE
import insulator.helper.toEitherOfList
import org.apache.avro.Schema
import org.apache.avro.Schema.Type.ARRAY
import org.apache.avro.Schema.Type.BOOLEAN
import org.apache.avro.Schema.Type.BYTES
import org.apache.avro.Schema.Type.DOUBLE
import org.apache.avro.Schema.Type.ENUM
import org.apache.avro.Schema.Type.FLOAT
import org.apache.avro.Schema.Type.INT
import org.apache.avro.Schema.Type.LONG
import org.apache.avro.Schema.Type.NULL
import org.apache.avro.Schema.Type.RECORD
import org.apache.avro.Schema.Type.STRING
import org.apache.avro.Schema.Type.UNION
import org.apache.avro.generic.GenericRecord

open class AvroToJsonParsingException(message: String) : Throwable(message)
class AvroFieldParsingException(field: Any?, type: String) : AvroToJsonParsingException("Invalid field $field. Expected $type")
class UnsupportedTypeException(type: String) : AvroToJsonParsingException("Unsupported $type")

class AvroToJsonConverter(private val objectMapper: ObjectMapper) {

    fun parse(record: GenericRecord) =
        parseField(record, record.schema)
            .flatMap { objectMapper.runCatchingE { writeValueAsString(it) } }

    private fun parseField(field: Any?, schema: Schema): Either<AvroToJsonParsingException, Any?> =
        when (schema.type) {
            RECORD -> parseRecord(field, schema)
            BYTES -> parseBytes(field, schema)
            UNION -> parseUnion(field, schema)
            ARRAY -> parseArray(field, schema)
            NULL -> parseNull(field)
            BOOLEAN -> parseBoolean(field)
            STRING -> parseString(field)
            ENUM -> parseEnum(field)
            INT, LONG, FLOAT, DOUBLE -> parseNumber(field)
            // missing: MAP, FIXED
            else -> UnsupportedTypeException(schema.type.getName()).left()
        }

    private fun parseRecord(field: Any?, schema: Schema): Either<AvroToJsonParsingException, Any?> {
        if (field !is GenericRecord) return AvroFieldParsingException(field, "Record").left()
        val keySchema = schema.fields.map { it.name() to it.schema() }
        return keySchema
            .map { (name, schema) -> parseField(field[name], schema) }
            .toEitherOfList()
            .map { values -> keySchema.map { it.first }.zip(values).toMap() }
    }

    private fun parseUnion(field: Any?, schema: Schema) =
        schema.types.map { t -> parseField(field, t) }
            .let { attempts -> attempts.firstOrNull { it.isRight() } ?: attempts.first() }

    private fun parseArray(field: Any?, schema: Schema) =
        if (field is Collection<*>) field.map { parseField(it, schema.elementType) }.toEitherOfList()
        else AvroFieldParsingException(field, "Array").left()
}
