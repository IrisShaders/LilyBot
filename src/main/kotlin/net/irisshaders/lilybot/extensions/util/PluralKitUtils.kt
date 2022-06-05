package net.irisshaders.lilybot.extensions.util

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.create.embed
import net.irisshaders.lilybot.api.pluralkit.PluralKit

class PluralKitUtils : Extension() {
	override val name = "pluralkit-utls"

	override suspend fun setup() {
		ephemeralMessageCommand {
			name = "Query PK user"
			locking = true

			action {
				if (!event.interaction.getTarget().author.isNullOrBot()) {
					respond {
						content = "This is not a PluralKit user!"
					}
					return@action
				}

				val userId = if (PluralKit.getProxiedMessageAuthorId(event.interaction.getTarget().webhookId!!) != null) {
					PluralKit.getProxiedMessageAuthorId(event.interaction.getTarget().webhookId!!)
				} else {
					respond { content = "Unable to find PluralKit user!" }
					return@action
				}

				val user = UserBehavior(userId!!, this@ephemeralMessageCommand.kord)

				user.fetchUser().dm {
					content = "Information about ${event.interaction.getTarget().author!!.tag}"
					embed {
						field {
							name = "Main account"
							value = "${user.asUser().mention} - ${user.asUser().tag}"
						}
						field {
							value = "${PluralKit.getSystem(userId)}"
						}
					}
				}
			}
		}
	}
}
