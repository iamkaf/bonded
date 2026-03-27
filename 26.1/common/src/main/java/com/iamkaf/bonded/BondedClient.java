package com.iamkaf.bonded;

import com.iamkaf.bonded.client.HUD;
import com.iamkaf.amber.api.event.v1.events.common.client.HudEvents;

public class BondedClient {
    public static void init() {
        HUD.init();
        HudEvents.RENDER_HUD.register(HUD::onRenderHud);
    }
}
