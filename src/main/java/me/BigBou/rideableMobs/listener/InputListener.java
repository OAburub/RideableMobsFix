package me.BigBou.rideableMobs.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.BigBou.rideableMobs.util.Util;
import org.bukkit.Input;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;

public class InputListener extends PacketAdapter {
    private static final Logger log = LoggerFactory.getLogger(InputListener.class);

    public InputListener(Plugin plugin) {
        super(plugin, new PacketType[]{PacketType.Play.Client.STEER_VEHICLE});
    }

    public static HashMap<Player, InputListener.WrappedInput> playerInputs = new HashMap<>();

    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if (Util.isWorldEnabled(player)) {
            Entity vehicle = player.getVehicle();
            if (vehicle != null && !(vehicle instanceof Vehicle)) {
                PacketContainer packet = event.getPacket();
                Object object = packet.getModifier().read(0);
                InputListener.WrappedInput input = new InputListener.WrappedInput(object);
                playerInputs.put(player, input);
            }
        }
    }
    public class WrappedInput implements Input {
        private final Object handle;

        public WrappedInput(Object handle) {
            this.handle = handle;
        }

        public boolean isForward() {
            return getBoolean("forward");
        }

        public boolean isJump() {
            return getBoolean("jump");
        }

        public boolean isLeft() {
            return getBoolean("left");
        }

        public boolean isRight() {
            return getBoolean("right");
        }

        public boolean isBackward() {
            return getBoolean("backward");
        }

        public boolean isSneak() {
            return getBoolean("shift");
        }

        public boolean isSprint() {
            return getBoolean("sprint");
        }

        private boolean getBoolean(String method) {
            try {
                return (boolean) handle.getClass().getMethod(method).invoke(handle);
            } catch (Exception e) {
                log.error("Error getting boolean value from WrappedInput: {}", e.getMessage());
                return false;
            }
        }

        public String toString() {
            return "WrappedInput{" +
                    "handle=" + handle +
                    ", isForward=" + isForward() +
                    ", isJump=" + isJump() +
                    ", isLeft=" + isLeft() +
                    ", isRight=" + isRight() +
                    ", isBackward=" + isBackward() +
                    ", isShift=" + isSneak() +
                    '}';
        }

        public static void listMethods(Object obj) {
            Method[] methods = obj.getClass().getDeclaredMethods();

            for (Method method : methods) {
                log.info("Method: " + method.getName());
            }
        }
    }
}
