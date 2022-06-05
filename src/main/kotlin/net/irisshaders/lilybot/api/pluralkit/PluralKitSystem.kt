package net.irisshaders.lilybot.api.pluralkit

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PluralKitSystem(
	val id: String,
	val uuid: String,
	val name: String?,
	val description: String?,
	val tag: String?,
	val pronouns: String,
	@SerialName("avatar_url") val avatarUrl: String?,
	val banner: String?,
	val color: String?, // Ask the PK docs, not me
	val created: Instant,
	val privacy: PluralKitSystemPrivacy?
)
