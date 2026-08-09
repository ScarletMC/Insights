package dev.frankheijden.insights.commands.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.SenderMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CommandSenderMapper implements SenderMapper<CommandSourceStack, CommandSender> {

    private static final SimpleCommandExceptionType ENTITY_REQUIRED =
            new SimpleCommandExceptionType(new LiteralMessage("Command sender is not an entity"));
    private static final SimpleCommandExceptionType PLAYER_REQUIRED =
            new SimpleCommandExceptionType(new LiteralMessage("Command sender is not a player"));

    @Override
    public CommandSender map(CommandSourceStack source) {
        return source.getSender();
    }

    @SuppressWarnings("NonExtendableApiUsage")
    @Override
    public CommandSourceStack reverse(CommandSender sender) {
        return new CommandSourceStack() {
            @Override
            public Location getLocation() {
                if (sender instanceof Entity entity) {
                    return entity.getLocation();
                }

                var worlds = Bukkit.getWorlds();
                return new Location(worlds.isEmpty() ? null : worlds.getFirst(), 0, 0, 0); // Best effort lol
            }

            @Override
            public CommandSender getSender() {
                return sender;
            }

            @Override
            public @Nullable Entity getExecutor() {
                return sender instanceof Entity entity ? entity : null;
            }

            @Override
            public Player getPlayerOrThrow() throws CommandSyntaxException {
                if (sender instanceof Player player) {
                    return player;
                }
                throw PLAYER_REQUIRED.create();
            }

            @Override
            public Entity getEntityOrThrow() throws CommandSyntaxException {
                Entity entity = getExecutor();
                if (entity == null) {
                    throw ENTITY_REQUIRED.create();
                }
                return entity;
            }

            @Override
            public CommandSourceStack withLocation(Location location) {
                return CommandSenderMapper.this.reverse(sender).withLocation(location);
            }

            @Override
            public CommandSourceStack withExecutor(Entity e) {
                return CommandSenderMapper.this.reverse(sender).withExecutor(e);
            }
        };
    }
}
