package enhancedportals.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enhancedportals.EnhancedPortals;
import enhancedportals.lib.BlockIds;
import enhancedportals.lib.GuiIds;
import enhancedportals.lib.Localization;
import enhancedportals.lib.PortalTexture;
import enhancedportals.lib.Reference;
import enhancedportals.tileentity.TileEntityPortalModifier;

public class BlockPortalModifier extends BlockEnhancedPortals
{
    Icon texture;

    public BlockPortalModifier()
    {
        super(BlockIds.PortalModifier, Material.rock);
        setCreativeTab(Reference.CREATIVE_TAB);
        setCanRotate();
        setHardness(25.0F);
        setResistance(2000.0F);
        setStepSound(soundStoneFootstep);
        setUnlocalizedName(Localization.PortalModifier_Name);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileEntityPortalModifier();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        TileEntityPortalModifier modifier = (TileEntityPortalModifier) blockAccess.getBlockTileEntity(x, y, z);

        return side == blockAccess.getBlockMetadata(x, y, z) ? modifier.texture.getModifierIcon() : texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        return side == 1 ? new PortalTexture(0).getModifierIcon() : texture;
    }

    @Override
    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
    {
        return ForgeDirection.VALID_DIRECTIONS;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        if (!player.isSneaking())
        {
            player.openGui(EnhancedPortals.instance, GuiIds.PortalModifier, world, x, y, z);
            return true;
        }

        return super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
    {
        boolean currentRedstoneState = world.getStrongestIndirectPower(x, y, z) > 0;

        ((TileEntityPortalModifier) world.getBlockTileEntity(x, y, z)).handleRedstoneChanges(currentRedstoneState);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister)
    {
        texture = iconRegister.registerIcon(Reference.MOD_ID + ":" + Localization.PortalModifier_Name + "_Side");
    }
}
