/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaVenetianBlindHandler} is responsible for handling commands,
 * which are sent to one of the channels of the venetian blind thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaVenetianBlindHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaVenetianBlindHandler.class);

    public SomfyTahomaVenetianBlindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return new Hashtable<String, String>() {{
            put(CONTROL, "core:ClosureState");
            put(ORIENTATION, "core:SlateOrientationState");
        }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!channelUID.getId().equals(CONTROL) && !channelUID.getId().equals(ORIENTATION)) {
            return;
        }

        if (command.equals(RefreshType.REFRESH)) {
            updateChannelState(channelUID);
        } else {
            String cmd = getTahomaCommand(command.toString(), channelUID.getId());
            //Check if the rollershutter is not moving
            String executionId = getCurrentExecutions();
            if (executionId != null) {
                //STOP command should be interpreted if rollershutter moving
                //otherwise do nothing
                if (cmd.equals(COMMAND_MY)) {
                    cancelExecution(executionId);
                }
            } else {
                String param = (cmd.equals(COMMAND_SET_CLOSURE) || cmd.equals(COMMAND_SET_ORIENTATION)) ? "[" + command.toString() + "]" : "[]";
                sendCommand(cmd, param);
            }
        }

    }

    private String getTahomaCommand(String command, String channelId) {
        switch (command) {
            case "OFF":
            case "DOWN":
                return COMMAND_DOWN;
            case "ON":
            case "UP":
                return COMMAND_UP;
            case "STOP":
                return COMMAND_MY;
            default:
                if (channelId.equals(CONTROL)) {
                    return COMMAND_SET_CLOSURE;
                } else {
                    return COMMAND_SET_ORIENTATION;
                }
        }
    }
}
