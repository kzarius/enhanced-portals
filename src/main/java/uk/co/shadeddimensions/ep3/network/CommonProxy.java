package uk.co.shadeddimensions.ep3.network;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.block.BlockCrafting;
import uk.co.shadeddimensions.ep3.block.BlockDecoration;
import uk.co.shadeddimensions.ep3.block.BlockFrame;
import uk.co.shadeddimensions.ep3.block.BlockNetherPortal;
import uk.co.shadeddimensions.ep3.block.BlockPortal;
import uk.co.shadeddimensions.ep3.block.BlockStabilizer;
import uk.co.shadeddimensions.ep3.crafting.ThermalExpansion;
import uk.co.shadeddimensions.ep3.crafting.Vanilla;
import uk.co.shadeddimensions.ep3.item.ItemDecoration;
import uk.co.shadeddimensions.ep3.item.ItemEntityCard;
import uk.co.shadeddimensions.ep3.item.ItemGoggles;
import uk.co.shadeddimensions.ep3.item.ItemGuide;
import uk.co.shadeddimensions.ep3.item.ItemHandheldScanner;
import uk.co.shadeddimensions.ep3.item.ItemLocationCard;
import uk.co.shadeddimensions.ep3.item.ItemMisc;
import uk.co.shadeddimensions.ep3.item.ItemPaintbrush;
import uk.co.shadeddimensions.ep3.item.ItemPortalModule;
import uk.co.shadeddimensions.ep3.item.ItemSynchronizer;
import uk.co.shadeddimensions.ep3.item.ItemUpgrade;
import uk.co.shadeddimensions.ep3.item.ItemWrench;
import uk.co.shadeddimensions.ep3.item.block.ItemFrame;
import uk.co.shadeddimensions.ep3.item.block.ItemStabilizer;
import uk.co.shadeddimensions.ep3.lib.GUIs;
import uk.co.shadeddimensions.ep3.lib.Reference;
import uk.co.shadeddimensions.ep3.network.packet.PacketTileUpdate;
import uk.co.shadeddimensions.ep3.portal.NetworkManager;
import uk.co.shadeddimensions.ep3.tileentity.TileEnhancedPortals;
import uk.co.shadeddimensions.ep3.tileentity.TilePortal;
import uk.co.shadeddimensions.ep3.tileentity.TileStabilizer;
import uk.co.shadeddimensions.ep3.tileentity.TileStabilizerMain;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileBiometricIdentifier;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileDiallingDevice;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileFrame;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileModuleManipulator;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileNetworkInterface;
import uk.co.shadeddimensions.ep3.tileentity.frame.TilePortalController;
import uk.co.shadeddimensions.ep3.tileentity.frame.TileRedstoneInterface;
import uk.co.shadeddimensions.ep3.util.ConfigHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy
{
    public static final int REDSTONE_FLUX_COST = 10000;
    public static final int REDSTONE_FLUX_TIMER = 20;

    public static BlockFrame blockFrame;
    public static BlockPortal blockPortal;
    public static BlockStabilizer blockStabilizer;
    public static BlockDecoration blockDecoration;
    public static BlockCrafting blockCrafting;
    
    public static ItemWrench itemWrench;
    public static ItemPaintbrush itemPaintbrush;
    public static ItemGoggles itemGoggles;
    public static ItemPortalModule itemPortalModule;
    public static ItemLocationCard itemLocationCard;
    public static ItemSynchronizer itemSynchronizer;
    public static ItemEntityCard itemEntityCard;
    public static ItemHandheldScanner itemScanner;
    public static ItemUpgrade itemInPlaceUpgrade;
    public static ItemMisc itemMisc;
    public static ItemGuide itemGuide;

    public int gogglesRenderIndex = 0;

    public static NetworkManager networkManager;

    public static final Logger logger = Logger.getLogger(Reference.NAME);
    public static ConfigHandler configuration;

    public static boolean useAlternateGlyphs, customNetherPortals, portalsDestroyBlocks, fasterPortalCooldown, disableVanillaRecipes, disableTERecipes, disablePortalSounds, disableParticles, forceShowFrameOverlays, disablePigmen, netherDisableParticles, netherDisableSounds;
    public static int redstoneFluxPowerMultiplier;

    public static void openGui(EntityPlayer player, GUIs gui, TileEnhancedPortals tile)
    {
        openGui(player, gui.ordinal(), tile);
    }

    public static void openGui(EntityPlayer player, int id, TileEnhancedPortals tile)
    {
        player.openGui(EnhancedPortals.instance, id, tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord);
    }

    public static void sendPacketToAllAround(TileEntity tile, Packet250CustomPayload packet)
    {
        PacketDispatcher.sendPacketToAllAround(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, 128, tile.worldObj.provider.dimensionId, packet);
    }

    public static void sendUpdatePacketToAllAround(TileEnhancedPortals tile)
    {
        sendPacketToAllAround(tile, new PacketTileUpdate(tile).getPacket());
    }

    public static void sendUpdatePacketToPlayer(TileEnhancedPortals tile, EntityPlayer player)
    {
        PacketDispatcher.sendPacketToPlayer(new PacketTileUpdate(tile).getPacket(), (Player) player);
    }

    public File getBaseDir()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFile(".");
    }

    public File getResourcePacksDir()
    {
        return new File(getBaseDir(), "resourcepacks");
    }

    public File getWorldDir()
    {
        return new File(getBaseDir(), DimensionManager.getWorld(0).getSaveHandler().getWorldDirectoryName());
    }

    boolean reflectBlock(Block block, Class<? extends Block> clazz)
    {
        Field field = null;

        for (Field f : net.minecraft.block.Block.class.getDeclaredFields())
        {
            if (f.getType() == clazz)
            {
                field = f;
                break;
            }
        }

        if (field == null)
        {
            return false;
        }

        field.setAccessible(true);

        if ((field.getModifiers() & Modifier.FINAL) != 0)
        {
            try
            {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            catch (Exception e)
            {
                return false;
            }
        }

        try
        {
            field.set(null, block);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public void miscSetup()
    {
        ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(itemPortalModule, 1, 4), 1, 1, 2));

        if (customNetherPortals)
        {
            int portalID = Block.portal.blockID;
            Block.blocksList[portalID] = null;

            if (!reflectBlock(new BlockNetherPortal(portalID), net.minecraft.block.BlockPortal.class))
            {
                Block.blocksList[portalID] = null;
                Block.blocksList[portalID] = new net.minecraft.block.BlockPortal(portalID);
                logger.warning("Unable to modify BlockPortal. Custom Nether Portals have been disabled.");
            }
        }
    }

    public void registerBlocks()
    {
        blockFrame = new BlockFrame(configuration.getBlockId("Frame"), "ep3.portalFrame");
        GameRegistry.registerBlock(blockFrame, ItemFrame.class, "ep3.portalFrame");

        blockPortal = new BlockPortal(configuration.getBlockId("Portal"), "ep3.portal");
        GameRegistry.registerBlock(blockPortal, "ep3.portal");

        blockStabilizer = new BlockStabilizer(configuration.getBlockId("DimensionalBridgeStabilizer"), "ep3.stabilizer");
        GameRegistry.registerBlock(blockStabilizer, ItemStabilizer.class, "ep3.stabilizer");

        blockDecoration = new BlockDecoration(configuration.getBlockId("Decoration"), "ep3.decoration");
        GameRegistry.registerBlock(blockDecoration, ItemDecoration.class, "ep3.decoration");
        
        blockCrafting = new BlockCrafting(configuration.getBlockId("Crafting"), "ep3.crafting");
        GameRegistry.registerBlock(blockCrafting, "ep3.crafting");
    }

    public void registerItems()
    {
        itemWrench = new ItemWrench(configuration.getItemId("Wrench"), "ep3.wrench");
        GameRegistry.registerItem(itemWrench, "ep3.wrench");

        itemPaintbrush = new ItemPaintbrush(configuration.getItemId("Paintbrush"), "ep3.paintbrush");
        GameRegistry.registerItem(itemPaintbrush, "ep3.paintbrush");

        itemGoggles = new ItemGoggles(configuration.getItemId("Glasses"), "ep3.goggles");
        GameRegistry.registerItem(itemGoggles, "ep3.goggles");

        itemLocationCard = new ItemLocationCard(configuration.getItemId("LocationCard"), "ep3.locationCard");
        GameRegistry.registerItem(itemLocationCard, "ep3.locationCard");

        itemPortalModule = new ItemPortalModule(configuration.getItemId("PortalModule"), "ep3.portalModule");
        GameRegistry.registerItem(itemPortalModule, "ep3.portalModule");

        itemSynchronizer = new ItemSynchronizer(configuration.getItemId("Synchronizer"), "ep3.synchronizer");
        GameRegistry.registerItem(itemSynchronizer, "ep3.synchronizer");

        itemEntityCard = new ItemEntityCard(configuration.getItemId("EntityCard"), "ep3.entityCard");
        GameRegistry.registerItem(itemEntityCard, "ep3.entityCard");

        itemScanner = new ItemHandheldScanner(configuration.getItemId("HandheldScanner"), "ep3.handheldScanner");
        GameRegistry.registerItem(itemScanner, "ep3.handheldScanner");

        itemInPlaceUpgrade = new ItemUpgrade(configuration.getItemId("InPlaceUpgrade"), "ep3.inPlaceUpgrade");
        GameRegistry.registerItem(itemInPlaceUpgrade, "ep3.inPlaceUpgrade");

        itemMisc = new ItemMisc(configuration.getItemId("MiscItems"), "ep3.miscItems");
        GameRegistry.registerItem(itemMisc, "ep3.miscItems");

        itemGuide = new ItemGuide(configuration.getItemId("Manual"), "ep3.guide");
        GameRegistry.registerItem(itemGuide, "ep3.guide");
    }

    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TilePortal.class, "epPortal");
        GameRegistry.registerTileEntity(TileFrame.class, "epPortalFrame");
        GameRegistry.registerTileEntity(TilePortalController.class, "epPortalController");
        GameRegistry.registerTileEntity(TileRedstoneInterface.class, "epRedstoneInterface");
        GameRegistry.registerTileEntity(TileNetworkInterface.class, "epNetworkInterface");
        GameRegistry.registerTileEntity(TileDiallingDevice.class, "epDiallingDevice");
        GameRegistry.registerTileEntity(TileBiometricIdentifier.class, "epBiometricIdentifier");
        GameRegistry.registerTileEntity(TileModuleManipulator.class, "epModuleManipulator");
        GameRegistry.registerTileEntity(TileStabilizer.class, "epStabilizer");
        GameRegistry.registerTileEntity(TileStabilizerMain.class, "epStabilizerMain");
    }

    public void setupConfiguration(Configuration theConfig)
    {
        configuration = new ConfigHandler(Reference.VERSION);
        configuration.setConfiguration(theConfig);

        configuration.addBlockEntry("Portal");
        configuration.addBlockEntry("Frame");
        configuration.addBlockEntry("DimensionalBridgeStabilizer");
        configuration.addBlockEntry("Decoration");
        configuration.addBlockEntry("Crafting");

        configuration.addItemEntry("Wrench");
        configuration.addItemEntry("Glasses");
        configuration.addItemEntry("Paintbrush");
        configuration.addItemEntry("LocationCard");
        configuration.addItemEntry("Synchronizer");
        configuration.addItemEntry("EntityCard");
        configuration.addItemEntry("HandheldScanner");
        configuration.addItemEntry("MiscItems");
        configuration.addItemEntry("PortalModule");
        configuration.addItemEntry("InPlaceUpgrade");
        configuration.addItemEntry("Manual");

        useAlternateGlyphs = configuration.get("Misc", "UseAlternateGlyphs", false);
        forceShowFrameOverlays = configuration.get("Misc", "ForceShowFrameOverlays", false);

        customNetherPortals = configuration.get("Overrides", "CustomNetherPortals", true);
        disablePigmen = configuration.get("Overrides", "StopPigmenFromSpawningAtPortals", false);
        netherDisableParticles = configuration.get("Overrides", "DisableNetherParticles", false);
        netherDisableSounds = configuration.get("Overrides", "DisableNetherSounds", false);
        disablePortalSounds = configuration.get("Overrides", "DisablePortalSounds", false);
        disableParticles = configuration.get("Overrides", "DisableParticles", false);

        portalsDestroyBlocks = configuration.get("Portal", "PortalsDestroyBlocks", true);
        fasterPortalCooldown = configuration.get("Portal", "FasterPortalCooldown", false);

        redstoneFluxPowerMultiplier = configuration.get("Power", "PowerMultiplier", 1);

        disableVanillaRecipes = configuration.get("Recipes", "DisableVanillaRecipes", false);
        disableTERecipes = configuration.get("Recipes", "DisableTERecipes", false);

        if (redstoneFluxPowerMultiplier < 0)
        {
            redstoneFluxPowerMultiplier = 0;
        }

        configuration.init();
    }

    public void setupCrafting()
    {
        Vanilla.registerRecipes();
        
        if (Loader.isModLoaded("ThermalExpansion") && !CommonProxy.disableTERecipes)
        {
            ThermalExpansion.registerRecipes();
            ThermalExpansion.registerMachineRecipes();
        }
    }
}