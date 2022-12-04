package me.liuli.falcon.check.combat;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.potion.Effect;
import me.liuli.falcon.manager.CheckResult;
import me.liuli.falcon.utils.LocationUtil;
import net.catrainbow.sakura.SakuraAPI;
import net.catrainbow.sakura.SakuraAPIAB;

public class CriticalsCheck {
    public static CheckResult doDamageEvent(EntityDamageByEntityEvent event) {
        Player player = (Player) event.getDamager();
        if (isCritical(player)) {
            if ((player.getLocation().getY() % 1.0 == 0 || player.getLocation().getY() % 0.5 == 0)
                    && player.getLocation().clone().subtract(0, 1.0, 0).getLevelBlock().isSolid()) {
                String result = "tried to do a critical without needed conditions";
                SakuraAPIAB api = new SakuraAPI();
                api.addVL(player, "BadPacket", 1.2d, result);
                return new CheckResult(result);
            }
        }
        return CheckResult.PASSED;
    }

    private static boolean isCritical(Player player) {
        return !player.isOnGround() && !player.hasEffect(Effect.BLINDNESS)
                && !LocationUtil.isHoveringOverWater(player.getLocation(), 25)
                && !player.getLocation().getLevelBlock().canBeClimbed();
    }
}
