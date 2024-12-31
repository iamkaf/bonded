package com.iamkaf.bonded;

import com.iamkaf.bonded.client.HUD;
import dev.architectury.event.events.client.ClientGuiEvent;

public class BondedClient {
    public static void init() {
        HUD.init();
        ClientGuiEvent.RENDER_HUD.register(HUD::onRenderHud);
    }
}
