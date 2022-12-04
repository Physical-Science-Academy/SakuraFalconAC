package me.liuli.falcon.check.movement;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.potion.Effect;
import me.liuli.falcon.cache.CheckCache;
import me.liuli.falcon.cache.MovementCache;
import me.liuli.falcon.manager.CheckResult;
import me.liuli.falcon.manager.CheckType;
import me.liuli.falcon.utils.MoveUtil;
import net.catrainbow.sakura.SakuraAPI;
import net.catrainbow.sakura.SakuraAPIAB;

public class WaterWalkCheck {
    public static CheckResult runCheck(Player player, boolean onGround) {
        CheckCache cache = CheckCache.get(player);
        if (cache == null)
            return CheckResult.PASSED;

        MovementCache movementCache = cache.movementCache;
        if (movementCache.distanceXZ <= 0 || player.getRiding() != null || MoveUtil.inBlock(player, Block.LILY_PAD)
                || player.isSwimming() || player.getAllowFlight() || onGround)
            return CheckResult.PASSED;

        Block blockBeneath = player.getPosition().clone().subtract(0, 0.1, 0).getLevelBlock();
        if (!(MoveUtil.isLiquid(blockBeneath) || MoveUtil.isSurroundedByBlock(player.getPosition(), Block.STILL_WATER)))
            return CheckResult.PASSED;

        if (((movementCache.motionY == 0 && movementCache.lastMotionY == 0)
                || movementCache.motionY == MoveUtil.JUMP_MOTION_Y)
                && movementCache.distanceXZ > CheckType.WATER_WALK.otherData.getDouble("walkMinimumDistXZ")) {
            String result = "tried to walk on water (xz=" + movementCache.distanceXZ + ")";
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "MagicWalk", 1.2d, result);
            return new CheckResult(result);
        }

        double minAbsMotionY = 0.12D;
        if (player.hasEffect(Effect.SPEED))
            minAbsMotionY += player.getEffect(Effect.SPEED).getAmplifier() * 0.05D;
        if (Math.abs(movementCache.lastMotionY - movementCache.motionY) > minAbsMotionY
                && movementCache.distanceXZ > CheckType.WATER_WALK.otherData.getDouble("lungeMinimumDistXZ")
                && movementCache.lastMotionY > -0.25) {
            String result = "tried to lunge in water (xz="
                    + movementCache.distanceXZ + ", absMotionY="
                    + Math.abs(movementCache.lastMotionY - movementCache.motionY) + ")";
                    SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "MagicWalk", 1.2d, result);
            return new CheckResult(result);
        }
        return CheckResult.PASSED;
    }
}
