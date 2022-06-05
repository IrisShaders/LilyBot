package net.irisshaders.lilybot.api.pluralkit

import kotlinx.serialization.Serializable

@Serializable
data class PluralKitProxyTag(
	val prefix: String?,
	val suffix: String?
)
