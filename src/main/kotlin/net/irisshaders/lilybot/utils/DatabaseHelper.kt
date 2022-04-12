package net.irisshaders.lilybot.utils

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.irisshaders.lilybot.database
import org.litote.kmongo.eq

object DatabaseHelper {

	/**
	 * Using the provided [inputGuildId] and [inputColumn] a value or null will be returned from the database
	 *
	 * @param inputGuildId The ID of the guild the command was run in
	 * @param inputColumn The config value associated with that guild that you want
	 * @return null or the result from the database
	 * @author NoComment1105
	 * @author tempest15
	 */
	suspend fun getConfig(inputGuildId: Snowflake, inputColumn: String): Snowflake? {
		val collection = database.getCollection<ConfigData>()
		val selectedConfig = collection.findOne(ConfigData::guildId eq inputGuildId) ?: return null

		return when (inputColumn) {
			"guildId" -> selectedConfig.guildId
			"moderatorsPing" -> selectedConfig.moderatorsPing
			"modActionLog" -> selectedConfig.modActionLog
			"messageLogs" -> selectedConfig.messageLogs
			"joinChannel" -> selectedConfig.joinChannel
			"supportChannel" -> selectedConfig.supportChannel
			"supportTeam" -> selectedConfig.supportTeam
			else -> null
		}
	}

	/**
	 * Adds the given [newConfig] to the database
	 *
	 * @param newConfig The new config values you want to set
	 * @author tempest15
	 */
	suspend fun setConfig(newConfig: ConfigData) {
		val collection = database.getCollection<ConfigData>()
		collection.deleteOne(ConfigData:: guildId eq newConfig.guildId)
		collection.insertOne(newConfig)
	}

	/**
	 * Clears the config for the provided [inputGuildId]
	 *
	 * @param inputGuildId The ID of the guild the command was run in
	 * @author tempest15
	 */
	suspend fun clearConfig(inputGuildId: Snowflake) {
		val collection = database.getCollection<ConfigData>()
		collection.deleteOne(ConfigData:: guildId eq inputGuildId)
	}

	/**
	 * Gets the number of points the provided [inputUserId] has in the provided [inputGuildId] from the database
	 *
	 * @param inputUserId The ID of the user to get the point value for
	 * @param inputGuildId The ID of the guild the command was run in
	 * @return null or the result from the database
	 * @author tempest15
	 */
	suspend fun getWarn(inputUserId: Snowflake, inputGuildId: Snowflake): Int {
		val collection = database.getCollection<WarnData>()
		val selectedUserInGuild = collection.findOne(WarnData::userId eq inputUserId,
			WarnData::guildId eq inputGuildId)

		return if (selectedUserInGuild != null) {
			selectedUserInGuild.strikes!!
		} else {
			0
		}
	}

	/**
	 * Updates the number of points the provided [inputUserId] has in the provided [inputGuildId] in the database
	 *
	 * @param inputUserId The ID of the user to get the point value for
	 * @param inputGuildId The ID of the guild the command was run in
	 * @param remove Remove a warn strike, or add a warn strike
	 * @author tempest15
	 */
	suspend fun setWarn(inputUserId: Snowflake, inputGuildId: Snowflake, remove: Boolean) {
		val currentStrikes = getWarn(inputUserId, inputGuildId)
		val collection = database.getCollection<WarnData>()
		collection.deleteOne(WarnData::userId eq inputUserId, WarnData::guildId eq inputGuildId)
		collection.insertOne(
			WarnData(
				inputUserId,
				inputGuildId,
				if (!remove) currentStrikes.plus(1) else currentStrikes.minus(1)
			)
		)
	}

	/**
	 * Using the provided [inputComponentId] and [inputColumn] a value or null will be returned from the database
	 *
	 * @param inputComponentId The ID of the component the event was triggered with
	 * @param inputColumn The config value associated with that component that you want
	 * @return null or the result from the database
	 * @author tempest15
	 */
	suspend fun getComponent(inputComponentId: String, inputColumn: String): Any? {
		// this returns any because it can return either a string or a snowflake
		val collection = database.getCollection<ComponentData>()
		val selectedComponent = collection.findOne(ComponentData:: componentId eq inputComponentId)

		return when (inputColumn) {
			"componentId" -> selectedComponent!!.componentId
			"roleId" -> selectedComponent!!.roleId
			"addOrRemove" -> selectedComponent!!.addOrRemove
			else -> null
		}
	}

