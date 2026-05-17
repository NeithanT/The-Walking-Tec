package GameLogic;

import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseContact;
import Defense.DefenseExplosive;
import Defense.DefenseFlying;
import Defense.DefenseHealer;
import Defense.DefenseMediumRange;
import Defense.DefenseMultipleAttack;
import Defense.DefenseType;
import Entity.Entity;
import Entity.EntityAttacker;
import Entity.EntityHealer;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieMediumRange;
import Zombie.ZombieType;
import java.util.ArrayList;
import java.util.Random;

public final class EntityFactory {

    private EntityFactory() {}

    public static Defense cloneDefense(Defense original) {
        if (original == null) return null;

        Defense cloned;
        try {
            ArrayList<DefenseType> types = original.getTypes();

            if (original instanceof DefenseMultipleAttack orig) {
                cloned = new DefenseMultipleAttack(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getAttack(), 0, orig.getAmtOfAttacks());
            } else if (original instanceof DefenseHealer orig) {
                cloned = new DefenseHealer(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getHealPower());
            } else if (original instanceof DefenseExplosive) {
                cloned = new DefenseExplosive(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(), 0);
            } else if (original instanceof DefenseFlying orig) {
                cloned = new DefenseFlying(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getAttack(), 0);
            } else if (original instanceof DefenseContact orig) {
                cloned = new DefenseContact(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getAttack());
            } else if (original instanceof DefenseMediumRange orig) {
                cloned = new DefenseMediumRange(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getAttack(), 0);
            } else if (original instanceof DefenseAttacker orig) {
                cloned = new DefenseAttacker(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost(),
                    orig.getAttack(), 0);
            } else {
                cloned = new Defense(
                    types, original.getEntityName(), original.getHealthPoints(),
                    original.getShowUpLevel(), original.getCost());
            }

            if (original.getImagePath() != null) {
                cloned.setImagePath(original.getImagePath());
            }
        } catch (Exception e) {
            System.err.println("Error cloning defense: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return cloned;
    }

    public static Zombie cloneZombie(Zombie source) {
        if (source == null) return null;

        Zombie clone;
        if (source instanceof ZombieExplosive exp) {
            clone = new ZombieExplosive(
                exp.getEntityName(), exp.getHealthPoints(), exp.getShowUpLevel(),
                exp.getCost(), 0, exp.getMovementSpeed());
        } else if (source instanceof ZombieFlying fly) {
            clone = new ZombieFlying(
                fly.getEntityName(), fly.getHealthPoints(), fly.getShowUpLevel(),
                fly.getCost(), fly.getDamage(), 0, fly.getMovementSpeed());
        } else if (source instanceof ZombieMediumRange med) {
            clone = new ZombieMediumRange(
                med.getEntityName(), med.getHealthPoints(), med.getShowUpLevel(),
                med.getCost(), med.getDamage(), med.getMovementSpeed());
        } else if (source instanceof ZombieContact con) {
            clone = new ZombieContact(
                con.getEntityName(), con.getHealthPoints(), con.getShowUpLevel(),
                con.getCost(), con.getDamage(), con.getMovementSpeed());
        } else if (source instanceof ZombieHealer heal) {
            clone = new ZombieHealer(
                heal.getEntityName(), heal.getHealthPoints(), heal.getShowUpLevel(),
                heal.getCost(), heal.getHealPower(), heal.getMovementSpeed());
        } else if (source instanceof ZombieAttacker atk) {
            clone = new ZombieAttacker(
                atk.getEntityName(), atk.getHealthPoints(), atk.getShowUpLevel(),
                atk.getCost(), atk.getDamage(), 0, atk.getMovementSpeed());
        } else {
            ArrayList<ZombieType> types = source.getTypes();
            clone = new Zombie(types, source.getEntityName(), source.getHealthPoints(),
                source.getShowUpLevel(), source.getCost(), source.getMovementSpeed());
        }

        clone.setActions(source.getActions());
        clone.setImagePath(source.getImagePath());
        clone.setTypes(source.getTypes());
        return clone;
    }

    public static Defense applyDefenseScaling(Defense defense, int level, Random rnd) {
        if (defense == null || level <= 1) return defense;

        double totalMultiplier = scalingMultiplier(level, rnd);

        int originalHP = defense.getHealthPoints();
        defense.setHealthPoints((int) Math.round(originalHP * totalMultiplier));

        if (defense instanceof DefenseAttacker atk) {
            int origDmg = atk.getAttack();
            atk.setAttack((int) Math.round(origDmg * totalMultiplier));
        } else if (defense instanceof DefenseHealer healer) {
            int origHeal = healer.getHealPower();
            healer.setHealPower((int) Math.round(origHeal * totalMultiplier));
        }

        return defense;
    }

    public static Zombie applyZombieScaling(Zombie zombie, int level, Random rnd) {
        if (zombie == null || level <= 1) return zombie;

        double totalMultiplier = scalingMultiplier(level, rnd);

        int originalHP = zombie.getHealthPoints();
        zombie.setHealthPoints((int) Math.round(originalHP * totalMultiplier));

        if (zombie instanceof ZombieAttacker atk) {
            int origDmg = atk.getDamage();
            atk.setDamage((int) Math.round(origDmg * totalMultiplier));
        } else if (zombie instanceof ZombieHealer healer) {
            int origHeal = healer.getHealPower();
            healer.setHealPower((int) Math.round(origHeal * totalMultiplier));
        }

        return zombie;
    }

    private static double scalingMultiplier(int level, Random rnd) {
        double total = 1.0;
        for (int i = 1; i < level; i++) {
            total *= (1.0 + 0.05 + (rnd.nextDouble() * 0.15));
        }
        return total;
    }
}
