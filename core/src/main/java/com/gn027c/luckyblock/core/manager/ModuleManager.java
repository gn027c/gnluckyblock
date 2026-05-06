package com.gn027c.luckyblock.core.manager;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<AbstractModule> modules = new ArrayList<>();

    public void registerModule(AbstractModule module) {
        modules.add(module);
    }

    public void enableModules() {
        modules.forEach(AbstractModule::onEnable);
    }

    public void disableModules() {
        modules.forEach(AbstractModule::onDisable);
    }

    public void reloadModules() {
        modules.forEach(AbstractModule::onReload);
    }

    public List<AbstractModule> getModules() {
        return modules;
    }
}

