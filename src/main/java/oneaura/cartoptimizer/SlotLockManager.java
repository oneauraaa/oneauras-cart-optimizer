package oneaura.cartoptimizer;

import java.util.HashMap;
import java.util.Map;

public class SlotLockManager {
    private static final SlotLockManager INSTANCE = new SlotLockManager();
    private final Map<Integer, Long> lockedSlots = new HashMap<>();

    public static SlotLockManager getInstance() {
        return INSTANCE;
    }

    public void lockSlot(int slotId, long durationMs) {
        lockedSlots.put(slotId, System.currentTimeMillis() + durationMs);
    }

    public boolean isLocked(int slotId) {
        Long expirationTime = lockedSlots.get(slotId);
        if (expirationTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expirationTime) {
            lockedSlots.remove(slotId);
            return false;
        }
        return true;
    }
}
