package dev.ftb.mods.ftbteams.data;

import com.mojang.brigadier.Command;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerTeam extends Team {
	public ServerTeam(TeamManager manager, UUID id) {
		super(manager, id);
	}

	@Override
	public TeamType getType() {
		return TeamType.SERVER;
	}

	public int delete(CommandSourceStack source) {
		markDirty();
		manager.save();
		manager.teamMap.remove(getId());
		String fn = getId() + ".snbt";

		try {
			Path dir = manager.server.getWorldPath(TeamManager.FOLDER_NAME).resolve("deleted");

			if (Files.notExists(dir)) {
				Files.createDirectories(dir);
			}

			Files.move(manager.server.getWorldPath(TeamManager.FOLDER_NAME).resolve("server/" + fn), dir.resolve(fn));
		} catch (IOException e) {
			e.printStackTrace();

			try {
				Files.deleteIfExists(manager.server.getWorldPath(TeamManager.FOLDER_NAME).resolve("server/" + fn));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		source.sendSuccess(Component.translatable("ftbteams.message.deleted_server_team", getStringID()), true);
		manager.markDirty();
		manager.syncTeamsToAll(this);
		TeamEvent.DELETED.invoker().accept(new TeamEvent(this));
		return Command.SINGLE_SUCCESS;
	}
}
