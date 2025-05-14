package spinloki.treasurehunt.campaign.items;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.treasurehunt.config.Settings;

import java.util.List;

public class THVanillaItemTagger {
    public static Logger log = Global.getLogger(Settings.class);
    public static void tagItems(){
        var blueprintPackages = Settings.getAllBlueprintPackages();
        for (var blueprintPackage : blueprintPackages){
            tagFighters(Settings.getFightersFromPackage(blueprintPackage), blueprintPackage);
            tagShips(Settings.getShipsFromPackage(blueprintPackage), blueprintPackage);
            tagWeapons(Settings.getWeaponsFromPackage(blueprintPackage), blueprintPackage);
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
