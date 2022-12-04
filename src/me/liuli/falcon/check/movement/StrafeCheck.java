package me.liuli.falcon.check.movement;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import me.liuli.falcon.cache.CheckCache;
import me.liuli.falcon.cache.MovementCache;
import me.liuli.falcon.manager.CheckResult;
import me.liuli.falcon.manager.CheckType;
import me.liuli.falcon.utils.MoveUtil;
import me.liuli.falcon.utils.OtherUtil;
import net.catrainbow.sakura.SakuraAPI;
import net.catrainbow.sakura.SakuraAPIAB;

public class StrafeCheck {
    public static CheckResult runCheck(Player player, double x, double z, Location from, Location to) {
        CheckCache cache = CheckCache.get(player);
        if (cache == null)
            return CheckResult.PASSED;

        if (MoveUtil.isNearBlock(player, Block.STILL_WATER) || player.getAllowFlight() || !player.isAlive()
                || MoveUtil.isNearSolid(player.getPosition()) || MoveUtil.isNearSolid(player.getPosition().add(0, 1, 0)))
            return CheckResult.PASSED;

        MovementCache movementCache = cache.movementCache;

        if (movementCache.elytraEffectTicks >= 20
                || movementCache.halfMovementHistoryCounter >= 20)
            return CheckResult.PASSED;

        Vector3 oldAcceleration = new Vector3(movementCache.lastDistanceX, 0, movementCache.lastDistanceZ);
        Vector3 newAcceleration = new Vector3(x, 0, z);

        float angle = OtherUtil.angle(newAcceleration, oldAcceleration);
        double distance = newAcceleration.lengthSquared();
        if (angle > CheckType.STRAFE.otherData.getDouble("maxAngleChange")
                && distance > CheckType.STRAFE.otherData.getDouble("minActivationDistance")
                && !player.onGround) {
            String result = "switched angle in air (angle=" + angle + ", dist=" + distance + ")";
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "Speed", 1.2d, result);
            return new CheckResult(result);
        }
        return CheckResult.PASSED;
    }
}