	/**
	 * Add the given [newComponent] to the database
	 *
	 * @param newComponent The data for the component to be added
	 * @author tempest15
	 */
	suspend fun setComponent(newComponent: ComponentData) {
		val collection = database.getCollection<ComponentData>()
		collection.deleteOne(ComponentData:: componentId eq newComponent.componentId)
		collection.insertOne(newComponent)
	}

	/**
	 * Gets Lily's status from the database
	 *
	 * @return null or the set status in the database
	 * @author NoComment1105
	 */
	fun getStatus(): String {
		var selectedStatus: StatusData?
		runBlocking {
			val collection = database.getCollection<StatusData>()
			selectedStatus = collection.findOne(StatusData::key eq "LilyStatus")
		}
		return selectedStatus?.status ?: "Iris"
	}

	/**
	 * Add the given [newStatus] to the database
	 *
	 * @param [newStatus] The new status you wish to set
	 * @author NoComment1105
	 */
	suspend fun setStatus(newStatus: String) {
		val collection = database.getCollection<StatusData>()
		collection.deleteOne(StatusData::key eq "LilyStatus")
		collection.insertOne(StatusData("LilyStatus", newStatus))
	}

	suspend fun getTag(guildId: Snowflake, name: String, column: String): Any? {
		val selectedTag: TagsData?
		val collection = database.getCollection<TagsData>()
		selectedTag = collection.findOne(TagsData::guildId eq guildId, TagsData::name eq name)

		return when (column) {
			"guildId" -> selectedTag!!.guildId
			"name" -> selectedTag!!.name
			"tagTitle" -> selectedTag!!.tagTitle
			"tagValue" -> selectedTag!!.tagValue
			else -> null
		}
	}

	suspend fun getSubTags(guildId: Snowflake, parentTag: String, name: String, column: String): Any? {
		val parentCollection = database.getCollection<TagsData>()
		val subTagsCollection = database.getCollection<SubTagsData>()
		val selectedSubTagParent: TagsData? = parentCollection.findOne(
			TagsData::guildId eq guildId,
			TagsData::parentId eq parentTag)
		val selectedSubTag: SubTagsData? = subTagsCollection.findOne(
			SubTagsData::parentTag eq parentTag,
			SubTagsData::name eq name
		)

		var returnValue: Any? = null
		if (selectedSubTag!!.parentTag == selectedSubTagParent!!.parentId) {
			returnValue = when (column) {
				"guildId" -> selectedSubTagParent.guildId
				"name" -> selectedSubTag.name
				"tagTitle" -> selectedSubTag.tagTitle
				"tagValue" -> selectedSubTag.tagValue
				else -> null
			}
		}
		return returnValue
	}

	suspend fun setTag(inputGuildId: Snowflake, name: String, tagTitle: String, tagValue: String,) {
		val collection = database.getCollection<TagsData>()
		collection.insertOne(TagsData(inputGuildId, name, tagTitle, tagValue, name))
	}

	suspend fun setSubTag(inputGuildId: Snowflake, parentTag: String, name: String, tagTitle: String?, tagValue: String?) {
		val collection = database.getCollection<SubTagsData>()
		collection.insertOne(SubTagsData(inputGuildId, parentTag, name, tagTitle, tagValue))
	}

	suspend fun deleteTag(inputGuildId: Snowflake, name: String) {
		val collection = database.getCollection<TagsData>()
		collection.deleteOne(TagsData::guildId eq inputGuildId, TagsData::name eq name)
	}
}

// Note that all values should always be nullable in case the database is empty.

@Serializable
data class ConfigData (
	val guildId: Snowflake?,
	val moderatorsPing: Snowflake?,
	val modActionLog: Snowflake?,
	val messageLogs: Snowflake?,
	val joinChannel: Snowflake?,
	val supportChannel: Snowflake?,
	val supportTeam: Snowflake?,
)

@Serializable
data class WarnData (
	val userId: Snowflake?,
	val guildId: Snowflake?,
	val strikes: Int?
)

@Serializable
data class ComponentData (
	val componentId: String?,
	val roleId: Snowflake?,
	val addOrRemove: String?
)

@Serializable
data class StatusData (
	val key: String?, // this is just so we can find the status and should always be set to "LilyStatus"
	val status: String?
)

@Serializable
data class TagsData (
	val guildId: Snowflake?,
	val name: String?,
	val tagTitle: String?,
	val tagValue: String?,
	val parentId: String?
)

@Serializable
data class SubTagsData (
	val guildId: Snowflake?,
	val parentTag: String?,
	val name: String?,
	val tagTitle: String?,
	val tagValue: String?
)
