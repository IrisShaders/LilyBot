package net.irisshaders.lilybot.extensions.util

import com.kotlindiscord.kord.extensions.DISCORD_BLACK
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingColor
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import net.irisshaders.lilybot.utils.ComponentData
import net.irisshaders.lilybot.utils.DatabaseHelper

//todo This really just needs a full rework. I'm about 90% sure it's not properly adapted for cross guild work.
class RoleMenu : Extension() {
    override val name = "rolemenu"

	override suspend fun setup() {
		ephemeralSlashCommand(::RoleMenuArgs) {
			name = "role-menu"
			description = "Creates a menu that allows users to select a role."

			check { hasPermission(Permission.ManageMessages) }

			@Suppress("DuplicatedCode")
			action {
				val descriptionAppendix = "\n\nUse the button below to add/remove the ${arguments.role.mention} role."

				val targetChannel: GuildMessageChannelBehavior =
					if (arguments.channel != null) {
						val targetChannelId = arguments.channel!!.id
						guild?.getChannel(targetChannelId) as GuildMessageChannelBehavior
					} else {
						this.channel as GuildMessageChannelBehavior
					}

				targetChannel.createMessage {
					embed {
						title = arguments.title
						description = arguments.description + descriptionAppendix
						color = arguments.color
					}

					val components = components {
						ephemeralButton(row = 0) {
							label = "Add role"
							style = ButtonStyle.Success
							id = arguments.role.name + "add"

							val newComponent = ComponentData(
								arguments.role.name + "add",
								arguments.role.id.toString(),
								"add"
							)
							DatabaseHelper.putInComponents(newComponent)

							action { }
						}

						ephemeralButton(row = 0) {
							label = "Remove role"
							style = ButtonStyle.Danger
							id = arguments.role.name + "remove"

							val newComponent = ComponentData(
								arguments.role.name + "remove",
								arguments.role.id.toString(),
								"remove"
							)
							DatabaseHelper.putInComponents(newComponent)

							action { }
						}
					}

					components.removeAll()

					respond { content = "Role menu created." }

					// Try to get the action log from the config. If a config is not set, inform the user and return@action
					val actionLogId = DatabaseHelper.selectInConfig(guild!!.id.toString(), "modActionLog")
					if (actionLogId == null) {
						respond { content = "**Error:** Unable to access config for this guild! Is your configuration set?" }
						return@action
					}
					val actionLog = guild?.getChannel(Snowflake(actionLogId)) as GuildMessageChannelBehavior

					actionLog.createEmbed {
						color = DISCORD_BLACK
						title = "Role menu created."
						description =
							"A role menu for the ${arguments.role.mention} role was created in ${targetChannel.mention}"

						field {
							name = "Embed title:"
							value = arguments.title
							inline = false
						}
						field {
							name = "Embed description:"
							value = arguments.description + descriptionAppendix
							inline = false
						}
						footer {
							text = "Requested by ${user.asUser().tag}"
							icon = user.asUser().avatar?.url
						}
						timestamp = Clock.System.now()
					}
				}
			}
		}

		event<InteractionCreateEvent> {
			check { failIfNot(event.interaction is ButtonInteraction) }
			
			action {
				val interaction = event.interaction as ButtonInteraction
				val guild = kord.getGuild(interaction.data.guildId.value!!)!!
				val member = guild.getMember(interaction.user.id)

				// this is  a very dirty fix, so it doesn't conflict with log uploading
				var roleId: String?
				var addOrRemove: String?
				try {
					roleId = DatabaseHelper.selectInComponents(interaction.componentId, "roleId")
					addOrRemove = DatabaseHelper.selectInComponents(interaction.componentId, "addOrRemove")

				} catch (e: NullPointerException) {
					return@action
				}

				if (roleId == "null" || addOrRemove == "null") {return@action}

				val role = guild.getRole(Snowflake(roleId!!))

				if (addOrRemove == "add") {
					if (!member.hasRole(role)) {
						member.addRole(role.id)
						interaction.respondEphemeral { content = "Added the ${role.mention} role." }
					} else if (member.hasRole(role)) {
						interaction.respondEphemeral {
							content =
								"You already have the ${role.mention} role so it can't be added. If you don't, please contact a staff member."
						}
					}
				} else if (addOrRemove == "remove") {
					if (!member.hasRole(role)) {
						interaction.respondEphemeral {
							content =
								"You don't have the ${role.mention} role so it can't be removed. If you do, please contact a staff member."
						}
					} else if (member.hasRole(role)) {
						member.removeRole(role.id)
						interaction.respondEphemeral { content = "Removed the ${role.mention} role." }
					}
				}
			}
		}
	}

    inner class RoleMenuArgs : Arguments() {
		val role by role {
			name = "roles"
			description = "The roles to be selected."
		}
        val title by defaultingString {
            name = "title"
            description = "The title of the embed. Defaults to \"Role Selection Menu\""
			defaultValue = "Role selection menu"
        }
        val description by defaultingString {
            name = "description"
            description = "Text for the embed. Will be appended with a description of how to use the buttons."
			defaultValue = " "
        }
		val channel by optionalChannel {
			name = "channel"
			description = "The channel for the message to be sent in. Defaults to the channel executed in."
		}
		val color by defaultingColor {
			name = "color"
			description = "The color for the embed menu to be."
			defaultValue = DISCORD_BLACK
		}
    }
}
