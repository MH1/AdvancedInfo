package org.hlousek.hytale.plugin.advancedinfo;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.protocol.packets.interface_.UpdateVisibleHudComponents;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapVisible;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.hlousek.hytale.plugin.advancedinfo.command.AdvancedInfoCommand;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.hlousek.hytale.plugin.advancedinfo.ui.InfoManager;

import javax.annotation.Nonnull;

public class AdvancedInfo extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final InfoManager infoManager = new InfoManager();

    public AdvancedInfo(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        ComponentType<EntityStore, PlayerHudSettings> settingsComponentType = entityStoreRegistry
            .registerComponent(
                PlayerHudSettings.class,
                "AdvancedInfo",
                PlayerHudSettings.CODEC
            );

        entityStoreRegistry.registerSystem(new InfoUpdateSystem(infoManager, settingsComponentType));
        getCommandRegistry().registerCommand((AbstractCommand) new AdvancedInfoCommand(settingsComponentType));

        getEventRegistry().registerGlobal(
            EventPriority.EARLY,
            PlayerReadyEvent.class,
            event -> {
                Player player = event.getPlayer();
                World world = player.getWorld();
                if (world == null) return;

                world.execute(() -> {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) return;

                    Store<EntityStore> store = ref.getStore();
                    PlayerHudSettings settings = store.getComponent(ref, settingsComponentType);
                    if (settings == null) {
                        settings = store.ensureAndGetComponent(ref, settingsComponentType);
                    }
                    settings.applyMigration();
                });
            }
        );

        getEventRegistry().registerGlobal(
            EventPriority.NORMAL,
            PlayerDisconnectEvent.class,
            event -> infoManager.onPlayerLeave(event.getPlayerRef())
        );
    }

    @Override
    protected void start() {
        PluginBase plugin = PluginManager.get().getPlugin(PluginIdentifier.fromString("Buuz135:MultipleHUD"));
        if (plugin != null) {
            try {
                Class.forName("com.buuz135.mhud.MultipleHUD");
                infoManager.setMultipleHUDPresent(true);
                LOGGER.atInfo().log("MultipleHUD detected - using it for HUD rendering.");
            } catch (ClassNotFoundException e) {
                LOGGER.atSevere().log("MultipleHUD plugin is loaded but the class cannot be accessed!");
            }
        } else {
            LOGGER.atInfo().log("MultipleHUD not present, using default HUD slot.");
        }

        ServerManager.get().registerSubPacketHandlers(packetHandler -> () -> {
            packetHandler.registerHandler(UpdateWorldMapVisible.PACKET_ID, p -> {
                boolean visible = ((UpdateWorldMapVisible) p).visible;
                infoManager.onMapVisibilityChanged(packetHandler.getPlayerRef().getUuid(), visible);
            });
            packetHandler.registerHandler(UpdateVisibleHudComponents.PACKET_ID, p -> {
                HudComponent[] components = ((UpdateVisibleHudComponents) p).visibleComponents;
                boolean hudHidden = components == null || !containsHotbar(components);
                LOGGER.atInfo().log("UpdateVisibleHudComponents received: components=%s hudHidden=%b",
                    components == null ? "null" : java.util.Arrays.toString(components), hudHidden);
                infoManager.onHudVisibilityChanged(packetHandler.getPlayerRef().getUuid(), !hudHidden);
            });
        });
    }

    private static boolean containsHotbar(@Nonnull HudComponent[] components) {
        for (HudComponent c : components) {
            if (c == HudComponent.Hotbar) return true;
        }
        return false;
    }
}
