package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;

public class DigSpeedFactory {
    public static DigSpeedBonus create(int threshold, float bonus) {
        return new DigSpeedBonus(Bonded.resource("dig_speed_" + threshold), threshold, bonus);
    }
}
