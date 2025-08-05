package zone.moddev.yuki.updatenotifiers;

import zone.moddev.yuki.Yuki;
import zone.moddev.yuki.updatenotifiers.blockbench.BlockbenchUpdateNotifier;
import zone.moddev.yuki.updatenotifiers.fabric.FabricApiUpdateNotifier;
import zone.moddev.yuki.updatenotifiers.forge.ForgeUpdateNotifier;
import zone.moddev.yuki.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import zone.moddev.yuki.updatenotifiers.neoforge.NeoForgeUpdateNotifier;
import zone.moddev.yuki.updatenotifiers.parchment.ParchmentUpdateNotifier;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateNotifiers {

    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r ->
            setThreadDaemon(new Thread(r, "TaskScheduler")));

    private static Thread setThreadDaemon(final Thread thread) {
        thread.setDaemon(true);
        return thread;
    }

    private UpdateNotifiers() {
        throw new IllegalStateException("Utility class");
    }

    public static void init() {
        var checkingPeriod = 15;
        Yuki.LOGGER.info("Checking for Minecraft, Forge, NeoForge and Fabric updates every {} minutes. (If enabled in the config)", checkingPeriod);
        TIMER.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, checkingPeriod, TimeUnit.SECONDS);
        TIMER.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, checkingPeriod, TimeUnit.SECONDS);
        TIMER.scheduleAtFixedRate(new NeoForgeUpdateNotifier(), 0, checkingPeriod, TimeUnit.SECONDS);
        TIMER.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, checkingPeriod, TimeUnit.SECONDS);

        Yuki.LOGGER.info("Checking for Parchment and Blockbench updates every hour. (If enabled in the config)");
        TIMER.scheduleAtFixedRate(new ParchmentUpdateNotifier(), 0, 1, TimeUnit.HOURS);
        TIMER.scheduleAtFixedRate(new BlockbenchUpdateNotifier(), 0, 1, TimeUnit.HOURS);
    }
}
