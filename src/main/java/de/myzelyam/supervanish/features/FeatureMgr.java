/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

public class FeatureMgr {

    public static final Map<String, FeatureInfo> REGISTERED_FEATURES
            = new HashMap<String, FeatureInfo>() {{
        put("SilentOpenChest", new FeatureInfo(SilentOpenChest.class,
                Collections.singletonList("ProtocolLib")));
        put("NightVision", new FeatureInfo(NightVision.class,
                Collections.singletonList("ProtocolLib")));
        put("VanishIndication", new FeatureInfo(VanishIndication.class,
                Collections.singletonList("ProtocolLib")));
        put("Broadcast", new FeatureInfo(Broadcast.class));
    }};
    private final SuperVanish plugin;
    private Set<Feature> activeFeatures = new HashSet<>();

    public FeatureMgr(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void enableFeatures() {
        featureLoop:
        for (String id : REGISTERED_FEATURES.keySet()) {
            FeatureInfo featureInfo = REGISTERED_FEATURES.get(id);
            for (String dependency : featureInfo.getDependencies()) {
                if (!Bukkit.getPluginManager().isPluginEnabled(dependency)) continue featureLoop;
            }
            Feature feature;
            try {
                feature = featureInfo.getFeatureClass().getConstructor(SuperVanish.class).newInstance(plugin);
            } catch (NoSuchMethodException | InvocationTargetException
                    | InstantiationException | IllegalAccessException e) {
                plugin.logException(e);
                continue;
            }
            if (!feature.isActive()) continue;
            activeFeatures.add(feature);
            Bukkit.getPluginManager().registerEvents(feature, plugin);
            feature.onEnable();
        }
    }

    public void disableFeatures() {
        for (Feature feature : activeFeatures) {
            feature.onDisable();
            HandlerList.unregisterAll(feature);
        }
        activeFeatures.clear();
    }

    public <T extends Feature> T getFeature(Class<T> featureClass) {
        for (Feature feature : activeFeatures) {
            if (feature.getClass().equals(featureClass)) {
                //noinspection unchecked
                return (T) feature;
            }
        }
        return null;
    }

    public Set<Feature> getActiveFeatures() {
        return activeFeatures;
    }

    @Data
    private static class FeatureInfo {
        private final Class<? extends Feature> featureClass;
        private final Collection<String> dependencies;

        FeatureInfo(Class<? extends Feature> featureClass, Collection<String> dependencies) {
            this.featureClass = featureClass;
            this.dependencies = dependencies;
        }

        FeatureInfo(Class<? extends Feature> featureClass) {
            this(featureClass, Collections.<String>emptySet());
        }
    }
}
