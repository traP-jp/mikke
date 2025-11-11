package jp.trap.mikke.features.file.domain.model

import kotlin.uuid.Uuid

@JvmInline
value class FileId(
    val value: Uuid,
) {
    override fun toString(): String = value.toString()
}
