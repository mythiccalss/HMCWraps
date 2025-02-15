package de.skyslycer.hmcwraps.actions.register;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleTimes;
import com.owen1212055.particlehelper.api.particle.MultiParticle;
import com.owen1212055.particlehelper.api.particle.Particle;
import com.owen1212055.particlehelper.api.particle.types.*;
import com.owen1212055.particlehelper.api.particle.types.dust.transition.TransitionDustParticle;
import com.owen1212055.particlehelper.api.particle.types.note.MultiNoteParticle;
import com.owen1212055.particlehelper.api.particle.types.velocity.VelocityParticle;
import com.owen1212055.particlehelper.api.particle.types.vibration.VibrationParticle;
import com.owen1212055.particlehelper.api.type.Particles;
import de.skyslycer.hmcwraps.HMCWrapsPlugin;
import de.skyslycer.hmcwraps.actions.Action;
import de.skyslycer.hmcwraps.actions.ActionMethod;
import de.skyslycer.hmcwraps.actions.information.ActionInformation;
import de.skyslycer.hmcwraps.actions.information.GuiActionInformation;
import de.skyslycer.hmcwraps.actions.information.WrapActionInformation;
import de.skyslycer.hmcwraps.actions.information.WrapGuiActionInformation;
import de.skyslycer.hmcwraps.gui.GuiBuilder;
import de.skyslycer.hmcwraps.messages.Messages;
import de.skyslycer.hmcwraps.serialization.wrap.Wrap;
import de.skyslycer.hmcwraps.serialization.wrap.range.RangeSettings;
import de.skyslycer.hmcwraps.serialization.wrap.range.ValueRangeSettings;
import de.skyslycer.hmcwraps.util.ListUtil;
import de.skyslycer.hmcwraps.util.StringUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class DefaultActionRegister {

    private final HMCWrapsPlugin plugin;

    public DefaultActionRegister(HMCWrapsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        registerParticle();
        registerParticleMulti();
        registerSound();
        registerTitle();
        registerSubtitle();
        registerActionBar();
        registerMessage();
        registerCommand();
        registerConsoleCommand();
        registerScrollForth();
        registerScrollBack();
        registerUnwrap();
        registerClose();
        registerFilterToggle();
        registerFavorite();
        registerClearFavorites();
        registerPreview();
        registerWrap();
    }

    private void registerScrollForth() {
        ActionMethod consumer = (actionInformation) -> {
            if (actionInformation instanceof GuiActionInformation guiActionInformation) {
                guiActionInformation.getGui().next();
            }
        };
        plugin.getActionHandler().subscribe(Action.SCROLL_FORTH, consumer);
        plugin.getActionHandler().subscribe(Action.NEXT_PAGE, consumer);
    }

    private void registerScrollBack() {
        ActionMethod consumer = (actionInformation) -> {
            if (actionInformation instanceof GuiActionInformation guiActionInformation) {
                guiActionInformation.getGui().previous();
            }
        };
        plugin.getActionHandler().subscribe(Action.SCROLL_BACK, consumer);
        plugin.getActionHandler().subscribe(Action.PREVIOUS_PAGE, consumer);
    }

    private void registerUnwrap() {
        plugin.getActionHandler().subscribe(Action.UNWRAP, (actionInformation) -> {
            var player = actionInformation.getPlayer();
            var wrap = plugin.getWrapper().getWrap(player.getInventory().getItemInMainHand());
            player.getInventory().setItemInMainHand(plugin.getWrapper().removeWrap(player.getInventory().getItemInMainHand(), player, true));
            player.getOpenInventory().close();
            plugin.getMessageHandler().send(player, Messages.REMOVE_WRAP);
            if (wrap != null) {
                plugin.getActionHandler().pushUnwrap(wrap, player);
                plugin.getActionHandler().pushVirtualUnwrap(wrap, player);
            }
        });
    }

    private void registerClose() {
        plugin.getActionHandler().subscribe(Action.CLOSE_INVENTORY, (actionInformation) -> {
            if (actionInformation instanceof GuiActionInformation guiActionInformation) {
                guiActionInformation.getGui().close(actionInformation.getPlayer());
            }
        });
    }

    private void registerParticle() {
        plugin.getActionHandler().subscribe(Action.PARTICLE, (information) -> doParticle(information, false));
    }

    private void registerParticleMulti() {
        plugin.getActionHandler().subscribe(Action.PARTICLE_MULTI, (information) -> doParticle(information, true));
    }

    private void doParticle(ActionInformation information, boolean multi) {
        var split = information.getArguments().split(" ");
        if (checkSplit(split, 1, multi ? "particle multi" : "particle", multi ? "heart 10 0.1 0.1 0.1" : "heart")) {
            return;
        }
        var particleType = Particles.fromKey(NamespacedKey.minecraft(split[0].toLowerCase()));
        if (particleType == null) {
            plugin.getLogger().warning("The particle " + split[0] + " does not exist!");
            return;
        }
        var particle = multi ? particleType.multi() : particleType.single();
        if (particle instanceof DestinationParticle || particle instanceof BlockDataParticle
                || particle instanceof VibrationParticle || particle instanceof VelocityParticle) {
            plugin.getLogger().warning("The particle " + split[0] + " is not supported by this action!");
            return;
        }
        particle = addParticleValues(particle, split);
        particle.compile().send(information.getPlayer(), information.getPlayer().getLocation());
    }

    private BigInteger getBigInteger(String string) {
        try {
            return new BigInteger(string);
        } catch (Exception e) {
            return BigInteger.valueOf(1);
        }
    }

    private Particle addParticleValues(Particle particle, String[] split) {
        var counter = 1;
        if (particle instanceof MultiParticle multiParticle) {
            multiParticle.setCount(getBigInteger(split[counter]).intValue());
            counter++;
            multiParticle.setXOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            multiParticle.setYOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            multiParticle.setZOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            if (multiParticle instanceof MultiNoteParticle multiNoteParticle) {
                multiNoteParticle.setColorMultplier(getBigInteger(split[counter]).intValue());
                counter++;
            }
        }
        if (particle instanceof ColorableParticle colorableParticle && StringUtil.colorFromString(split[counter]) != null) {
            colorableParticle.setColor(StringUtil.colorFromString(split[counter]));
            counter++;
        }
        if (particle instanceof TransitionDustParticle transitionDustParticle && StringUtil.colorFromString(split[counter]) != null) {
            transitionDustParticle.setFadeColor(StringUtil.colorFromString(split[counter]));
            counter++;
        }
        if (particle instanceof ItemStackParticle materialParticle && Material.getMaterial(split[counter]) != null) {
            materialParticle.setItemStack(new ItemStack(Material.getMaterial(split[counter])));
            counter++;
        }
        if (particle instanceof SpeedModifiableParticle speedModifiableParticle) {
            speedModifiableParticle.setSpeed(getBigInteger(split[counter]).floatValue());
            counter++;
        }
        if (particle instanceof DelayableParticle delayableParticle) {
            delayableParticle.setDelay(getBigInteger(split[counter]).intValue());
            counter++;
        }
        if (particle instanceof SizeableParticle sizeableParticle) {
            sizeableParticle.setSize(getBigInteger(split[counter]).floatValue());
            counter++;
        }
        if (particle instanceof RollableParticle rollableParticle) {
            rollableParticle.setRoll(getBigInteger(split[counter]).floatValue());
        }
        return particle;
    }

    private void registerSound() {
        plugin.getActionHandler().subscribe(Action.SOUND, (information) -> {
            var player = information.getPlayer();
            var split = information.getArguments().split(" ");
            if (checkSplit(split, 1, "sound", "ENTITY_VILLAGER_HURT")) return;
            var sound = Sound.ENTITY_VILLAGER_HURT;
            try {
                sound = Sound.valueOf(split[0]);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("The sound " + split[0] + " is not a valid sound! Example: ENTITY_VILLAGER_HURT (HMCWraps action configuration)");
            }
            if (split.length == 3) {
                try {
                    player.playSound(player.getLocation(), sound, Float.parseFloat(split[1]), Float.parseFloat(split[2]));
                    return;
                } catch (NumberFormatException exception) {
                    plugin.getLogger().warning("The sound " + split[1] + " or " + split[2] + " is not a valid float number! Example: 1.0 (HMCWraps action configuration)");
                }
            }
            player.playSound(player.getLocation(), sound, 1, 1);
        });
    }

    private void registerTitle() {
        plugin.getActionHandler().subscribe(Action.TITLE, (information -> {
            var player = information.getPlayer();
            var split = information.getArguments().split(" ");
            if (checkSplit(split, 4, "title", "0.5 4.0 1.0 message")) return;
            var message = String.join(" ", Arrays.copyOfRange(split, 3, split.length));
            setTitleTimes(player, split);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSetTitleText(StringUtil.parseComponent(player, parseMessage(information, message))));
        }));
    }

    private void registerSubtitle() {
        plugin.getActionHandler().subscribe(Action.SUBTITLE, (information -> {
            var player = information.getPlayer();
            var split = information.getArguments().split(" ");
            if (checkSplit(split, 4, "title", "0.5 4.0 1.0")) return;
            var message = String.join(" ", Arrays.copyOfRange(split, 3, split.length));
            setTitleTimes(player, split);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSetTitleSubtitle(StringUtil.parseComponent(player, parseMessage(information, message))));
        }));
    }

    private void setTitleTimes(Player player, String[] split) {
        var fadeIn = 5;
        var hold = 40;
        var fadeOut = 10;
        try {
            fadeIn = Math.round(Float.parseFloat(split[0]) * 20);
            hold = Math.round(Float.parseFloat(split[1]) * 20);
            fadeOut = Math.round(Float.parseFloat(split[2]) * 20);
        } catch (NumberFormatException exception) {
            plugin.getLogger().warning("The title duration " + split[0] + " or " + split[1] + " or " + split[2] + " is not a valid float number! Example: 1.0 (HMCWraps action configuration)");
        }
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSetTitleTimes(fadeIn, hold, fadeOut));
    }

    private void registerActionBar() {
        plugin.getActionHandler().subscribe(Action.ACTIONBAR, (information -> {
            if (checkSplit(information.getArguments().split(" "), 1, "actionbar", "message")) return;
            information.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, StringUtil.parse(information.getPlayer(), parseMessage(information)));
        }));
    }

    private void registerMessage() {
        plugin.getActionHandler().subscribe(Action.MESSAGE, (information) -> {
            if (checkSplit(information.getArguments().split(" "), 1, "message", "message")) return;
            StringUtil.send(information.getPlayer(), parseMessage(information));
        });
    }

    private void registerCommand() {
        plugin.getActionHandler().subscribe(Action.COMMAND, (information) -> {
            if (checkSplit(information.getArguments().split(" "), 1, "command", "say HMCWraps")) return;
            var player = information.getPlayer();
            Bukkit.dispatchCommand(player, parseCommand(information));
        });
    }

    private void registerConsoleCommand() {
        plugin.getActionHandler().subscribe(Action.CONSOLE_COMMAND, (information) -> {
            if (checkSplit(information.getArguments().split(" "), 1, "console command", "kill <player>")) return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parseCommand(information));
        });
    }

    private void registerFilterToggle() {
        plugin.getActionHandler().subscribe(Action.TOGGLE_FILTER, information -> {
            plugin.getFilterStorage().set(information.getPlayer(), !plugin.getFilterStorage().get(information.getPlayer()));
            GuiBuilder.open(plugin, information.getPlayer(), information.getPlayer().getInventory().getItemInMainHand());
        });
    }

    private void registerFavorite() {
        plugin.getActionHandler().subscribe(Action.SET_FAVORITE, (information -> {
            var player = information.getPlayer();
            var current = plugin.getFavoriteWrapStorage().get(player);
            var wrap = getWrap(information);
            if (wrap == null) return;

            var collections = plugin.getCollectionHelper();
            (new LinkedList<>(current)).forEach((currentWrap) -> {
                if (currentWrap.getUuid().equals(wrap.getUuid())) {
                    return;
                }
                if (!ListUtil.containsAny(collections.getMaterials(collections.getCollection(currentWrap)), collections.getMaterials(collections.getCollection(wrap)))) {
                    return;
                }
                var range = wrap.getRange() == null ? new RangeSettings(new ValueRangeSettings<>(), new ValueRangeSettings<>()) : wrap.getRange();
                var currentRange = currentWrap.getRange() == null ? new RangeSettings(new ValueRangeSettings<>(null, null), new ValueRangeSettings<>(null, null)) : currentWrap.getRange();
                if (!isSameRange(range.getModelId(), currentRange.getModelId()) || !isSameRange(range.getColor(), currentRange.getColor())) {
                    return;
                }
                current.remove(currentWrap);
            });
            current.removeIf(it -> it.getUuid().equals(wrap.getUuid()));
            current.add(wrap);
            plugin.getFavoriteWrapStorage().set(player, current);
        }));
    }

    private <T> boolean isSameRange(ValueRangeSettings<T> first, ValueRangeSettings<T> second) {
        if (first.getInclude() != null) {
            if (second.getInclude() != null) {
                return ListUtil.containsAny(first.getInclude(), second.getInclude());
            } else if (second.getExclude() != null) {
                return !new HashSet<>(second.getExclude()).containsAll(first.getInclude());
            }
        } else if (first.getExclude() != null) {
            if (second.getInclude() != null) {
                return !new HashSet<>(first.getExclude()).containsAll(second.getInclude());
            } else if (second.getExclude() != null) {
                return !new HashSet<>(second.getExclude()).containsAll(first.getExclude());
            }
        }
        return true;
    }

    private void registerClearFavorites() {
        plugin.getActionHandler().subscribe(Action.CLEAR_FAVORITES, (information -> plugin.getFavoriteWrapStorage().set(information.getPlayer(), new ArrayList<>())));
    }

    private void registerPreview() {
        plugin.getActionHandler().subscribe(Action.PREVIEW, (information -> {
            var player = information.getPlayer();
            var wrap = getWrap(information);
            if (wrap == null) return;
            if (!wrap.isPreview()) {
                plugin.getMessageHandler().send(player, Messages.PREVIEW_DISABLED);
                return;
            }
            if (plugin.getConfiguration().getPermissions().isPreviewPermission() && !wrap.hasPermission(player)) {
                plugin.getMessageHandler().send(player, Messages.NO_PERMISSION_FOR_WRAP);
                return;
            }
            plugin.getPreviewManager().create(player, (ignored) -> openIfPossible(plugin, information, player), wrap);
            plugin.getActionHandler().pushPreview(wrap, player);
        }));
    }

    private void openIfPossible(HMCWrapsPlugin plugin, ActionInformation information, Player player) {
        if (!plugin.getCollectionHelper().getItems(player.getInventory().getItemInMainHand().getType()).isEmpty()
                && (information instanceof GuiActionInformation || information instanceof WrapGuiActionInformation)) {
            GuiBuilder.open(plugin, player, player.getInventory().getItemInMainHand());
        }
    }

    private void registerWrap() {
        plugin.getActionHandler().subscribe(Action.WRAP, (information -> {
            var player = information.getPlayer();
            var wrap = getWrap(information);
            if (wrap == null) return;
            if (!wrap.hasPermission(player) && plugin.getConfiguration().getPermissions().isPermissionVirtual()) {
                plugin.getMessageHandler().send(player, Messages.NO_PERMISSION_FOR_WRAP);
                return;
            }
            var item = player.getInventory().getItemInMainHand();
            player.getInventory().setItem(EquipmentSlot.HAND, plugin.getWrapper().setWrap(wrap, item, false, player, true));
            plugin.getMessageHandler().send(player, Messages.APPLY_WRAP);
            plugin.getActionHandler().pushWrap(wrap, player);
            plugin.getActionHandler().pushVirtualWrap(wrap, player);
            player.getOpenInventory().close();
        }));
    }

    private boolean checkSplit(String[] split, int length, String action, String example) {
        if (split.length < length) {
            plugin.getLogger().warning("The " + action + " action needs at least " + length + " arguments! Example: " + example + " (HMCWraps action configuration)");
            return true;
        }
        return false;
    }

    private String parseCommand(ActionInformation information) {
        var string = parseMessage(information);
        if (string.startsWith("/")) {
            string = string.substring(1);
        }
        return string;
    }

    private String parseMessage(ActionInformation information, String message) {
        var player = information.getPlayer();
        var string = StringUtil.replacePlaceholders(player, message.replace("<player>", player.getName()));
        if (information instanceof WrapActionInformation wrapInformation) {
            string = string.replace("<wrap_id>", wrapInformation.getWrap().getUuid()).replace("<wrap>", wrapInformation.getWrap().getName());
        }
        return string;
    }

    private Wrap getWrap(ActionInformation information) {
        var split = information.getArguments().split(" ");
        Wrap wrap = null;
        if (split.length == 1 && plugin.getWrapsLoader().getWraps().get(split[0]) != null) {
            wrap = plugin.getWrapsLoader().getWraps().get(split[0]);
        } else if (information instanceof WrapActionInformation wrapInformation) {
            wrap = wrapInformation.getWrap();
        } else if (information instanceof WrapGuiActionInformation wrapInformation) {
            wrap = wrapInformation.getWrap();
        }
        return wrap;
    }

    private String parseMessage(ActionInformation information) {
        return parseMessage(information, information.getArguments());
    }

}
