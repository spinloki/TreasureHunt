package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class THMockMemory implements MemoryAPI {
    private final Map<String,Object> data = new HashMap<>();
    @Override public void set(String key, Object value) {
        data.put(key, value);
    }
    @Override public Object get(String key) {
        return data.get(key);
    }
    @Override
    public void unset(String key) {

    }

    @Override
    public void expire(String key, float days) {

    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public boolean is(String key, Object value) {
        return false;
    }

    @Override
    public boolean is(String key, float value) {
        return false;
    }

    @Override
    public boolean is(String key, boolean value) {
        return false;
    }

    @Override
    public void set(String key, Object value, float expire) {

    }

    @Override
    public String getString(String key) {
        return "";
    }

    @Override
    public float getFloat(String key) {
        return 0;
    }
    @Override
    public boolean getBoolean(String key) {
        Object v = data.get(key);
        return v instanceof Boolean && (Boolean) v;
    }

    @Override
    public long getLong(String key) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        return 0L;
    }

    @Override
    public Vector2f getVector2f(String key) {
        return null;
    }

    @Override
    public SectorEntityToken getEntity(String key) {
        return null;
    }

    @Override
    public CampaignFleetAPI getFleet(String key) {
        return null;
    }

    @Override
    public boolean between(String key, float min, float max) {
        return false;
    }

    @Override
    public Collection<String> getKeys() {
        return List.of();
    }

    @Override
    public float getExpire(String key) {
        return 0;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void addRequired(String key, String requiredKey) {

    }

    @Override
    public void removeRequired(String key, String requiredKey) {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Set<String> getRequired(String key) {
        return Set.of();
    }

    @Override
    public void removeAllRequired(String key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int getInt(String key) {
        return 0;
    }
}
