package spinloki.TreasureHunt.internal.items;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.internal.registry.THRewardRegistry;

import java.util.List;

public class THVanillaItemTagger {
    public static Logger log = Global.getLogger(THVanillaItemTagger.class);
    public static void tagItems(){
        THRewardRegistry rewards = THRegistry.getRewardRegistry();
        var blueprintPackages = rewards.getAllBlueprintPackages();
        for (var blueprintPackage : blueprintPackages){
            tagFighters(rewards.getFightersFromPackage(blueprintPackage), blueprintPackage);
            tagShips(rewards.getShipsFromPackage(blueprintPackage), blueprintPackage);
            tagWeapons(rewards.getWeaponsFromPackage(blueprintPackage), blueprintPackage);
        }
    }

    private static void tagFighters(List<String> fighters, String blueprintPackage){
        for(var fighter : fighters){
            var fighterSpec = Global.getSettings().getFighterWingSpec(fighter);
            if (fighterSpec == null){
                log.error("Fighter wing " + fighter + " had null spec in global settings");
                continue;
            }
            fighterSpec.addTag(blueprintPackage);
        }
    }

    public static void tagShips(List<String> ships, String blueprintPackage){
        for(var ship : ships){
            var shipSpec = Global.getSettings().getHullSpec(ship);
            if (shipSpec == null){
                log.error("Ship " + ship + " had null spec in global settings");
                continue;
            }
            shipSpec.addTag(blueprintPackage);
        }
    }

    private static void tagWeapons(List<String> weapons, String blueprintPackage){
        for(var weapon : weapons){
            var weaponSpec = Global.getSettings().getWeaponSpec(weapon);
            if (weaponSpec == null){
                log.error("Weapon " + weapon + " had null spec in global settings");
                continue;
            }
            weaponSpec.addTag(blueprintPackage);
        }
    }
}
