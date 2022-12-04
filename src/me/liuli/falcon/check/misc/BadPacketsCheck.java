package me.liuli.falcon.check.misc;

import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import me.liuli.falcon.cache.CheckCache;
import me.liuli.falcon.manager.AnticheatManager;
import me.liuli.falcon.manager.CheckResult;
import me.liuli.falcon.manager.CheckType;
import me.liuli.falcon.utils.MoveUtil;
import net.catrainbow.sakura.SakuraAPI;
import net.catrainbow.sakura.SakuraAPIAB;

public class BadPacketsCheck {
    public static CheckResult runCheck(Player player, DataPacket packet) {
        if (packet instanceof MovePlayerPacket) {
            if (AnticheatManager.canCheckPlayer(player, CheckType.BADPACKETS))
                return checkMove(player, (MovePlayerPacket) packet);
        } else if (packet instanceof PlayerActionPacket) {
            if (AnticheatManager.canCheckPlayer(player, CheckType.BADPACKETS))
                return checkAction(player, (PlayerActionPacket) packet);
        }/*else if(packet instanceof AnimatePacket) {
            if (AnticheatManager.canCheckPlayer(player, CheckType.BADPACKETS))
                return checkAnimate(player);
        }else if(packet instanceof InventoryTransactionPacket){
            if (AnticheatManager.canCheckPlayer(player, CheckType.BADPACKETS)) {
                return checkInv(player, (InventoryTransactionPacket) packet);
            }
        }*/
        return CheckResult.PASSED;
    }

    private static CheckResult checkAnimate(Player player) {
        CheckCache checkCache = CheckCache.get(player);
        if (checkCache == null) return CheckResult.PASSED;

        long lastAnimate = checkCache.lastAnimate;
        checkCache.lastAnimate = System.currentTimeMillis();

        if ((System.currentTimeMillis() - lastAnimate) < CheckType.BADPACKETS.otherData.getInteger("animateDelay")) {
            String result = "animate too fast(time=" + (System.currentTimeMillis() - checkCache.lastAnimate) + ")";
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "BadPacket", 1.2d, result);
            return new CheckResult(result);
        }

        return CheckResult.PASSED;
    }

    private static CheckResult checkAction(Player player, PlayerActionPacket packet) {
        CheckCache checkCache = CheckCache.get(player);
        if (checkCache == null) return CheckResult.PASSED;

        if (packet.action == PlayerActionPacket.ACTION_JUMP) {
            if (!(MoveUtil.isNearSolid(player.clone().add(0, 2, 0))) && (System.currentTimeMillis() - checkCache.lastJump) < CheckType.BADPACKETS.otherData.getInteger("jumpCoolDown")) {
                String result = "jump too fast(time=" + (System.currentTimeMillis() - checkCache.lastJump) + ")";
                SakuraAPIAB api = new SakuraAPI();
                api.addVL(player, "BadPacket", 1.2d, result);
                return new CheckResult(result);
            }
            if (player.onGround) {
                checkCache.lastJump = System.currentTimeMillis();
            } else {
                String result = "trying to jump offGround";
                SakuraAPIAB api = new SakuraAPI();
                api.addVL(player, "BadPacket", 1.2d, result);
                return new CheckResult(result);
            }
        }
        return CheckResult.PASSED;
    }

    private static CheckResult checkMove(Player player, MovePlayerPacket packet) {
        if (packet.eid != player.getId() && CheckType.BADPACKETS.otherData.getBoolean("badId")) {
            String result ="Bad entity id in packet(id=" + packet.eid + ",realId=" + player.getId();
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "BadPacket", 5d, result);
            return new CheckResult(result);
        }
        if (Math.abs(packet.pitch) > 90 && CheckType.BADPACKETS.otherData.getBoolean("derp")) {
            String result = "Had an illegal pitch(pitch=" + packet.pitch + ")";
            SakuraAPIAB api = new SakuraAPI();
            api.addVL(player, "BadPacket", 1.2d, result);
            return new CheckResult(result);
        }
        return CheckResult.PASSED;
    }

    private static CheckResult checkInv(Player player, InventoryTransactionPacket packet) {
        if (!CheckType.BADPACKETS.otherData.getBoolean("inventory")) return CheckResult.PASSED;
        PlayerInventory inventory = player.getInventory();
        for (NetworkInventoryAction action : packet.actions) {
            if (action.windowId == 0) {
                if (action.inventorySlot < 0 || action.inventorySlot > 40) {
                    String result = "Trying move a item from unknown slot";
                    SakuraAPIAB api = new SakuraAPI();
                    api.addVL(player, "BadPacket", 1.2d, result);
                    return new CheckResult(result);
                }
                Item realFromItem = inventory.getItem(action.inventorySlot);
                Item packetFromItem = action.oldItem;
                if (!realFromItem.equals(packetFromItem)) {
                    String result = "Trying move a item not exists(packet=" + packetFromItem.getName() + ",real=" + realFromItem + ")";
                    SakuraAPIAB api = new SakuraAPI();
                    api.addVL(player, "BadPacket", 1.2d, result);
                    return new CheckResult(result);
                }
            }
        }
        return CheckResult.PASSED;
    }
}
