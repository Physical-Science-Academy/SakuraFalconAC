package me.liuli.falcon.check.misc;

import cn.nukkit.Player;
import me.liuli.falcon.cache.CheckCache;
import me.liuli.falcon.manager.AnticheatManager;
import me.liuli.falcon.manager.CheckResult;
import me.liuli.falcon.manager.CheckType;
import net.catrainbow.sakura.SakuraAPI;
import net.catrainbow.sakura.SakuraAPIAB;

import java.util.Timer;
import java.util.TimerTask;

public class NoSwingCheck {
    public static void addSwingRecord(Player player) {
        CheckCache cache = CheckCache.get(player);
        cache.lastSwing = System.currentTimeMillis();
    }

    public static void check(Player player) {
        Timer timer = new Timer("setTimeout", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CheckResult checkResult = checkSwing(player);
                if (checkResult.failed()) {
                    AnticheatManager.addVL(player, CheckType.NOSWING, checkResult);
                }
            }
        }, CheckType.NOSWING.otherData.getInteger("swing") / 2);
    }

    private static CheckResult checkSwing(Player player) {
        CheckCache cache = CheckCache.get(player);

        if ((System.currentTimeMillis() - cache.lastSwing) > CheckType.NOSWING.otherData.getInteger("swing")) {
            String result = "Attack a entity without swing(last=" + (System.currentTimeMillis() - cache.lastSwing) + ")";
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "BadPacket", 2d, result);
            return new CheckResult(result);
        }
        return CheckResult.PASSED;
    }
}
