package net.irisshaders.lilybot.api.pluralkit

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PluralKitMember(
	val id: String,
	val uuid: String,
	val name: String,
	@SerialName("display_name") val displayName: String?,
	val color: String?, // Ask the PK docs, not me
	val birthday: String?,
	val pronouns: String?,
	@SerialName("avatar_url") val avatarUrl: String?,
	val banner: String?,
	val description: String?,
	val created: Instant?,
	@SerialName("proxy_tags") val proxyTags: List<PluralKitProxyTag>,
	@SerialName("keep_proxy") val keepProxy: Boolean,
	val privacy: PluralKitMemberPrivacy?
)
