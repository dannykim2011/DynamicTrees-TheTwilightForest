package com.dtteam.dttwilightforest;

import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dttwilightforest.init.DTTFClient;
import com.dtteam.dttwilightforest.init.DTTFRegistries;
import com.dtteam.dttwilightforest.loot.LootModifiers;
import com.dtteam.dttwilightforest.worldgen.TwilightForestGroundFinder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DynamicTreesTheTwilightForest.MOD_ID)
public class DynamicTreesTheTwilightForest {
    public static final String MOD_ID = "dttwilightforest";

    public DynamicTreesTheTwilightForest(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);

        ifDynamicTreesPlusLoaded(() -> modEventBus.register(getDynamicTreesPlusRegistriesClass()));

        LootModifiers.register(modEventBus);

        NeoForgeRegistryHandler.setup(MOD_ID, modEventBus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTTFClient.setup();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DTTFRegistries.setup();
        ifDynamicTreesPlusLoaded(() -> invokeDynamicTreesPlusRegistriesMethod("setup"));
        GroundFinder.registerGroundFinder(
                ResourceKey.create(
                        Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath("twilightforest", "twilight_forest")
                ),
                new TwilightForestGroundFinder()
        );
    }

    private void gatherData(final GatherDataEvent event) {
        if (ModList.get().isLoaded("dynamictreesplus")) {
            invokeDynamicTreesPlusRegistriesMethod("gatherData", GatherDataEvent.class, event);
        } else {
            GatherDataHelper.gatherAllData(MOD_ID, event,
                    SoilProperties.REGISTRY,
                    Family.REGISTRY,
                    Species.REGISTRY,
                    LeavesProperties.REGISTRY
            );
        }
    }

    public static ResourceLocation location(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void ifDynamicTreesPlusLoaded(final Runnable action) {
        if (ModList.get().isLoaded("dynamictreesplus")) {
            action.run();
        }
    }

    private static Class<?> getDynamicTreesPlusRegistriesClass() {
        try {
            return Class.forName("com.dtteam.dttwilightforest.init.DTTFPlusRegistries");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load Dynamic Trees Plus integration class.", e);
        }
    }

    private static void invokeDynamicTreesPlusRegistriesMethod(final String methodName) {
        invokeDynamicTreesPlusRegistriesMethod(methodName, new Class<?>[0]);
    }

    private static void invokeDynamicTreesPlusRegistriesMethod(final String methodName, final Class<?> parameterType, final Object argument) {
        invokeDynamicTreesPlusRegistriesMethod(methodName, new Class<?>[]{parameterType}, argument);
    }

    private static void invokeDynamicTreesPlusRegistriesMethod(final String methodName, final Class<?>[] parameterTypes, final Object... arguments) {
        try {
            final Method method = getDynamicTreesPlusRegistriesClass().getMethod(methodName, parameterTypes);
            method.invoke(null, arguments);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Could not invoke Dynamic Trees Plus integration method: " + methodName, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Dynamic Trees Plus integration failed while invoking: " + methodName, e.getCause());
        }
    }

}
