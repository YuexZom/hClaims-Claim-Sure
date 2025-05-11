package me.rexoz.xyz.claimtime;

import com.hakan.claim.Claim;
import com.hakan.claim.events.ClaimCreateEvent;
import com.hakan.claim.events.ClaimDeleteEvent;
import com.hakan.claim.events.ClaimTimeExpireEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimBlocker extends JavaPlugin implements Listener {

    private final Set<String> protectedChunks = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onClaimDelete(ClaimDeleteEvent event) {
        protectArea(event.getClaim());
    }

    @EventHandler
    public void onClaimExpire(ClaimTimeExpireEvent event) {
        protectArea(event.getClaim());
    }

    private void protectArea(Claim claim) {
        Location claimCenter = claim.getLocation();
        String chunkKey = chunkKey(claimCenter);

        protectedChunks.add(chunkKey);

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            protectedChunks.remove(chunkKey);
        }, 20L * 600);
    }

    private String chunkKey(Location location) {
        return location.getChunk().getX() + "," + location.getChunk().getZ();
    }

    public boolean isProtected(Location location) {
        String chunkKey = chunkKey(location);
        return protectedChunks.contains(chunkKey);
    }

    @EventHandler
    public void onClaimAttempt(ClaimCreateEvent event) {
        Claim claim = event.getClaim();
        Location claimCenter = claim.getLocation();

        if (isProtected(claimCenter)) {
            event.setCancelled(true);
            UUID ownerUUID = claim.getOwner();
            Player player = Bukkit.getPlayer(ownerUUID);
            if (player != null) {
                player.sendMessage("§6Claim §7> §4Bu alanda 10 dakika boyunca claim alınamaz!");
            }
        }
    }
}
