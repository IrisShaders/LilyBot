package net.irisshaders.lilybot.api.pluralkit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PluralKitMemberPrivacy(
	val visibility: Boolean,
	@SerialName("name_privacy") val namePrivacy: Boolean,
	@SerialName("description_privacy") val descriptionPrivacy: Boolean,
	@SerialName("birthday_privacy") val birthdayPrivacy: Boolean,
	@SerialName("pronoun_privacy") val pronounPrivacy: Boolean,
	@SerialName("avatar_privacy") val avatarPrivacy: Boolean,
	@SerialName("metadata_privacy") val metadataPrivacy: Boolean,
)
